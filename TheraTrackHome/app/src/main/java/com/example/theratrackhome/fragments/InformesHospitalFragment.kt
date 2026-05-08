package com.example.theratrackhome.fragments

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.CsvExporter
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.PdfReportGenerator
import com.example.theratrackhome.controller.RegistroController
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.model.Recomendacion
import com.example.theratrackhome.model.RegistroDiario
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InformesHospitalFragment : Fragment() {

    private lateinit var spinnerPaciente: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnFechaDesde: MaterialButton
    private lateinit var btnFechaHasta: MaterialButton
    private lateinit var btnGenerar: MaterialButton
    private lateinit var btnPdf: MaterialButton
    private lateinit var btnCsv: MaterialButton
    private lateinit var tvPreviewTitulo: TextView
    private lateinit var tvPreviewMeta: TextView
    private lateinit var tvResumenSintomas: TextView
    private lateinit var tvAlertasClinicas: TextView
    private lateinit var chart: BarChart

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var fechaDesde: String = ""
    private var fechaHasta: String = ""
    private var medicoResponsable: String = "No especificado"
    private var pacientes: List<Paciente> = emptyList()
    private var perfiles: Map<String, Perfil> = emptyMap()
    private var informeActual: InformeData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_informes_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavHospital)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnBackInformes)?.visibility = View.GONE

        spinnerPaciente   = view.findViewById(R.id.spinnerPaciente)
        spinnerTipo       = view.findViewById(R.id.spinnerTipoInforme)
        btnFechaDesde     = view.findViewById(R.id.btnFechaDesde)
        btnFechaHasta     = view.findViewById(R.id.btnFechaHasta)
        btnGenerar        = view.findViewById(R.id.btnGenerarInforme)
        btnPdf            = view.findViewById(R.id.btnDescargarPdf)
        btnCsv            = view.findViewById(R.id.btnExportarCsv)
        tvPreviewTitulo   = view.findViewById(R.id.tvPreviewTitulo)
        tvPreviewMeta     = view.findViewById(R.id.tvPreviewMeta)
        tvResumenSintomas = view.findViewById(R.id.tvResumenSintomas)
        tvAlertasClinicas = view.findViewById(R.id.tvAlertasClinicas)
        chart             = view.findViewById(R.id.chartSintomas)

        configurarFechasIniciales()
        configurarTipoInforme()
        configurarEventos()
        cargarPacientes()
    }

    private fun configurarFechasIniciales() {
        val hasta = Calendar.getInstance()
        val desde = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -30) }
        fechaDesde = sdf.format(desde.time)
        fechaHasta = sdf.format(hasta.time)
        btnFechaDesde.text = fechaDesde
        btnFechaHasta.text = fechaHasta
    }

    private fun configurarTipoInforme() {
        val tipos = listOf("Informe Clínico Completo", "Resumen Semanal", "Informe de Alertas", "Informe de Dosimetría")
        spinnerTipo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipos)
    }

    private fun configurarEventos() {
        btnFechaDesde.setOnClickListener { seleccionarFecha(true) }
        btnFechaHasta.setOnClickListener { seleccionarFecha(false) }
        btnGenerar.setOnClickListener { generarInforme() }
        btnPdf.setOnClickListener { descargarPdf() }
        btnCsv.setOnClickListener { exportarCsv() }
    }

    private fun seleccionarFecha(esDesde: Boolean) {
        val cal = Calendar.getInstance()
        runCatching { cal.time = sdf.parse(if (esDesde) fechaDesde else fechaHasta) ?: Date() }
        DatePickerDialog(requireContext(), { _, year, month, day ->
            cal.set(year, month, day)
            val value = sdf.format(cal.time)
            if (esDesde) {
                fechaDesde = value
                btnFechaDesde.text = value
            } else {
                fechaHasta = value
                btnFechaHasta.text = value
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun cargarPacientes() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    medicoResponsable = perfil.nombre
                    val hid = perfil.hospitalId ?: return@onSuccess mostrarError("Profesional sin hospital asignado")
                    PacienteController.obtenerPacientesHospital(hid)
                        .onSuccess { lista ->
                            pacientes = lista
                            val mapa = mutableMapOf<String, Perfil>()
                            lista.forEach { paciente ->
                                PacienteController.obtenerPerfilPorId(paciente.id).onSuccess { mapa[paciente.id] = it }
                            }
                            perfiles = mapa
                            val nombres = lista.map { p -> mapa[p.id]?.nombre ?: "Paciente ${p.id.take(8)}" }
                            spinnerPaciente.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
                            if (lista.isEmpty()) mostrarError("No hay pacientes disponibles")
                        }
                        .onFailure { mostrarError("Error al cargar pacientes: ${it.message}") }
                }
                .onFailure { mostrarError("Error al cargar perfil: ${it.message}") }
        }
    }

    private fun generarInforme() {
        val paciente = pacientes.getOrNull(spinnerPaciente.selectedItemPosition)
            ?: return mostrarError("Seleccione un paciente")
        val perfil = perfiles[paciente.id]
            ?: return mostrarError("No se pudo cargar el perfil del paciente")

        viewLifecycleOwner.lifecycleScope.launch {
            btnGenerar.isEnabled = false
            val registros = RegistroController.obtenerRegistrosRango(paciente.id, fechaDesde, fechaHasta).getOrElse {
                btnGenerar.isEnabled = true
                return@launch mostrarError("Error al cargar registros: ${it.message}")
            }
            val alertas = PacienteController.obtenerAlertasPacienteEnRango(paciente.id, fechaDesde, fechaHasta).getOrDefault(emptyList())
            val recomendaciones = PacienteController.obtenerRecomendacionesPaciente(paciente.hospitalId).getOrDefault(emptyList())
                .filter { it.pacienteId == null || it.pacienteId == paciente.id }
            informeActual = InformeData(paciente, perfil, registros, alertas, recomendaciones)
            mostrarPreview(informeActual!!)
            btnPdf.isEnabled = true
            btnCsv.isEnabled = true
            btnGenerar.isEnabled = true
        }
    }

    private fun mostrarPreview(data: InformeData) {
        val ref = "TT-${Calendar.getInstance().get(Calendar.YEAR)}-${data.paciente.id.take(5).uppercase()}"
        tvPreviewTitulo.text = "INFORME CLÍNICO - THERATRACK HOME"
        tvPreviewMeta.text = "Ref: $ref\nPaciente: ${data.perfil.nombre}\nEmisión: ${java.text.DateFormat.getDateTimeInstance().format(Date())}\nMédico responsable: $medicoResponsable\nTipo: ${spinnerTipo.selectedItem}"
        tvResumenSintomas.text = resumenSintomas(data.registros)
        tvAlertasClinicas.text = if (data.alertas.isEmpty()) "Alertas clínicas: sin alertas en el rango." else {
            buildString {
                appendLine("Alertas clínicas:")
                data.alertas.take(8).forEach { appendLine("• ${it.tipo}: ${it.titulo} (${if (it.leida) "leída" else "pendiente"})") }
            }
        }
        configurarChart(data.registros)
    }

    private fun resumenSintomas(registros: List<RegistroDiario>): String {
        val conteo = linkedMapOf<String, Int>()
        registros.flatMap { it.sintomas.orEmpty().split(",", ";") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { conteo[it] = (conteo[it] ?: 0) + 1 }
        if (conteo.isEmpty()) return "Resumen de síntomas: sin síntomas reportados."
        return buildString {
            appendLine("Resumen de síntomas:")
            conteo.toList().sortedByDescending { it.second }.forEach { appendLine("• ${it.first}: ${it.second}") }
        }
    }

    private fun configurarChart(registros: List<RegistroDiario>) {
        val entries = registros.takeLast(14).mapIndexed { index, r ->
            val value = r.nivelDolor ?: r.nivelFatiga ?: 0
            BarEntry(index.toFloat(), value.toFloat())
        }
        val set = BarDataSet(entries, "Dolor/Fatiga").apply { color = requireContext().getColor(R.color.colorPrimary) }
        chart.data = BarData(set)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.invalidate()
    }

    private fun descargarPdf() {
        val data = informeActual ?: return mostrarError("Genere un informe primero")
        viewLifecycleOwner.lifecycleScope.launch {
            PdfReportGenerator.generarInformeClinico(
                requireContext(),
                data.paciente,
                data.perfil,
                data.registros,
                data.alertas,
                data.recomendaciones,
                medicoResponsable,
                fechaDesde,
                fechaHasta
            ).onSuccess { abrirPdf(it) }
                .onFailure { mostrarError("Error al generar PDF: ${it.message}") }
        }
    }

    private fun exportarCsv() {
        val data = informeActual ?: return mostrarError("Genere un informe primero")
        viewLifecycleOwner.lifecycleScope.launch {
            CsvExporter.exportarRegistros(requireContext(), data.paciente, data.registros)
                .onSuccess { compartirCsv(it) }
                .onFailure { mostrarError("Error al exportar CSV: ${it.message}") }
        }
    }

    private fun uriFor(file: File) = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)

    private fun abrirPdf(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uriFor(file), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "PDF generado, pero no hay una app para abrirlo", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            Toast.makeText(requireContext(), "PDF generado, pero no se pudo abrir: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun compartirCsv(file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uriFor(file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Compartir CSV"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "CSV generado, pero no hay una app para compartirlo", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            Toast.makeText(requireContext(), "CSV generado, pero no se pudo compartir: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private data class InformeData(
        val paciente: Paciente,
        val perfil: Perfil,
        val registros: List<RegistroDiario>,
        val alertas: List<Alerta>,
        val recomendaciones: List<Recomendacion>
    )
}
