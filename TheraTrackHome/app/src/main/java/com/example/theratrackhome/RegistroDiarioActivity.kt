package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.controller.RegistroController
import com.example.theratrackhome.model.RegistroDiario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroDiarioActivity : AppCompatActivity() {

    // Filas de síntomas
    private lateinit var rowFatiga: LinearLayout
    private lateinit var rowNauseas: LinearLayout
    private lateinit var rowDolor: LinearLayout
    private lateinit var rowIrritacion: LinearLayout
    private lateinit var rowMareo: LinearLayout
    private lateinit var rowNinguno: LinearLayout

    // Checkboxes
    private lateinit var checkFatiga: CheckBox
    private lateinit var checkNauseas: CheckBox
    private lateinit var checkDolor: CheckBox
    private lateinit var checkIrritacion: CheckBox
    private lateinit var checkMareo: CheckBox
    private lateinit var checkNinguno: CheckBox

    private lateinit var etObservaciones: EditText
    private lateinit var btnGuardar: MaterialButton

    // Filas y checks agrupados (excluye Ninguno)
    private val sintomasRows get() = listOf(rowFatiga, rowNauseas, rowDolor, rowIrritacion, rowMareo)
    private val sintomasChecks get() = listOf(checkFatiga, checkNauseas, checkDolor, checkIrritacion, checkMareo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_diario)

        enlazarVistas()
        configurarClicksSintomas()
        configurarBottomNav()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnGuardar.setOnClickListener { guardarRegistro() }
    }

    private fun enlazarVistas() {
        rowFatiga     = findViewById(R.id.rowFatiga)
        rowNauseas    = findViewById(R.id.rowNauseas)
        rowDolor      = findViewById(R.id.rowDolor)
        rowIrritacion = findViewById(R.id.rowIrritacion)
        rowMareo      = findViewById(R.id.rowMareo)
        rowNinguno    = findViewById(R.id.rowNinguno)

        checkFatiga     = findViewById(R.id.checkFatiga)
        checkNauseas    = findViewById(R.id.checkNauseas)
        checkDolor      = findViewById(R.id.checkDolor)
        checkIrritacion = findViewById(R.id.checkIrritacion)
        checkMareo      = findViewById(R.id.checkMareo)
        checkNinguno    = findViewById(R.id.checkNinguno)

        etObservaciones = findViewById(R.id.etObservaciones)
        btnGuardar      = findViewById(R.id.btnGuardar)
    }

    private fun configurarClicksSintomas() {
        // Síntomas normales: toggle y desmarcar Ninguno
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

        // Ninguno: marca solo él, desmarca el resto
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
        lifecycleScope.launch {
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
                            this@RegistroDiarioActivity,
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
                            startActivity(Intent(this@RegistroDiarioActivity, ConfirmacionSintomasActivity::class.java))
                            finish()
                        }
                        .onFailure { e ->
                            val mensaje = if (e.message?.contains("unique", ignoreCase = true) == true)
                                "Ya registraste tus síntomas hoy."
                            else
                                e.message ?: "Error al guardar el registro"
                            Toast.makeText(this@RegistroDiarioActivity, mensaje, Toast.LENGTH_LONG).show()
                            btnGuardar.isEnabled = true
                        }
                }
                .onFailure {
                    Toast.makeText(this@RegistroDiarioActivity, "Error al cargar datos del paciente", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun configurarBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.menu.findItem(R.id.nav_sintomas)?.isChecked = true
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio   -> { startActivity(Intent(this, DashboardPacienteActivity::class.java)); finish(); true }
                R.id.nav_historial-> { startActivity(Intent(this, HistorialActivity::class.java)); finish(); true }
                R.id.nav_sintomas -> true
                R.id.nav_perfil   -> { startActivity(Intent(this, PerfilPacienteActivity::class.java)); true }
                else -> false
            }
        }
    }
}
