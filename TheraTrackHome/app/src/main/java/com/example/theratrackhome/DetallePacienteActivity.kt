package com.example.theratrackhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DetallePacienteActivity : AppCompatActivity() {

    private lateinit var btnVolver: ImageView
    private lateinit var tvDetalleTitulo: TextView
    private lateinit var tvDetalleSubtitulo: TextView
    private lateinit var tvEstadoBadge: TextView
    private lateinit var tvIniciales: TextView
    private lateinit var tvPacienteId: TextView
    private lateinit var tvRadiofarmaco: TextView
    private lateinit var tvDiasRestantesNum: TextView
    private lateinit var tvDosis: TextView
    private lateinit var tvFechaTratamiento: TextView
    private lateinit var tvFechaAlta: TextView
    private lateinit var tvDiasAislamiento: TextView
    private lateinit var tvNotas: TextView
    private lateinit var containerAlertas: LinearLayout
    private lateinit var tvSinAlertas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_paciente)

        btnVolver           = findViewById(R.id.btnVolver)
        tvDetalleTitulo     = findViewById(R.id.tvDetalleTitulo)
        tvDetalleSubtitulo  = findViewById(R.id.tvDetalleSubtitulo)
        tvEstadoBadge       = findViewById(R.id.tvEstadoBadge)
        tvIniciales         = findViewById(R.id.tvIniciales)
        tvPacienteId        = findViewById(R.id.tvPacienteId)
        tvRadiofarmaco      = findViewById(R.id.tvRadiofarmaco)
        tvDiasRestantesNum  = findViewById(R.id.tvDiasRestantesNum)
        tvDosis             = findViewById(R.id.tvDosis)
        tvFechaTratamiento  = findViewById(R.id.tvFechaTratamiento)
        tvFechaAlta         = findViewById(R.id.tvFechaAlta)
        tvDiasAislamiento   = findViewById(R.id.tvDiasAislamiento)
        tvNotas             = findViewById(R.id.tvNotas)
        containerAlertas    = findViewById(R.id.containerAlertasPaciente)
        tvSinAlertas        = findViewById(R.id.tvSinAlertasPaciente)

        btnVolver.setOnClickListener { finish() }

        val pacienteId = intent.getStringExtra("paciente_id") ?: run { finish(); return }
        cargarDetalle(pacienteId)
    }

    private fun cargarDetalle(pacienteId: String) {
        lifecycleScope.launch {
            PacienteController.obtenerPacientePorId(pacienteId)
                .onSuccess { paciente ->
                    mostrarPaciente(paciente)
                    PacienteController.obtenerAlertasPaciente(pacienteId)
                        .onSuccess { alertas -> mostrarAlertas(alertas) }
                        .onFailure { tvSinAlertas.visibility = View.VISIBLE }
                }
                .onFailure {
                    tvDetalleSubtitulo.text = "Error al cargar datos"
                }
        }
    }

    private fun mostrarPaciente(p: Paciente) {
        val iniciales = p.id.take(2).uppercase()
        tvIniciales.text = iniciales
        tvPacienteId.text = "Paciente ${p.id.take(8)}"
        tvDetalleSubtitulo.text = p.id
        tvRadiofarmaco.text = p.radiofarmaco
        tvDosis.text = if (p.dosisMbq != null) "${p.dosisMbq} MBq" else "No registrada"
        tvFechaTratamiento.text = p.fechaTratamiento
        tvFechaAlta.text = p.fechaAlta
        tvDiasAislamiento.text = "${p.diasAislamiento} días"
<<<<<<< HEAD
        tvNotas.text = p.notasClinicas?.takeIf { it.isNotBlank() } ?: "Sin notas registradas"
=======
        tvNotas.text = p.notasClinivas?.takeIf { it.isNotBlank() } ?: "Sin notas registradas"
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31

        val dias = calcularDiasRestantes(p)
        tvDiasRestantesNum.text = dias.toString()
        if (dias <= 0) {
            tvEstadoBadge.text = "Alta completada"
            tvEstadoBadge.setTextColor(getColor(R.color.colorGreenSafe))
            tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_green)
            tvDiasRestantesNum.setTextColor(getColor(R.color.colorGreenSafe))
        }
    }

    private fun mostrarAlertas(alertas: List<Alerta>) {
        if (alertas.isEmpty()) {
            tvSinAlertas.visibility = View.VISIBLE
            return
        }
        tvSinAlertas.visibility = View.GONE
        for (alerta in alertas.take(5)) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_alerta_dashboard, containerAlertas, false)
            view.findViewById<TextView>(R.id.tvAlertaTitulo).text = alerta.titulo
            view.findViewById<TextView>(R.id.tvAlertaMensaje).text = alerta.mensaje
            view.findViewById<TextView>(R.id.tvAlertaTipo).text = alerta.tipo
            containerAlertas.addView(view)
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
