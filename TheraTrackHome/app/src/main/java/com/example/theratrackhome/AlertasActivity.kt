package com.example.theratrackhome

import android.os.Bundle
<<<<<<< HEAD
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Recomendacion
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class AlertasActivity : AppCompatActivity() {

    private lateinit var btnVolver: ImageView
    private lateinit var tvSubtitulo: TextView
    private lateinit var chipMisAlertas: TextView
    private lateinit var chipRecomendaciones: TextView
    private lateinit var containerMisAlertas: LinearLayout
    private lateinit var containerRecomendaciones: LinearLayout
    private lateinit var tvSinElementos: TextView

    private var alertas: List<Alerta> = emptyList()
    private var recomendaciones: List<Recomendacion> = emptyList()
    private var mostrandoAlertas = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        btnVolver               = findViewById(R.id.btnVolverAlertas)
        tvSubtitulo             = findViewById(R.id.tvSubtituloAlertasPaciente)
        chipMisAlertas          = findViewById(R.id.chipMisAlertas)
        chipRecomendaciones     = findViewById(R.id.chipRecomendaciones)
        containerMisAlertas     = findViewById(R.id.containerMisAlertas)
        containerRecomendaciones = findViewById(R.id.containerRecomendaciones)
        tvSinElementos          = findViewById(R.id.tvSinElementos)

        btnVolver.setOnClickListener { finish() }

        chipMisAlertas.setOnClickListener {
            mostrandoAlertas = true
            activarChip(chipMisAlertas, chipRecomendaciones)
            containerMisAlertas.visibility = View.VISIBLE
            containerRecomendaciones.visibility = View.GONE
            actualizarSinElementos(alertas.isEmpty())
        }

        chipRecomendaciones.setOnClickListener {
            mostrandoAlertas = false
            activarChip(chipRecomendaciones, chipMisAlertas)
            containerRecomendaciones.visibility = View.VISIBLE
            containerMisAlertas.visibility = View.GONE
            actualizarSinElementos(recomendaciones.isEmpty())
        }

        cargarDatos()
    }

    private fun activarChip(activo: TextView, inactivo: TextView) {
        activo.setBackgroundResource(R.drawable.bg_chip_active)
        activo.setTextColor(getColor(R.color.white))
        inactivo.setBackgroundResource(R.drawable.bg_chip_inactive)
        inactivo.setTextColor(getColor(R.color.colorTextSecondary))
    }

    private fun actualizarSinElementos(vacio: Boolean) {
        tvSinElementos.visibility = if (vacio) View.VISIBLE else View.GONE
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    val pacienteId = paciente.id
                    val hospitalId = paciente.hospitalId

                    PacienteController.obtenerAlertasPaciente(pacienteId)
                        .onSuccess { lista ->
                            alertas = lista
                            val noLeidas = lista.count { !it.leida }
                            tvSubtitulo.text = "$noLeidas sin leer · ${lista.size} total"
                            mostrarAlertas(lista)
                            if (mostrandoAlertas) actualizarSinElementos(lista.isEmpty())
                        }
                        .onFailure {
                            tvSubtitulo.text = "Error al cargar alertas"
                        }

                    PacienteController.obtenerRecomendacionesPaciente(hospitalId)
                        .onSuccess { lista ->
                            recomendaciones = lista
                            mostrarRecomendaciones(lista)
                        }
                }
                .onFailure {
                    tvSubtitulo.text = "Sin datos clínicos disponibles"
                    actualizarSinElementos(true)
                    Toast.makeText(this@AlertasActivity, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarAlertas(lista: List<Alerta>) {
        containerMisAlertas.removeAllViews()
        for (alerta in lista) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_alerta_dashboard, containerMisAlertas, false)
            view.findViewById<TextView>(R.id.tvAlertaTitulo).text = alerta.titulo
            view.findViewById<TextView>(R.id.tvAlertaMensaje).text = alerta.mensaje
            view.findViewById<TextView>(R.id.tvAlertaTipo).text = alerta.tipo
            if (alerta.leida) view.alpha = 0.55f
            containerMisAlertas.addView(view)
        }
    }

    private fun mostrarRecomendaciones(lista: List<Recomendacion>) {
        containerRecomendaciones.removeAllViews()
        for (rec in lista) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_recomendacion_paciente, containerRecomendaciones, false)

            view.findViewById<TextView>(R.id.tvRecTitulo).text = rec.titulo
            view.findViewById<TextView>(R.id.tvRecDescripcion).text = rec.descripcion
            view.findViewById<TextView>(R.id.tvRecTipo).text = rec.tipo

            val tvNueva = view.findViewById<TextView>(R.id.tvRecNueva)
            if (!rec.leida) tvNueva.visibility = View.VISIBLE

            val iconView = view.findViewById<ImageView>(R.id.ivIconoRec)
            val iconContainer = view.findViewById<View>(R.id.fIconoRec)
            when (rec.tipo.lowercase()) {
                "descanso" -> {
                    iconView.setImageResource(R.drawable.ic_moon)
                    iconView.clearColorFilter()
                    iconContainer.setBackgroundResource(R.drawable.bg_circle_purple_light)
                }
                "medicacion", "medicación" -> {
                    iconView.setImageResource(R.drawable.ic_medical)
                    iconView.setColorFilter(getColor(R.color.colorRedAlert))
                    iconContainer.setBackgroundResource(R.drawable.bg_circle_red_light)
                }
                "dieta", "nutricion", "nutrición" -> {
                    iconView.setImageResource(R.drawable.ic_drop)
                    iconView.setColorFilter(getColor(R.color.colorOrangeCaution))
                    iconContainer.setBackgroundResource(R.drawable.bg_circle_orange_light)
                }
                "ejercicio" -> {
                    iconView.setImageResource(R.drawable.ic_wind)
                    iconView.setColorFilter(getColor(R.color.colorGreenSafe))
                    iconContainer.setBackgroundResource(R.drawable.bg_circle_green_light)
                }
                else -> {
                    iconView.setImageResource(R.drawable.ic_medical)
                    iconView.setColorFilter(getColor(R.color.colorPrimary))
                    iconContainer.setBackgroundResource(R.drawable.bg_circle_blue_light)
                }
            }

            view.setOnClickListener { mostrarDetalleRecomendacion(rec) }
            containerRecomendaciones.addView(view)
        }
    }

    private fun mostrarDetalleRecomendacion(rec: Recomendacion) {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_recomendacion_detalle, null)

        view.findViewById<TextView>(R.id.tvBsTitulo).text = rec.titulo
        view.findViewById<TextView>(R.id.tvBsDescripcion).text = rec.descripcion
        view.findViewById<TextView>(R.id.tvBsTipo).text = rec.tipo

        val btnMarcar = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMarcarLeida)
        if (rec.leida) btnMarcar.visibility = View.GONE

        btnMarcar.setOnClickListener {
            rec.id?.let { id ->
                lifecycleScope.launch {
                    PacienteController.marcarRecomendacionLeida(id)
                        .onSuccess {
                            sheet.dismiss()
                            cargarDatos()
                        }
                        .onFailure {
                            Toast.makeText(this@AlertasActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        view.findViewById<View>(R.id.btnCerrarSheet).setOnClickListener { sheet.dismiss() }

        sheet.setContentView(view)
        sheet.show()
=======
import androidx.appcompat.app.AppCompatActivity

class AlertasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    }
}
