package com.example.theratrackhome.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.DetallePacienteActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Paciente
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PacientesHospitalFragment : Fragment() {

    private lateinit var tvSubtitulo: TextView
    private lateinit var etBuscar: EditText
    private lateinit var containerLista: LinearLayout
    private lateinit var tvSinResultados: TextView

    private var todosPacientes: List<Paciente> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_pacientes_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavHospital)?.visibility = View.GONE

        tvSubtitulo     = view.findViewById(R.id.tvSubtituloPacientes)
        etBuscar        = view.findViewById(R.id.etBuscarPaciente)
        containerLista  = view.findViewById(R.id.containerListaPacientes)
        tvSinResultados = view.findViewById(R.id.tvSinResultados)

        cargarPacientes()

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarPacientes(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarPacientes() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    val hospitalId = perfil.hospitalId ?: return@onSuccess
                    PacienteController.obtenerPacientesHospital(hospitalId)
                        .onSuccess { pacientes ->
                            todosPacientes = pacientes
                            tvSubtitulo.text = "${pacientes.size} pacientes registrados"
                            mostrarPacientes(pacientes)
                        }
                        .onFailure {
                            tvSubtitulo.text = "Error al cargar pacientes"
                        }
                }
        }
    }

    private fun filtrarPacientes(query: String) {
        if (query.isBlank()) {
            mostrarPacientes(todosPacientes)
            return
        }
        val filtrados = todosPacientes.filter { p ->
            p.id.contains(query, ignoreCase = true) ||
            p.radiofarmaco.contains(query, ignoreCase = true)
        }
        mostrarPacientes(filtrados)
    }

    private fun mostrarPacientes(pacientes: List<Paciente>) {
        containerLista.removeAllViews()
        if (pacientes.isEmpty()) {
            tvSinResultados.visibility = View.VISIBLE
            return
        }
        tvSinResultados.visibility = View.GONE
        for (paciente in pacientes) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_paciente_dashboard, containerLista, false)
            val iniciales = paciente.id.take(2).uppercase()
            itemView.findViewById<TextView>(R.id.tvPacienteIniciales).text = iniciales
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
            containerLista.addView(itemView)
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
