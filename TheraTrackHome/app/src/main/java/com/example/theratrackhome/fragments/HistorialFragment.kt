package com.example.theratrackhome.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.RegistroController
import com.example.theratrackhome.model.RegistroDiario
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialFragment : Fragment() {

    private lateinit var layoutMediciones: LinearLayout
    private lateinit var tvSinRegistros: TextView
    private lateinit var lineChart: LineChart
    private lateinit var chipHoy: TextView
    private lateinit var chip7Dias: TextView
    private lateinit var chip30Dias: TextView

    private var pacienteId: String = ""
    private var diasSeleccionados: Int = 7

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnBack)?.visibility = View.GONE

        layoutMediciones = view.findViewById(R.id.layoutMediciones)
        tvSinRegistros   = view.findViewById(R.id.tvSinRegistros)
        lineChart        = view.findViewById(R.id.lineChart)
        chipHoy          = view.findViewById(R.id.chipHoy)
        chip7Dias        = view.findViewById(R.id.chip7Dias)
        chip30Dias       = view.findViewById(R.id.chip30Dias)

        configurarChips()
        configurarGrafica()
        cargarDatos()
    }

    private fun configurarChips() {
        fun activarChip(activo: TextView, vararg inactivos: TextView) {
            activo.setBackgroundResource(R.drawable.bg_chip_active)
            activo.setTextColor(requireContext().getColor(R.color.white))
            activo.setTypeface(null, android.graphics.Typeface.BOLD)
            inactivos.forEach {
                it.setBackgroundResource(R.drawable.bg_chip_inactive)
                it.setTextColor(requireContext().getColor(R.color.colorTextSecondary))
                it.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }

        chipHoy.setOnClickListener {
            diasSeleccionados = 1
            activarChip(chipHoy, chip7Dias, chip30Dias)
            cargarDatos()
        }
        chip7Dias.setOnClickListener {
            diasSeleccionados = 7
            activarChip(chip7Dias, chipHoy, chip30Dias)
            cargarDatos()
        }
        chip30Dias.setOnClickListener {
            diasSeleccionados = 30
            activarChip(chip30Dias, chipHoy, chip7Dias)
            cargarDatos()
        }
    }

    private fun configurarGrafica() {
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setNoDataText("Sin datos en este período")
            setNoDataTextColor(Color.parseColor("#6B7280"))

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.parseColor("#6B7280")
                textSize = 10f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
                textColor = Color.parseColor("#6B7280")
                axisMinimum = 0f
                axisMaximum = 5.5f
                granularity = 1f
            }
            axisRight.isEnabled = false
        }
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    pacienteId = paciente.id
                    RegistroController.obtenerRegistros(paciente.id, diasSeleccionados)
                        .onSuccess { registros ->
                            val ordenados = registros.sortedBy { it.fecha }
                            mostrarGrafica(ordenados)
                            mostrarLista(ordenados)
                        }
                        .onFailure { mostrarEstadoVacio() }
                }
                .onFailure { mostrarEstadoVacio() }
        }
    }

    private fun mostrarGrafica(registros: List<RegistroDiario>) {
        if (registros.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Sin datos en este período")
            lineChart.invalidate()
            return
        }

        val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("dd MMM", Locale.forLanguageTag("es"))
        val etiquetas = registros.map {
            try { sdfOut.format(sdfIn.parse(it.fecha)!!) } catch (e: Exception) { it.fecha }
        }
        val entries = registros.mapIndexed { i, r ->
            Entry(i.toFloat(), ((r.nivelFatiga ?: 1) + (r.nivelDolor ?: 1)).toFloat() / 2f)
        }

        val dataSet = LineDataSet(entries, "Nivel").apply {
            color = Color.parseColor("#1A56DB")
            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 4.5f
            setCircleColor(Color.parseColor("#1A56DB"))
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#EBF0FA")
            fillAlpha = 200
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(etiquetas)
        lineChart.data = LineData(dataSet)
        lineChart.animateX(600)
        lineChart.invalidate()
    }

    private fun mostrarLista(registros: List<RegistroDiario>) {
        layoutMediciones.removeAllViews()

        if (registros.isEmpty()) {
            mostrarEstadoVacio()
            return
        }

        tvSinRegistros.visibility = View.GONE
        val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("dd MMM · HH:mm", Locale.forLanguageTag("es"))

        registros.reversed().forEach { registro ->
            val item = LayoutInflater.from(requireContext()).inflate(R.layout.item_medicion, layoutMediciones, false)

            val nivelPromedio = ((registro.nivelFatiga ?: 1) + (registro.nivelDolor ?: 1)) / 2
            val esSeguro = nivelPromedio <= 2

            item.findViewById<FrameLayout>(R.id.iconEstadoContainer)
                .setBackgroundResource(
                    if (esSeguro) R.drawable.bg_circle_green_solid
                    else R.drawable.bg_circle_orange_solid
                )
            item.findViewById<ImageView>(R.id.ivEstadoIcon)
                .setImageResource(
                    if (esSeguro) R.drawable.ic_check_white
                    else R.drawable.ic_warning_white
                )

            val fecha = try { sdfOut.format(sdfIn.parse(registro.fecha)!!) }
                        catch (e: Exception) { registro.fecha }

            item.findViewById<TextView>(R.id.tvFecha).text = fecha
            item.findViewById<TextView>(R.id.tvNivelSeguridad).text =
                "Nivel de seguridad: ${if (esSeguro) "Seguro" else "Precaución"}"
            item.findViewById<TextView>(R.id.tvValor).text = "$nivelPromedio/5"

            layoutMediciones.addView(item)
        }
    }

    private fun mostrarEstadoVacio() {
        layoutMediciones.removeAllViews()
        tvSinRegistros.visibility = View.VISIBLE
        lineChart.clear()
        lineChart.invalidate()
    }
}
