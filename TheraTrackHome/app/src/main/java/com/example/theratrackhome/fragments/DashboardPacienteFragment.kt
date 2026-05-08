package com.example.theratrackhome.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.AlertasActivity
import com.example.theratrackhome.AsistenciaActivity
import com.example.theratrackhome.PacienteMainActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.SyncNotificacionesManager
import com.example.theratrackhome.model.Paciente
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardPacienteFragment : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_dashboard_paciente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE

        cardEstado            = view.findViewById(R.id.cardEstado)
        iconEstadoContainer   = view.findViewById(R.id.iconEstadoContainer)
        ivIconEstado          = view.findViewById(R.id.ivIconEstado)
        tvBadgeEstado         = view.findViewById(R.id.tvBadgeEstado)
        tvTituloEstado        = view.findViewById(R.id.tvTituloEstado)
        tvDescripcionEstado   = view.findViewById(R.id.tvDescripcionEstado)
        tvRecomendacionActual = view.findViewById(R.id.tvRecomendacionActual)
        tvUltimaActualizacion = view.findViewById(R.id.tvUltimaActualizacion)
        tvNombrePaciente      = view.findViewById(R.id.tvNombrePaciente)
        btnVerHistorial       = view.findViewById(R.id.btnVerHistorial)
        btnSintomas           = view.findViewById(R.id.btnSintomas)

        configurarBotones(view)
        cargarDatosPaciente()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            SyncNotificacionesManager.revisarYNotificar(requireContext())
        }
    }

    private fun configurarBotones(view: View) {
        btnVerHistorial.setOnClickListener {
            (requireActivity() as PacienteMainActivity).navegarA("historial")
        }
        btnSintomas.setOnClickListener {
            (requireActivity() as PacienteMainActivity).navegarA("sintomas")
        }
        view.findViewById<ImageView>(R.id.ivBell).setOnClickListener {
            startActivity(Intent(requireContext(), AlertasActivity::class.java))
        }
        view.findViewById<FrameLayout>(R.id.avatarContainer).setOnClickListener {
            (requireActivity() as PacienteMainActivity).navegarA("perfil")
        }
        view.findViewById<CardView>(R.id.cardRec1).setOnClickListener {
            startActivity(Intent(requireContext(), AsistenciaActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardRec2).setOnClickListener {
            startActivity(Intent(requireContext(), AsistenciaActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardRec3).setOnClickListener {
            startActivity(Intent(requireContext(), AsistenciaActivity::class.java))
        }
    }

    private fun cargarDatosPaciente() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvNombrePaciente.text = perfil.nombre
                }

            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    val diasRestantes = calcularDiasRestantes(paciente)
                    val estado = determinarEstado(diasRestantes)
                    aplicarEstado(estado, diasRestantes)

                    PacienteController.obtenerRecomendaciones(paciente.hospitalId)
                        .onSuccess { recs ->
                            if (recs.isNotEmpty()) {
                                tvRecomendacionActual.text = recs.first().descripcion
                            }
                        }
                }
                .onFailure {
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
                cardEstado.setCardBackgroundColor(requireContext().getColor(R.color.colorGreenBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_green_solid)
                ivIconEstado.setImageResource(R.drawable.ic_check_white)
                tvBadgeEstado.text = "SEGURO"
                tvBadgeEstado.setTextColor(requireContext().getColor(R.color.colorGreenSafe))
                tvTituloEstado.text = "Estado: Seguro"
                tvDescripcionEstado.text = "Nivel bajo de radiación detectado"
                tvDescripcionEstado.setTextColor(requireContext().getColor(R.color.colorGreenSafe))
            }
            EstadoSeguridad.PRECAUCION -> {
                cardEstado.setCardBackgroundColor(requireContext().getColor(R.color.colorOrangeBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_orange_solid)
                ivIconEstado.setImageResource(R.drawable.ic_warning_white)
                tvBadgeEstado.text = "PRECAUCIÓN"
                tvBadgeEstado.setTextColor(requireContext().getColor(R.color.colorOrangeCaution))
                tvTituloEstado.text = "Estado: Precaución"
                tvDescripcionEstado.text = "Aumente distancia y limite el tiempo cerca de convivientes."
                tvDescripcionEstado.setTextColor(requireContext().getColor(R.color.colorOrangeCaution))
            }
            EstadoSeguridad.ALERTA -> {
                cardEstado.setCardBackgroundColor(requireContext().getColor(R.color.colorRedBg))
                iconEstadoContainer.setBackgroundResource(R.drawable.bg_circle_red_solid)
                ivIconEstado.setImageResource(R.drawable.ic_alert_white)
                tvBadgeEstado.text = "ALERTA"
                tvBadgeEstado.setTextColor(requireContext().getColor(R.color.colorRedAlert))
                tvTituloEstado.text = "Estado: Alerta"
                tvDescripcionEstado.text = "Se ha detectado un nivel de radiación fuera de rangos normales."
                tvDescripcionEstado.setTextColor(requireContext().getColor(R.color.colorRedAlert))
            }
        }
        val hoy = java.text.DateFormat.getDateInstance().format(java.util.Date())
        tvUltimaActualizacion.text = "Última actualización: $hoy"
    }
}
