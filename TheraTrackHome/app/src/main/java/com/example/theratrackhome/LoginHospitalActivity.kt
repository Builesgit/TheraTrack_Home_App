package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.util.IdentificadorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginHospitalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_hospital)

        val btnBack             = findViewById<ImageView>(R.id.btnBack)
        val etColegiadoOEmail   = findViewById<EditText>(R.id.etEmail)
        val etPin               = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin            = findViewById<MaterialButton>(R.id.btnLogin)
        val btnAyudaColegiado   = findViewById<ImageView>(R.id.btnAyudaColegiado)
        val tvSolicitarAcceso   = findViewById<TextView>(R.id.tvSolicitarAcceso)

        btnBack.setOnClickListener { finish() }
        btnAyudaColegiado.setOnClickListener { mostrarAyudaColegiado() }

        tvSolicitarAcceso.setOnClickListener {
            startActivity(Intent(this, RegistroHospitalActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val input = etColegiadoOEmail.text.toString().trim()
            val pin   = etPin.text.toString().trim()

            if (input.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailNormalizado = IdentificadorUtils.normalizarAEmail(input, esPaciente = false)
            val identificador    = IdentificadorUtils.extraerIdentificadorOriginal(input)
            val password         = IdentificadorUtils.generarPassword(identificador, pin)

            btnLogin.isEnabled = false
            lifecycleScope.launch {
                AuthController.login(emailNormalizado, password)
                    .onSuccess {
                        AuthController.obtenerRolActual()
                            .onSuccess { rol ->
                                if (rol == "profesional") {
                                    startActivity(Intent(this@LoginHospitalActivity, HospitalMainActivity::class.java))
                                    finish()
                                } else {
                                    AuthController.logout()
                                    Toast.makeText(
                                        this@LoginHospitalActivity,
                                        "Esta cuenta no tiene acceso al panel hospitalario.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    btnLogin.isEnabled = true
                                }
                            }
                            .onFailure {
                                Toast.makeText(this@LoginHospitalActivity, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
                                btnLogin.isEnabled = true
                            }
                    }
                    .onFailure { e ->
                        when (e) {
                            is AuthController.CuentaPendienteException -> {
                                startActivity(Intent(this@LoginHospitalActivity, EsperaAprobacionActivity::class.java))
                                finish()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginHospitalActivity,
                                    "Colegiado/Email o PIN incorrectos",
                                    Toast.LENGTH_LONG
                                ).show()
                                btnLogin.isEnabled = true
                            }
                        }
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
