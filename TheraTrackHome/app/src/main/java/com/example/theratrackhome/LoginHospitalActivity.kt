package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginHospitalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_hospital)

        val btnBack    = findViewById<ImageView>(R.id.btnBack)
        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin   = findViewById<MaterialButton>(R.id.btnLogin)

        btnBack.setOnClickListener { finish() }

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            lifecycleScope.launch {
                AuthController.login(email, password)
                    .onSuccess {
                        // Verificar que el usuario es profesional
                        AuthController.obtenerRolActual()
                            .onSuccess { rol ->
                                if (rol == "profesional") {
                                    startActivity(Intent(this@LoginHospitalActivity, DashboardHospitalActivity::class.java))
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
                        Toast.makeText(
                            this@LoginHospitalActivity,
                            e.message ?: "Credenciales incorrectas",
                            Toast.LENGTH_LONG
                        ).show()
                        btnLogin.isEnabled = true
                    }
            }
        }
    }
}
