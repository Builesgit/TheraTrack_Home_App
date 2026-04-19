package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Paciente
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardPacienteActivity : AppCompatActivity() {

    enum class EstadoSeguridad { SEGURO, PRECAUCION, ALERTA }

    private lateinit var cardEstado: CardView
    private lateinit var iconEstadoContainer: FrameLayout
    private lateinit var ivIconEstado: ImageView
    private lateinit var tvBadgeEstado: TextView
    private lateinit var tvTituloEstado: TextView
    private lateinit var tvDescripcionEstado: TextView
    private lateinit var tvRecomendacionActual: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var tvNombrePaciente: TextView
    private lateinit var btnVerHistorial: MaterialButton
    private lateinit var btnSintomas: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_paciente)

        enlazarVistas()
        configurarBotones()
        configurarBottomNav()
        cargarDatosPaciente()
    }

    private fun enlazarVistas() {
        cardEstado           = findViewById(R.id.cardEstado)
        iconEstadoContainer  = findViewById(R.id.iconEstadoContainer)
        ivIconEstado         = findViewById(R.id.ivIconEstado)
        tvBadgeEstado        = findViewById(R.id.tvBadgeEstado)
        tvTituloEstado       = findViewById(R.id.tvTituloEstado)
        tvDescripcionEstado  = findViewById(R.id.tvDescripcionEstado)
        tvRecomendacionActual= findViewById(R.id.tvRecomendacionActual)
        tvUltimaActualizacion= findViewById(R.id.tvUltimaActualizacion)
        tvNombrePaciente     = findViewById(R.id.tvNombrePaciente)
        btnVerHistorial      = findViewById(R.id.btnVerHistorial)
        btnSintomas          = findViewById(R.id.btnSintomas)
    }

    private fun configurarBotones() {
        btnVerHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
        btnSintomas.setOnClickListener {
            startActivity(Intent(this, RegistroDiarioActivity::class.java))
        }
    }

    private fun configurarBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.menu.findItem(R.id.nav_inicio)?.isChecked = true
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio   -> true
                R.id.nav_historial-> { startActivity(Intent(this, HistorialActivity::class.java)); true }
                R.id.nav_sintomas -> { startActivity(Intent(this, RegistroDiarioActivity::class.java)); true }
                R.id.nav_perfil   -> { startActivity(Intent(this, PerfilPacienteActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun cargarDatosPaciente() {
        lifecycleScope.launch {
            // Cargar perfil para el nombre
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvNombrePaciente.text = perfil.nombre
                }

            // Cargar datos clínicos para calcular estado
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    val diasRestantes = calcularDiasRestantes(paciente)
                    val estado = determinarEstado(diasRestantes)
                    aplicarEstado(estado, diasRestantes)

                    // Cargar recomendaciones del hospital
                    PacienteController.obtenerRecomendaciones(paciente.hospitalId)
                        .onSuccess { recs ->
                            if (recs.isNotEmpty()) {
                                tvRecomendacionActual.text = recs.first().contenido
                            }
                        }
                }
                .onFailure {
                    // Sin datos clínicos aún → mostrar estado SEGURO por defecto
                    aplicarEstado(EstadoSeguridad.SEGURO, 0)
                }
        }
    }

    private fun calcularDiasRestantes(paciente: Paciente): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val alta = sdf.parse(paciente.fechaAlta) ?: return 0L
            val cal = Calendar.getInstance()
            cal.time = alta
            cal.add(Calendar.DAY_OF_MONTH, paciente.diasAislamiento)
            val finAislamiento = cal.time
            val hoy = Calendar.getInstance().time
            val diff = finAislamiento.time - hoy.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            0L
        }
    }

    private fun determinarEstado(diasRestantes: Long): EstadoSeguridad = when {
        diasRestantes > 5 -> EstadoSeguridad.ALERTA
        diasRestantes in 2..5 -> EstadoSeguridad.PRECAUCION
        else -> EstadoSeguridad.SEGURO
    }

    private fun aplicarEstado(estado: EstadoSeguridad, diasRestantes: Long) {
        when (estado) {
            EstadoSeguridad.SEGURO -> {
                cardEstado.setCardBackgroundColor(getColor(R.color.colorGreenBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_green_solid)
                ivIconEstado.setImageResource(R.drawable.ic_check_white)
                tvBadgeEstado.text = "SEGURO"
                tvBadgeEstado.setTextColor(getColor(R.color.colorGreenSafe))
                tvTituloEstado.text = "Estado: Seguro"
                tvDescripcionEstado.text = "Nivel bajo de radiación detectado"
                tvDescripcionEstado.setTextColor(getColor(R.color.colorGreenSafe))
            }
            EstadoSeguridad.PRECAUCION -> {
                cardEstado.setCardBackgroundColor(getColor(R.color.colorOrangeBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_orange_solid)
                ivIconEstado.setImageResource(R.drawable.ic_warning_white)
                tvBadgeEstado.text = "PRECAUCIÓN"
                tvBadgeEstado.setTextColor(getColor(R.color.colorOrangeCaution))
                tvTituloEstado.text = "Estado: Precaución"
                tvDescripcionEstado.text = "Aumente distancia y limite el tiempo cerca de convivientes."
                tvDescripcionEstado.setTextColor(getColor(R.color.colorOrangeCaution))
            }
            EstadoSeguridad.ALERTA -> {
                cardEstado.setCardBackgroundColor(getColor(R.color.colorRedBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_red_solid)
                ivIconEstado.setImageResource(R.drawable.ic_alert_white)
                tvBadgeEstado.text = "ALERTA"
                tvBadgeEstado.setTextColor(getColor(R.color.colorRedAlert))
                tvTituloEstado.text = "Estado: Alerta"
                tvDescripcionEstado.text = "Se ha detectado un nivel de radiación fuera de rangos normales."
                tvDescripcionEstado.setTextColor(getColor(R.color.colorRedAlert))
            }
        }
        val hoy = java.text.DateFormat.getDateInstance().format(java.util.Date())
        tvUltimaActualizacion.text = "Última actualización: $hoy"
    }
}
