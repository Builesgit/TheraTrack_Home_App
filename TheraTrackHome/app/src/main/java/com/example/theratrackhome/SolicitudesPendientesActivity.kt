package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AdminController
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Perfil
import kotlinx.coroutines.launch

class SolicitudesPendientesActivity : AppCompatActivity() {

    private lateinit var btnVolver: ImageView
    private lateinit var tvSubtitulo: TextView
    private lateinit var chipPacientes: TextView
    private lateinit var chipProfesionales: TextView
    private lateinit var containerSolicitudes: LinearLayout
    private lateinit var tvSinSolicitudes: TextView

    private var todasSolicitudes: List<Perfil> = emptyList()
    private var mostrandoPacientes = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes_pendientes)

        btnVolver           = findViewById(R.id.btnVolverSolicitudes)
        tvSubtitulo         = findViewById(R.id.tvSubtituloSolicitudes)
        chipPacientes       = findViewById(R.id.chipPacientes)
        chipProfesionales   = findViewById(R.id.chipProfesionales)
        containerSolicitudes = findViewById(R.id.containerSolicitudes)
        tvSinSolicitudes    = findViewById(R.id.tvSinSolicitudes)

        btnVolver.setOnClickListener { finish() }

        chipPacientes.setOnClickListener {
            mostrandoPacientes = true
            chipPacientes.setBackgroundResource(R.drawable.bg_chip_active)
            chipPacientes.setTextColor(getColor(R.color.white))
            chipProfesionales.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipProfesionales.setTextColor(getColor(R.color.colorTextSecondary))
            mostrarSolicitudes(todasSolicitudes.filter { it.rol == "paciente" })
        }

        chipProfesionales.setOnClickListener {
            mostrandoPacientes = false
            chipProfesionales.setBackgroundResource(R.drawable.bg_chip_active)
            chipProfesionales.setTextColor(getColor(R.color.white))
            chipPacientes.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipPacientes.setTextColor(getColor(R.color.colorTextSecondary))
            mostrarSolicitudes(todasSolicitudes.filter { it.rol == "profesional" })
        }
    }

    override fun onResume() {
        super.onResume()
        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {
        lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    val hospitalId = perfil.hospitalId ?: return@onSuccess
                    AdminController.obtenerSolicitudesPendientes(hospitalId)
                        .onSuccess { solicitudes ->
                            todasSolicitudes = solicitudes
                            tvSubtitulo.text = "${solicitudes.size} solicitudes pendientes"
                            val filtradas = if (mostrandoPacientes)
                                solicitudes.filter { it.rol == "paciente" }
                            else
                                solicitudes.filter { it.rol == "profesional" }
                            mostrarSolicitudes(filtradas)
                        }
                        .onFailure {
                            tvSubtitulo.text = "Error al cargar solicitudes"
                            tvSinSolicitudes.visibility = View.VISIBLE
                        }
                }
                .onFailure {
                    tvSubtitulo.text = "Error al cargar perfil"
                }
        }
    }

    private fun mostrarSolicitudes(solicitudes: List<Perfil>) {
        containerSolicitudes.removeAllViews()
        if (solicitudes.isEmpty()) {
            tvSinSolicitudes.visibility = View.VISIBLE
            return
        }
        tvSinSolicitudes.visibility = View.GONE
        for (solicitud in solicitudes) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_solicitud, containerSolicitudes, false)

            val iniciales = solicitud.nombre.take(2).uppercase()
            view.findViewById<TextView>(R.id.tvSolicitudIniciales).text = iniciales
            view.findViewById<TextView>(R.id.tvSolicitudNombre).text = solicitud.nombre
            view.findViewById<TextView>(R.id.tvSolicitudEmail).text = solicitud.email ?: ""

            val tvRol = view.findViewById<TextView>(R.id.tvSolicitudRol)
            tvRol.text = if (solicitud.rol == "paciente") "Paciente" else "Profesional"

            val tvCargo = view.findViewById<TextView>(R.id.tvSolicitudCargo)
            if (!solicitud.cargo.isNullOrBlank()) {
                tvCargo.text = solicitud.cargo
                tvCargo.visibility = View.VISIBLE
            }

            val tvMotivo = view.findViewById<TextView>(R.id.tvSolicitudMotivo)
            if (!solicitud.motivoSolicitud.isNullOrBlank()) {
                tvMotivo.text = solicitud.motivoSolicitud
                tvMotivo.visibility = View.VISIBLE
            }

            view.findViewById<View>(R.id.btnAprobarSolicitud).setOnClickListener {
                if (solicitud.rol == "paciente") {
                    val intent = Intent(this, CrearFichaPacienteActivity::class.java)
                    intent.putExtra("perfil_id", solicitud.id)
                    intent.putExtra("perfil_nombre", solicitud.nombre)
                    startActivity(intent)
                } else {
                    aprobarProfesional(solicitud.id)
                }
            }

            view.findViewById<View>(R.id.btnRechazarSolicitud).setOnClickListener {
                rechazarSolicitud(solicitud.id)
            }

            containerSolicitudes.addView(view)
        }
    }

    private fun aprobarProfesional(perfilId: String) {
        lifecycleScope.launch {
            AdminController.aprobarSolicitud(perfilId)
                .onSuccess {
                    Toast.makeText(this@SolicitudesPendientesActivity, "Profesional aprobado", Toast.LENGTH_SHORT).show()
                    cargarSolicitudes()
                }
                .onFailure {
                    Toast.makeText(this@SolicitudesPendientesActivity, "Error al aprobar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun rechazarSolicitud(perfilId: String) {
        lifecycleScope.launch {
            AdminController.rechazarSolicitud(perfilId)
                .onSuccess {
                    Toast.makeText(this@SolicitudesPendientesActivity, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    cargarSolicitudes()
                }
                .onFailure {
                    Toast.makeText(this@SolicitudesPendientesActivity, "Error al rechazar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
