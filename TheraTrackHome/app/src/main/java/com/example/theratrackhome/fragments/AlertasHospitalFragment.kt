package com.example.theratrackhome.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Alerta
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class AlertasHospitalFragment : Fragment() {

    private lateinit var tvSubtitulo: TextView
    private lateinit var chipTodas: TextView
    private lateinit var chipNoLeidas: TextView
    private lateinit var containerLista: LinearLayout
    private lateinit var tvSinAlertas: TextView

    private var todasAlertas: List<Alerta> = emptyList()
    private var mostrandoSoloNoLeidas = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_alertas_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavHospital)?.visibility = View.GONE

        tvSubtitulo    = view.findViewById(R.id.tvSubtituloAlertas)
        chipTodas      = view.findViewById(R.id.chipTodas)
        chipNoLeidas   = view.findViewById(R.id.chipNoLeidas)
        containerLista = view.findViewById(R.id.containerListaAlertas)
        tvSinAlertas   = view.findViewById(R.id.tvSinAlertas)

        chipTodas.setOnClickListener {
            mostrandoSoloNoLeidas = false
            chipTodas.setBackgroundResource(R.drawable.bg_chip_active)
            chipTodas.setTextColor(requireContext().getColor(R.color.white))
            chipNoLeidas.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipNoLeidas.setTextColor(requireContext().getColor(R.color.colorTextSecondary))
            mostrarAlertas(todasAlertas)
        }

        chipNoLeidas.setOnClickListener {
            mostrandoSoloNoLeidas = true
            chipNoLeidas.setBackgroundResource(R.drawable.bg_chip_active)
            chipNoLeidas.setTextColor(requireContext().getColor(R.color.white))
            chipTodas.setBackgroundResource(R.drawable.bg_chip_inactive)
            chipTodas.setTextColor(requireContext().getColor(R.color.colorTextSecondary))
            mostrarAlertas(todasAlertas.filter { !it.leida })
        }

        cargarAlertas()
    }

    private fun cargarAlertas() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    val hospitalId = perfil.hospitalId ?: return@onSuccess
                    PacienteController.obtenerTodasAlertasHospital(hospitalId)
                        .onSuccess { alertas ->
                            todasAlertas = alertas
                            val noLeidas = alertas.count { !it.leida }
                            tvSubtitulo.text = "$noLeidas sin leer · ${alertas.size} total"
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
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_alerta_hospital, containerLista, false)
            itemView.findViewById<TextView>(R.id.tvAlertaHospTitulo).text = alerta.titulo
            itemView.findViewById<TextView>(R.id.tvAlertaHospMensaje).text = alerta.mensaje
            itemView.findViewById<TextView>(R.id.tvAlertaHospTipo).text = alerta.tipo
            itemView.findViewById<TextView>(R.id.tvAlertaHospPaciente).text = "· Pac. ${alerta.pacienteId.take(8)}"
            val tvLeida = itemView.findViewById<TextView>(R.id.tvAlertaHospLeida)
            if (alerta.leida) {
                itemView.alpha = 0.6f
                tvLeida.visibility = View.VISIBLE
            }
            itemView.setOnClickListener { mostrarDetalleAlerta(alerta) }
            containerLista.addView(itemView)
        }
    }

    private fun mostrarDetalleAlerta(alerta: Alerta) {
        val sheet = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_alerta_detalle, null)

        sheetView.findViewById<TextView>(R.id.tvBsAlertaTitulo).text = alerta.titulo
        sheetView.findViewById<TextView>(R.id.tvBsAlertaMensaje).text = alerta.mensaje
        sheetView.findViewById<TextView>(R.id.tvBsAlertaTipo).text = alerta.tipo
        sheetView.findViewById<TextView>(R.id.tvBsAlertaPaciente).text = "Pac. ${alerta.pacienteId.take(8)}"
        sheetView.findViewById<TextView>(R.id.tvBsAlertaFecha).text = "Fecha: ${alerta.createdAt?.take(10) ?: "—"}"

        val btnMarcar = sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMarcarAlertaLeida)
        if (alerta.leida) btnMarcar.visibility = View.GONE

        btnMarcar.setOnClickListener {
            alerta.id?.let { id ->
                viewLifecycleOwner.lifecycleScope.launch {
                    PacienteController.marcarAlertaLeida(id)
                        .onSuccess {
                            sheet.dismiss()
                            cargarAlertas()
                        }
                        .onFailure {
                            Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        sheetView.findViewById<View>(R.id.btnCerrarAlertaSheet).setOnClickListener { sheet.dismiss() }

        sheet.setContentView(sheetView)
        sheet.show()
    }
}
