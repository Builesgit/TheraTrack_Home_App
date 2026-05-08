package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.model.Hospital
import com.example.theratrackhome.util.IdentificadorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegistroHospitalActivity : AppCompatActivity() {

    private val cargos = arrayOf(
        "Médico/a",
        "Enfermera/o",
        "Técnico/a de Radiodiagnóstico",
        "Radiofísico/a",
        "Administrativo/a",
        "Otro"
    )

    private var hospitales: List<Hospital> = emptyList()
    private var hospitalSeleccionado: Hospital? = null
    private var cargoSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_hospital)

        val btnBack           = findViewById<ImageView>(R.id.btnBack)
        val etNombre          = findViewById<EditText>(R.id.etNombre)
        val etColegiado       = findViewById<EditText>(R.id.etColegiado)
        val btnAyudaColegiado = findViewById<ImageView>(R.id.btnAyudaColegiado)
        val spinnerHospital   = findViewById<Spinner>(R.id.spinnerHospital)
        val spinnerCargo      = findViewById<Spinner>(R.id.spinnerCargo)
        val etTelefono        = findViewById<EditText>(R.id.etTelefono)
        val etMotivo          = findViewById<EditText>(R.id.etMotivo)
        val etPassword        = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val checkPrivacidad   = findViewById<CheckBox>(R.id.checkPrivacidad)
        val btnEnviar         = findViewById<MaterialButton>(R.id.btnEnviarSolicitud)

        btnBack.setOnClickListener { finish() }
        btnAyudaColegiado.setOnClickListener { mostrarAyudaColegiado() }

        // Spinner de cargos (fijo)
        cargoSeleccionado = cargos[0]
        val adapterCargo = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        adapterCargo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCargo.adapter = adapterCargo
        spinnerCargo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                cargoSeleccionado = cargos[pos]
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Cargar hospitales para el Spinner
        lifecycleScope.launch {
            PacienteController.obtenerHospitales()
                .onSuccess { lista ->
                    hospitales = lista
                    val nombres = lista.map { it.nombre }.toTypedArray()
                    val adapterH = ArrayAdapter(this@RegistroHospitalActivity,
                        android.R.layout.simple_spinner_item, nombres)
                    adapterH.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHospital.adapter = adapterH
                    if (lista.isNotEmpty()) hospitalSeleccionado = lista[0]
                    spinnerHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                            hospitalSeleccionado = hospitales.getOrNull(pos)
                        }
                        override fun onNothingSelected(p: AdapterView<*>?) {}
                    }
                }
                .onFailure {
                    Toast.makeText(this@RegistroHospitalActivity,
                        "Error al cargar hospitales. Comprueba tu conexión.", Toast.LENGTH_LONG).show()
                }
        }

        btnEnviar.setOnClickListener {
            val nombre    = etNombre.text.toString().trim()
            val colegiado = etColegiado.text.toString().trim()
            val telefono  = etTelefono.text.toString().trim()
            val motivo    = etMotivo.text.toString().trim()
            val pin       = etPassword.text.toString().trim()
            val pinConf   = etConfirmPassword.text.toString().trim()
            val hospital  = hospitalSeleccionado

            if (nombre.isEmpty() || colegiado.isEmpty() || telefono.isEmpty() || motivo.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!IdentificadorUtils.validarColegiado(colegiado)) {
                Toast.makeText(this, "El número de colegiado debe tener entre 6 y 10 dígitos numéricos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hospital == null) {
                Toast.makeText(this, "Selecciona un hospital", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin.length != 6) {
                Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin != pinConf) {
                Toast.makeText(this, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!checkPrivacidad.isChecked) {
                Toast.makeText(this, "Acepta la política de privacidad para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email    = IdentificadorUtils.colegiadoToEmail(colegiado)
            val password = IdentificadorUtils.generarPassword(colegiado, pin)

            btnEnviar.isEnabled = false
            lifecycleScope.launch {
                AuthController.registrarProfesional(
                    nombre           = nombre,
                    email            = email,
                    password         = password,
                    hospitalId       = hospital.id,
                    cargo            = cargoSeleccionado,
                    telefono         = telefono,
                    motivoSolicitud  = motivo,
                    identificador    = colegiado,
                    tipoIdentificador = "colegiado"
                )
                    .onSuccess {
                        val intent = Intent(this@RegistroHospitalActivity, EsperaAprobacionActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    .onFailure { e ->
                        Toast.makeText(
                            this@RegistroHospitalActivity,
                            e.message ?: "Error al enviar solicitud",
                            Toast.LENGTH_LONG
                        ).show()
                        btnEnviar.isEnabled = true
                    }
            }
        }
    }

    private fun mostrarAyudaColegiado() {
        AlertDialog.Builder(this)
            .setTitle("¿Qué es el número de colegiado?")
            .setMessage(
                "El número de colegiado es el identificador oficial de los profesionales sanitarios en España.\n\n" +
                "Formato: 2 dígitos de provincia + 6-8 dígitos del número.\n" +
                "Ejemplo: 281234567 (Madrid).\n\n" +
                "Puedes encontrarlo en tu carné de colegiado o en el registro del colegio médico de tu provincia."
            )
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
