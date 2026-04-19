package com.example.theratrackhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Alerta
import kotlinx.coroutines.launch

class AlertasHospitalActivity : BaseHospitalActivity() {

    private lateinit var tvSubtitulo: TextView
    private lateinit var chipTodas: TextView
    private lateinit var chipNoLeidas: TextView
    private lateinit var containerLista: LinearLayout
    private lateinit var tvSinAlertas: TextView

    private var todasAlertas: List<Alerta> = emptyList()
    private var mostrandoSoloNoLeidas = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas_hospital)

        tvSubtitulo    = findViewById(R.id.tvSubtituloAlertas)
        chipTodas      = findViewById(R.id.chipTodas)
        chipNoLeidas   = findViewById(R.id.chipNoLeidas)
        containerLista = findViewById(R.id.containerListaAlertas)
        tvSinAlertas   = findViewById(R.id.tvSinAlertas)

        configurarNavegacion(R.id.nav_alertas)

        chipTodas.setOnClickListener {
            mostrandoSoloNoLeidas = false
            chipTodas.setBackgroundResource(R.drawable.bg_chip_active)
            chipTodas.setTextColor(getColor(R.color.white))
            chipNoLeidas.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipNoLeidas.setTextColor(getColor(R.color.colorTextSecondary))
            mostrarAlertas(todasAlertas)
        }

        chipNoLeidas.setOnClickListener {
            mostrandoSoloNoLeidas = true
            chipNoLeidas.setBackgroundResource(R.drawable.bg_chip_active)
            chipNoLeidas.setTextColor(getColor(R.color.white))
            chipTodas.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipTodas.setTextColor(getColor(R.color.colorTextSecondary))
            mostrarAlertas(todasAlertas.filter { !it.leida })
        }

        cargarAlertas()
    }

    private fun cargarAlertas() {
        lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    val hospitalId = perfil.hospitalId ?: return@onSuccess
                    PacienteController.obtenerTodasAlertasHospital(hospitalId)
                        .onSuccess { alertas ->
                            todasAlertas = alertas
                            val noLeidas = alertas.count { !it.leida }
                            tvSubtitulo.text = "$noLeidas alertas sin leer · ${alertas.size} total"
                            mostrarAlertas(alertas)
                        }
                        .onFailure {
                            tvSubtitulo.text = "Error al cargar alertas"
                            tvSinAlertas.visibility = View.VISIBLE
                        }
                }
        }
    }

    private fun mostrarAlertas(alertas: List<Alerta>) {
        containerLista.removeAllViews()
        if (alertas.isEmpty()) {
            tvSinAlertas.visibility = View.VISIBLE
            return
        }
        tvSinAlertas.visibility = View.GONE
        for (alerta in alertas) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_alerta_dashboard, containerLista, false)
            view.findViewById<TextView>(R.id.tvAlertaTitulo).text = alerta.titulo
            view.findViewById<TextView>(R.id.tvAlertaMensaje).text = alerta.mensaje
            view.findViewById<TextView>(R.id.tvAlertaTipo).text = alerta.tipo
            if (alerta.leida) view.alpha = 0.5f
            containerLista.addView(view)
        }
    }
}
