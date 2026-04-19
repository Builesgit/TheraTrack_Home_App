package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton     = findViewById<ImageView>(R.id.backButton)
        val loginButton    = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
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
                    }
            }
        }

        // botón crear cuenta
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}