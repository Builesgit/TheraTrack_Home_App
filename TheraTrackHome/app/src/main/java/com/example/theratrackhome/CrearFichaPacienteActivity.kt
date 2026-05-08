package com.example.theratrackhome

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AdminController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CrearFichaPacienteActivity : AppCompatActivity() {

    private lateinit var btnVolver: ImageView
    private lateinit var tvPacienteNombre: TextView
    private lateinit var etRadiofarmaco: EditText
    private lateinit var etDosisMbq: EditText
    private lateinit var etFechaTratamiento: EditText
    private lateinit var etFechaAlta: EditText
    private lateinit var etDiasAislamiento: EditText
    private lateinit var etNotasClinicas: EditText
    private lateinit var btnCrearFicha: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_ficha_paciente)

        btnVolver           = findViewById(R.id.btnVolverFicha)
        tvPacienteNombre    = findViewById(R.id.tvFichaPacienteNombre)
        etRadiofarmaco      = findViewById(R.id.etRadiofarmaco)
        etDosisMbq          = findViewById(R.id.etDosisMbq)
        etFechaTratamiento  = findViewById(R.id.etFechaTratamiento)
        etFechaAlta         = findViewById(R.id.etFechaAlta)
        etDiasAislamiento   = findViewById(R.id.etDiasAislamiento)
        etNotasClinicas     = findViewById(R.id.etNotasClinicas)
        btnCrearFicha       = findViewById(R.id.btnCrearFicha)

        val perfilId     = intent.getStringExtra("perfil_id") ?: run { finish(); return }
        val perfilNombre = intent.getStringExtra("perfil_nombre") ?: "Paciente"

        tvPacienteNombre.text = perfilNombre
        btnVolver.setOnClickListener { finish() }

        btnCrearFicha.setOnClickListener {
            val radiofarmaco    = etRadiofarmaco.text.toString().trim()
            val dosisTxt        = etDosisMbq.text.toString().trim()
            val fechaTratamiento = etFechaTratamiento.text.toString().trim()
            val fechaAlta       = etFechaAlta.text.toString().trim()
            val diasTxt         = etDiasAislamiento.text.toString().trim()
            val notas           = etNotasClinicas.text.toString().trim()

            if (radiofarmaco.isBlank() || fechaTratamiento.isBlank() || fechaAlta.isBlank()) {
                Toast.makeText(this, "Rellena los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dosisMbq = dosisTxt.toDoubleOrNull()
            val diasAislamiento = diasTxt.toIntOrNull() ?: 7

            btnCrearFicha.isEnabled = false

            lifecycleScope.launch {
                AdminController.aprobarSolicitud(perfilId)
                    .onFailure {
                        Toast.makeText(
                            this@CrearFichaPacienteActivity,
                            "Error al aprobar paciente: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        btnCrearFicha.isEnabled = true
                        return@launch
                    }

                AdminController.crearFichaPaciente(
                    perfilId        = perfilId,
                    radiofarmaco    = radiofarmaco,
                    dosisMbq        = dosisMbq,
                    fechaTratamiento = fechaTratamiento,
                    fechaAlta       = fechaAlta,
                    diasAislamiento = diasAislamiento,
                    notasClinicas   = notas.ifBlank { null }
                ).onSuccess {
                    Toast.makeText(
                        this@CrearFichaPacienteActivity,
                        "Ficha creada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }.onFailure {
                    Toast.makeText(
                        this@CrearFichaPacienteActivity,
                        "Error al crear ficha: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    btnCrearFicha.isEnabled = true
                }
            }
        }
    }
}
