package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit // Importación corregida

class DashboardHospitalActivity : BaseHospitalActivity() {

    private lateinit var tvSaludo: TextView
    private lateinit var tvNombreHospital: TextView
    private lateinit var tvTotalPacientes: TextView
    private lateinit var tvAlertasActivas: TextView
    private lateinit var tvEnSeguimiento: TextView
    private lateinit var tvAltaCompletada: TextView
    private lateinit var containerAlertas: LinearLayout
    private lateinit var tvSinAlertas: TextView
    private lateinit var containerPacientes: LinearLayout
    private lateinit var tvSinPacientes: TextView
    private lateinit var tvVerTodasAlertas: TextView
    private lateinit var tvVerTodosPacientes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_hospital)

        tvSaludo            = findViewById(R.id.tvSaludo)
        tvNombreHospital    = findViewById(R.id.tvNombreHospital)
        tvTotalPacientes    = findViewById(R.id.tvTotalPacientes)
        tvAlertasActivas    = findViewById(R.id.tvAlertasActivas)
        tvEnSeguimiento     = findViewById(R.id.tvEnSeguimiento)
        tvAltaCompletada    = findViewById(R.id.tvAltaCompletada)
        containerAlertas    = findViewById(R.id.containerAlertas)
        tvSinAlertas        = findViewById(R.id.tvSinAlertas)
        containerPacientes  = findViewById(R.id.containerPacientes)
        tvSinPacientes      = findViewById(R.id.tvSinPacientes)
        tvVerTodasAlertas   = findViewById(R.id.tvVerTodasAlertas)
        tvVerTodosPacientes = findViewById(R.id.tvVerTodosPacientes)

        configurarNavegacion(R.id.nav_dashboard)

        tvVerTodasAlertas.setOnClickListener {
            startActivity(Intent(this, AlertasHospitalActivity::class.java))
        }
        tvVerTodosPacientes.setOnClickListener {
            startActivity(Intent(this, PacientesHospitalActivity::class.java))
        }

        cargarDatos()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvSaludo.text = "Bienvenido, ${perfil.nombre}"
                    val hospitalId = perfil.hospitalId ?: return@onSuccess

                    PacienteController.obtenerNombreHospital(hospitalId)
                        .onSuccess { nombre -> tvNombreHospital.text = nombre }

                    PacienteController.obtenerPacientesHospital(hospitalId)
                        .onSuccess { pacientes ->
                            mostrarMetricas(pacientes)
                            mostrarPacientesRecientes(pacientes.take(3))

                            val ids = pacientes.mapNotNull { it.id }
                            PacienteController.obtenerAlertasNoLeidasPacientes(ids)
                                .onSuccess { alertas ->
                                    mostrarAlertasRecientes(alertas.take(3))
                                    tvAlertasActivas.text = alertas.size.toString()
                                }
                                .onFailure {
                                    tvAlertasActivas.text = "–"
                                    tvSinAlertas.visibility = View.VISIBLE
                                }
                        }
                        .onFailure {
                            Toast.makeText(this@DashboardHospitalActivity, "Error al cargar pacientes", Toast.LENGTH_SHORT).show()
                        }
                }
                .onFailure {
                    Toast.makeText(this@DashboardHospitalActivity, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarMetricas(pacientes: List<Paciente>) {
        tvTotalPacientes.text = pacientes.size.toString()

        var enSeguimiento = 0
        var altaCompletada = 0

        for (p in pacientes) {
            val dias = calcularDiasRestantes(p)
            if (dias > 0) enSeguimiento++ else altaCompletada++
        }

        tvEnSeguimiento.text = enSeguimiento.toString()
        tvAltaCompletada.text = altaCompletada.toString()
    }

    private fun mostrarAlertasRecientes(alertas: List<Alerta>) {
        if (alertas.isEmpty()) {
            tvSinAlertas.visibility = View.VISIBLE
            return
        }
        tvSinAlertas.visibility = View.GONE
        for (alerta in alertas) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_alerta_dashboard, containerAlertas, false)
            view.findViewById<TextView>(R.id.tvAlertaTitulo).text = alerta.titulo
            view.findViewById<TextView>(R.id.tvAlertaMensaje).text = alerta.mensaje
            view.findViewById<TextView>(R.id.tvAlertaTipo).text = alerta.tipo
            containerAlertas.addView(view)
        }
    }

    private fun mostrarPacientesRecientes(pacientes: List<Paciente>) {
        if (pacientes.isEmpty()) {
            tvSinPacientes.visibility = View.VISIBLE
            return
        }
        tvSinPacientes.visibility = View.GONE
        for (paciente in pacientes) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_paciente_dashboard, containerPacientes, false)
            val iniciales = paciente.id.take(2).uppercase()
            view.findViewById<TextView>(R.id.tvPacienteIniciales).text = iniciales
            view.findViewById<TextView>(R.id.tvPacienteId).text = "Pac. ${paciente.id.take(8)}"
            view.findViewById<TextView>(R.id.tvPacienteFarmaco).text = paciente.radiofarmaco
            val dias = calcularDiasRestantes(paciente)
            val diasText = if (dias > 0) "${dias}d restantes" else "Alta completada"
            view.findViewById<TextView>(R.id.tvPacienteDias).text = diasText
            view.setOnClickListener {
                val intent = Intent(this, DetallePacienteActivity::class.java)
                intent.putExtra("paciente_id", paciente.id)
                startActivity(intent)
            }
            containerPacientes.addView(view)
        }
    }

    private fun calcularDiasRestantes(paciente: Paciente): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaAlta = sdf.parse(paciente.fechaAlta) ?: return 0L // Retorno como Long
            val cal = Calendar.getInstance()
            cal.time = fechaAlta
            cal.add(Calendar.DAY_OF_YEAR, paciente.diasAislamiento)
            val finAislamiento = cal.time
            val hoy = Calendar.getInstance().time
            val diff = finAislamiento.time - hoy.time
            TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
        } catch (e: Exception) {
            0L // Retorno como Long
        }
    }
}