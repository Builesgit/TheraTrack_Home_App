package com.example.theratrackhome.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.ConfirmacionSintomasActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.RegistroController
import com.example.theratrackhome.model.RegistroDiario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroDiarioFragment : Fragment() {

    private lateinit var rowFatiga: LinearLayout
    private lateinit var rowNauseas: LinearLayout
    private lateinit var rowDolor: LinearLayout
    private lateinit var rowIrritacion: LinearLayout
    private lateinit var rowMareo: LinearLayout
    private lateinit var rowNinguno: LinearLayout

    private lateinit var checkFatiga: CheckBox
    private lateinit var checkNauseas: CheckBox
    private lateinit var checkDolor: CheckBox
    private lateinit var checkIrritacion: CheckBox
    private lateinit var checkMareo: CheckBox
    private lateinit var checkNinguno: CheckBox

    private lateinit var etObservaciones: EditText
    private lateinit var btnGuardar: MaterialButton

    private val sintomasRows get() = listOf(rowFatiga, rowNauseas, rowDolor, rowIrritacion, rowMareo)
    private val sintomasChecks get() = listOf(checkFatiga, checkNauseas, checkDolor, checkIrritacion, checkMareo)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_registro_diario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnBack)?.visibility = View.GONE

        rowFatiga     = view.findViewById(R.id.rowFatiga)
        rowNauseas    = view.findViewById(R.id.rowNauseas)
        rowDolor      = view.findViewById(R.id.rowDolor)
        rowIrritacion = view.findViewById(R.id.rowIrritacion)
        rowMareo      = view.findViewById(R.id.rowMareo)
        rowNinguno    = view.findViewById(R.id.rowNinguno)

        checkFatiga     = view.findViewById(R.id.checkFatiga)
        checkNauseas    = view.findViewById(R.id.checkNauseas)
        checkDolor      = view.findViewById(R.id.checkDolor)
        checkIrritacion = view.findViewById(R.id.checkIrritacion)
        checkMareo      = view.findViewById(R.id.checkMareo)
        checkNinguno    = view.findViewById(R.id.checkNinguno)

        etObservaciones = view.findViewById(R.id.etObservaciones)
        btnGuardar      = view.findViewById(R.id.btnGuardar)

        configurarClicksSintomas()

        btnGuardar.setOnClickListener { guardarRegistro() }
    }

    private fun configurarClicksSintomas() {
        sintomasRows.forEachIndexed { i, row ->
            row.setOnClickListener {
                val check = sintomasChecks[i]
                val nuevoEstado = !check.isChecked
                check.isChecked = nuevoEstado
                actualizarEstiloFila(row, nuevoEstado)
                if (nuevoEstado) {
                    checkNinguno.isChecked = false
                    actualizarEstiloFila(rowNinguno, false)
                }
            }
        }

        rowNinguno.setOnClickListener {
            val nuevoEstado = !checkNinguno.isChecked
            checkNinguno.isChecked = nuevoEstado
            actualizarEstiloFila(rowNinguno, nuevoEstado)
            if (nuevoEstado) {
                sintomasChecks.forEach { it.isChecked = false }
                sintomasRows.forEach { actualizarEstiloFila(it, false) }
            }
        }
    }

    private fun actualizarEstiloFila(row: LinearLayout, seleccionado: Boolean) {
        row.setBackgroundResource(
            if (seleccionado) R.drawable.bg_symptom_selected
            else R.drawable.bg_symptom_normal
        )
    }

    private fun guardarRegistro() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    val sintomasSeleccionados = buildList {
                        if (checkFatiga.isChecked)     add("Fatiga")
                        if (checkNauseas.isChecked)    add("Náuseas")
                        if (checkDolor.isChecked)      add("Dolor")
                        if (checkIrritacion.isChecked) add("Irritación")
                        if (checkMareo.isChecked)      add("Mareo")
                        if (checkNinguno.isChecked)    add("Ninguno")
                    }

                    if (sintomasSeleccionados.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Selecciona al menos un síntoma o 'Ninguno'",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@onSuccess
                    }

                    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val nivelFatiga = if (checkFatiga.isChecked) 3 else 1
                    val nivelDolor  = if (checkDolor.isChecked)  3 else 1

                    val registro = RegistroDiario(
                        pacienteId  = paciente.id,
                        fecha       = hoy,
                        nivelFatiga = nivelFatiga,
                        nivelDolor  = nivelDolor,
                        sintomas    = sintomasSeleccionados.joinToString(", "),
                        notas       = etObservaciones.text.toString().trim().ifEmpty { null }
                    )

                    btnGuardar.isEnabled = false
                    RegistroController.guardarRegistro(registro)
                        .onSuccess {
                            startActivity(Intent(requireContext(), ConfirmacionSintomasActivity::class.java))
                        }
                        .onFailure { e ->
                            val mensaje = if (e.message?.contains("unique", ignoreCase = true) == true)
                                "Ya registraste tus síntomas hoy."
                            else
                                e.message ?: "Error al guardar el registro"
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                            btnGuardar.isEnabled = true
                        }
                }
                .onFailure {
                    Toast.makeText(requireContext(), "Error al cargar datos del paciente", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
