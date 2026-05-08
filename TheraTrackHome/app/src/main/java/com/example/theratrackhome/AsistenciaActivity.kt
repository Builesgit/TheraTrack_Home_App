package com.example.theratrackhome

<<<<<<< HEAD
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class AsistenciaActivity : AppCompatActivity() {

    private var telefonoHospital: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia)
        findViewById<ImageView>(R.id.btnBackAsistencia).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvVersionApp).text = "Versión ${obtenerVersionName()}"
        configurarFaq()
        configurarContacto()
        configurarBottomNav()
        cargarTelefonoHospital()
    }

    private fun configurarFaq() {
        val container = findViewById<LinearLayout>(R.id.containerFaq)
        container.addView(titulo("Preguntas frecuentes"))
        val items = listOf(
            "¿Por qué necesito reportar síntomas a diario?" to "El reporte diario permite a tu equipo médico detectar a tiempo cualquier reacción adversa al tratamiento radiofarmacológico.",
            "¿Cuánto duran los días de aislamiento?" to "Depende del radiofármaco administrado. Tu hospital ha definido el tiempo en tu ficha clínica.",
            "¿Puedo recibir visitas durante el aislamiento?" to "Consulta con tu médico. Generalmente se recomienda evitar contacto cercano con embarazadas y niños.",
            "¿Qué hago si olvido reportar un día?" to "Reporta cuanto antes. La app permite registrar el día actual; los días pasados quedarán como no registrados.",
            "¿Mis datos están seguros?" to "Tus datos se almacenan cifrados y solo tu equipo médico autorizado puede acceder a ellos."
        )
        items.forEach { (pregunta, respuesta) ->
            val q = TextView(this).apply {
                text = pregunta
                textSize = 14f
                setTextColor(getColor(R.color.colorTextPrimary))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 16, 0, 8)
            }
            val r = TextView(this).apply {
                text = respuesta
                textSize = 13f
                setTextColor(getColor(R.color.colorTextSecondary))
                visibility = View.GONE
            }
            q.setOnClickListener { r.visibility = if (r.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
            container.addView(q)
            container.addView(r)
        }
    }

    private fun titulo(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 16f
        setTextColor(getColor(R.color.colorTextPrimary))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun configurarContacto() {
        findViewById<TextView>(R.id.rowContactarHospital).setOnClickListener {
            val tel = telefonoHospital
            if (tel.isNullOrBlank()) Toast.makeText(this, "Teléfono no disponible", Toast.LENGTH_SHORT).show()
            else abrirIntentSeguro(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")), "No hay una app de teléfono disponible")
        }
        findViewById<TextView>(R.id.rowSoporteTecnico).setOnClickListener {
            abrirIntentSeguro(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@theratrack.com")), "No hay una app de correo disponible")
        }
        findViewById<TextView>(R.id.rowPrivacidad).setOnClickListener { abrirUrl("https://theratrack.com/privacidad") }
        findViewById<TextView>(R.id.rowTerminos).setOnClickListener { abrirUrl("https://theratrack.com/terminos") }
    }

    private fun cargarTelefonoHospital() {
        lifecycleScope.launch {
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    PacienteController.obtenerHospitalPorId(paciente.hospitalId).onSuccess { hospital ->
                        telefonoHospital = hospital.telefono
                        findViewById<TextView>(R.id.rowContactarHospital).text =
                            "Contactar mi hospital: ${hospital.telefono ?: "Teléfono no disponible"}"
                    }.onFailure {
                        findViewById<TextView>(R.id.rowContactarHospital).text = "Contactar mi hospital: Teléfono no disponible"
                    }
                }
                .onFailure {
                    findViewById<TextView>(R.id.rowContactarHospital).text = "Contactar mi hospital: Teléfono no disponible"
                }
        }
    }

    private fun abrirUrl(url: String) {
        abrirIntentSeguro(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "No hay una app para abrir el enlace")
    }

    private fun abrirIntentSeguro(intent: Intent, mensajeError: String) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerVersionName(): String {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            } ?: "1.0"
        }.getOrDefault("1.0")
    }

    private fun configurarBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "dashboard")
                    }
                    startActivity(i); finish(); true
                }
                R.id.nav_historial -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "historial")
                    }
                    startActivity(i); finish(); true
                }
                R.id.nav_sintomas -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "sintomas")
                    }
                    startActivity(i); finish(); true
                }
                R.id.nav_perfil -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "perfil")
                    }
                    startActivity(i); finish(); true
                }
                else -> false
            }
        }
=======
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AsistenciaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia)
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    }
}
