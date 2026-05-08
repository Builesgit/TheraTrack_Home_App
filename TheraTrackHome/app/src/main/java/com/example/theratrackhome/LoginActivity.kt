package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.util.IdentificadorUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton     = findViewById<ImageView>(R.id.backButton)
        val loginButton    = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val etCipaOEmail   = findViewById<EditText>(R.id.email)
        val etPin          = findViewById<TextInputEditText>(R.id.password)
        val btnAyudaCipa   = findViewById<ImageView>(R.id.btnAyudaCipa)

        backButton.setOnClickListener { finish() }
        btnAyudaCipa.setOnClickListener { mostrarAyudaCipa() }

        loginButton.setOnClickListener {
            val input = etCipaOEmail.text.toString().trim()
            val pin   = etPin.text.toString().trim()

            if (input.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailNormalizado = IdentificadorUtils.normalizarAEmail(input, esPaciente = true)
            val identificador    = IdentificadorUtils.extraerIdentificadorOriginal(input)
            val password         = IdentificadorUtils.generarPassword(identificador, pin)

            loginButton.isEnabled = false
            lifecycleScope.launch {
                AuthController.login(emailNormalizado, password)
                    .onSuccess {
                        AuthController.obtenerRolActual()
                            .onSuccess { rol ->
                                if (rol == "profesional") {
                                    AuthController.logout()
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Esta cuenta es de profesional. Usa el acceso hospitalario.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    loginButton.isEnabled = true
                                } else {
                                    startActivity(Intent(this@LoginActivity, PacienteMainActivity::class.java))
                                    finish()
                                }
                            }
                            .onFailure {
                                startActivity(Intent(this@LoginActivity, PacienteMainActivity::class.java))
                                finish()
                            }
                    }
                    .onFailure { e ->
                        when (e) {
                            is AuthController.CuentaPendienteException -> {
                                startActivity(Intent(this@LoginActivity, EsperaAprobacionActivity::class.java))
                                finish()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "CIPA/Email o PIN incorrectos",
                                    Toast.LENGTH_LONG
                                ).show()
                                loginButton.isEnabled = true
                            }
                        }
                    }
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun mostrarAyudaCipa() {
        val view = layoutInflater.inflate(R.layout.dialog_ayuda_cipa, null)
        AlertDialog.Builder(this)
            .setTitle("¿Dónde encuentro mi CIPA?")
            .setView(view)
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
