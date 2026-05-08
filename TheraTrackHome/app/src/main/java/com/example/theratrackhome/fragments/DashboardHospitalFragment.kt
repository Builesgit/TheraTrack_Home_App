package com.example.theratrackhome.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.DetallePacienteActivity
import com.example.theratrackhome.HospitalMainActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.SolicitudesPendientesActivity
import com.example.theratrackhome.controller.AdminController
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.SyncNotificacionesManager
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardHospitalFragment : Fragment() {

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
    private lateinit var cardSolicitudesPendientes: View
    private lateinit var tvContadorSolicitudes: TextView

    private var hospitalId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_dashboard_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavHospital)?.visibility = View.GONE

        tvSaludo                  = view.findViewById(R.id.tvSaludo)
        tvNombreHospital          = view.findViewById(R.id.tvNombreHospital)
        tvTotalPacientes          = view.findViewById(R.id.tvTotalPacientes)
        tvAlertasActivas          = view.findViewById(R.id.tvAlertasActivas)
        tvEnSeguimiento           = view.findViewById(R.id.tvEnSeguimiento)
        tvAltaCompletada          = view.findViewById(R.id.tvAltaCompletada)
        containerAlertas          = view.findViewById(R.id.containerAlertas)
        tvSinAlertas              = view.findViewById(R.id.tvSinAlertas)
        containerPacientes        = view.findViewById(R.id.containerPacientes)
        tvSinPacientes            = view.findViewById(R.id.tvSinPacientes)
        tvVerTodasAlertas         = view.findViewById(R.id.tvVerTodasAlertas)
        tvVerTodosPacientes       = view.findViewById(R.id.tvVerTodosPacientes)
        cardSolicitudesPendientes = view.findViewById(R.id.cardSolicitudesPendientes)
        tvContadorSolicitudes     = view.findViewById(R.id.tvContadorSolicitudes)

        tvVerTodasAlertas.setOnClickListener {
            (requireActivity() as HospitalMainActivity).navegarA("alertas")
        }
        tvVerTodosPacientes.setOnClickListener {
            (requireActivity() as HospitalMainActivity).navegarA("pacientes")
        }
        cardSolicitudesPendientes.setOnClickListener {
            startActivity(Intent(requireContext(), SolicitudesPendientesActivity::class.java))
        }

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            SyncNotificacionesManager.revisarYNotificar(requireContext())
        }
        val hid = hospitalId ?: return
        cargarContadorSolicitudes(hid)
    }

    private fun cargarContadorSolicitudes(hid: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            AdminController.obtenerSolicitudesPendientes(hid)
                .onSuccess { solicitudes ->
                    val count = solicitudes.size
                    if (count > 0) {
                        tvContadorSolicitudes.text = count.toString()
                        tvContadorSolicitudes.visibility = View.VISIBLE
                    } else {
                        tvContadorSolicitudes.visibility = View.GONE
                    }
                }
        }
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvSaludo.text = "Bienvenido, ${perfil.nombre}"
                    val hid = perfil.hospitalId ?: return@onSuccess
                    hospitalId = hid

                    PacienteController.obtenerNombreHospital(hid)
                        .onSuccess { nombre -> tvNombreHospital.text = nombre }

                    cargarContadorSolicitudes(hid)

                    PacienteController.obtenerPacientesHospital(hid)
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
                            Toast.makeText(requireContext(), "Error al cargar pacientes", Toast.LENGTH_SHORT).show()
                        }
                }
                .onFailure {
                    Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show()
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
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_alerta_dashboard, containerAlertas, false)
            itemView.findViewById<TextView>(R.id.tvAlertaTitulo).text = alerta.titulo
            itemView.findViewById<TextView>(R.id.tvAlertaMensaje).text = alerta.mensaje
            itemView.findViewById<TextView>(R.id.tvAlertaTipo).text = alerta.tipo
            containerAlertas.addView(itemView)
        }
    }

    private fun mostrarPacientesRecientes(pacientes: List<Paciente>) {
        if (pacientes.isEmpty()) {
            tvSinPacientes.visibility = View.VISIBLE
            return
        }
        tvSinPacientes.visibility = View.GONE
        for (paciente in pacientes) {
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_paciente_dashboard, containerPacientes, false)
            itemView.findViewById<TextView>(R.id.tvPacienteIniciales).text = paciente.id.take(2).uppercase()
            itemView.findViewById<TextView>(R.id.tvPacienteId).text = "Pac. ${paciente.id.take(8)}"
            itemView.findViewById<TextView>(R.id.tvPacienteFarmaco).text = paciente.radiofarmaco
            val dias = calcularDiasRestantes(paciente)
            itemView.findViewById<TextView>(R.id.tvPacienteDias).text =
                if (dias > 0) "${dias}d restantes" else "Alta completada"
            itemView.setOnClickListener {
                val intent = Intent(requireContext(), DetallePacienteActivity::class.java)
                intent.putExtra("paciente_id", paciente.id)
                startActivity(intent)
            }
            containerPacientes.addView(itemView)
        }
    }

    private fun calcularDiasRestantes(paciente: Paciente): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaAlta = sdf.parse(paciente.fechaAlta) ?: return 0L
            val cal = Calendar.getInstance()
            cal.time = fechaAlta
            cal.add(Calendar.DAY_OF_YEAR, paciente.diasAislamiento)
            val diff = cal.time.time - Calendar.getInstance().time.time
            TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
        } catch (e: Exception) {
            0L
        }
    }
}
