package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.controller.PacienteController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PerfilPacienteActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvRadiofarmaco: TextView
    private lateinit var tvFechaTratamiento: TextView
    private lateinit var tvHospital: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_paciente)

        enlazarVistas()
        configurarBotones()
        configurarBottomNav()
        cargarDatos()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun enlazarVistas() {
        tvNombre           = findViewById(R.id.tvNombre)
        tvSubtitulo        = findViewById(R.id.tvSubtitulo)
        tvRadiofarmaco     = findViewById(R.id.tvRadiofarmaco)
        tvFechaTratamiento = findViewById(R.id.tvFechaTratamiento)
        tvHospital         = findViewById(R.id.tvHospital)
    }

    private fun configurarBotones() {
        findViewById<MaterialButton>(R.id.btnVerRecomendaciones).setOnClickListener {
            startActivity(Intent(this, AlertasActivity::class.java))
        }

        // Ajustes — filas informativas (sin funcionalidad aún)
        findViewById<LinearLayout>(R.id.rowNotificaciones).setOnClickListener {
            Toast.makeText(this, "Notificaciones — próximamente", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.rowIdioma).setOnClickListener {
            Toast.makeText(this, "Idioma — próximamente", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.rowContacto).setOnClickListener {
            startActivity(Intent(this, AsistenciaActivity::class.java))
        }

        // Cerrar sesión con confirmación
        findViewById<LinearLayout>(R.id.rowCerrarSesion).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ -> cerrarSesion() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            // Perfil (nombre)
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvNombre.text = perfil.nombre
                    tvSubtitulo.text = "Paciente • ${perfil.rol.replaceFirstChar { it.uppercase() }}"
                }

            // Datos clínicos
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    tvRadiofarmaco.text = paciente.radiofarmaco

                    val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaFormateada = try {
                        sdfOut.format(sdfIn.parse(paciente.fechaTratamiento)!!)
                    } catch (e: Exception) { paciente.fechaTratamiento }
                    tvFechaTratamiento.text = fechaFormateada

                    // Nombre del hospital
                    PacienteController.obtenerNombreHospital(paciente.hospitalId)
                        .onSuccess { nombre -> tvHospital.text = nombre }
                        .onFailure   { tvHospital.text = "—" }
                }
                .onFailure {
                    tvRadiofarmaco.text     = "Sin datos"
                    tvFechaTratamiento.text = "—"
                    tvHospital.text         = "—"
                }
        }
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            AuthController.logout()
                .onSuccess {
                    val intent = Intent(this@PerfilPacienteActivity, PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .onFailure {
                    Toast.makeText(this@PerfilPacienteActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun configurarBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.menu.findItem(R.id.nav_perfil)?.isChecked = true
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio   -> { startActivity(Intent(this, DashboardPacienteActivity::class.java)); finish(); true }
                R.id.nav_historial-> { startActivity(Intent(this, HistorialActivity::class.java)); finish(); true }
                R.id.nav_sintomas -> { startActivity(Intent(this, RegistroDiarioActivity::class.java)); finish(); true }
                R.id.nav_perfil   -> true
                else -> false
            }
        }
    }
}
