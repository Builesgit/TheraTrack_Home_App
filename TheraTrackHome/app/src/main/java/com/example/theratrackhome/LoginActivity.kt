package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.util.IdentificadorUtils
=======
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton     = findViewById<ImageView>(R.id.backButton)
        val loginButton    = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
<<<<<<< HEAD
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
=======
        val emailField     = findViewById<EditText>(R.id.email)
        val passwordField  = findViewById<TextInputEditText>(R.id.password)

        // volver atrás
        backButton.setOnClickListener { finish() }

        // botón iniciar sesión
        loginButton.setOnClickListener {
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            lifecycleScope.launch {
                AuthController.login(email, password)
                    .onSuccess {
                        startActivity(Intent(this@LoginActivity, DashboardPacienteActivity::class.java))
                        finish()
                    }
                    .onFailure { e ->
                        Toast.makeText(
                            this@LoginActivity,
                            e.message ?: "Error al iniciar sesión",
                            Toast.LENGTH_LONG
                        ).show()
                        loginButton.isEnabled = true
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
                    }
            }
        }

<<<<<<< HEAD
=======
        // botón crear cuenta
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
<<<<<<< HEAD

    private fun mostrarAyudaCipa() {
        val view = layoutInflater.inflate(R.layout.dialog_ayuda_cipa, null)
        AlertDialog.Builder(this)
            .setTitle("¿Dónde encuentro mi CIPA?")
            .setView(view)
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
=======
}
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
