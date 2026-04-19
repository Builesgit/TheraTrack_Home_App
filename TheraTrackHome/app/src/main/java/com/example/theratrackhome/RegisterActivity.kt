package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backButton        = findViewById<ImageView>(R.id.backButton)
        val etNombre          = findViewById<EditText>(R.id.etNombre)
        val etEmail           = findViewById<EditText>(R.id.etEmail)
        val etCodigo          = findViewById<EditText>(R.id.etCodigo)
        val etPassword        = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val checkTerms        = findViewById<CheckBox>(R.id.checkTerms)
        val btnRegister       = findViewById<Button>(R.id.btnRegister)

        backButton.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val nombre          = etNombre.text.toString().trim()
            val email           = etEmail.text.toString().trim()
            val codigo          = etCodigo.text.toString().trim()
            val password        = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (nombre.isEmpty() || email.isEmpty() || codigo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!checkTerms.isChecked) {
                Toast.makeText(this, "Acepta los términos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            lifecycleScope.launch {
                AuthController.register(nombre, email, codigo, password)
                    .onSuccess {
                        startActivity(Intent(this@RegisterActivity, DashboardPacienteActivity::class.java))
                        finish()
                    }
                    .onFailure { e ->
                        Toast.makeText(
                            this@RegisterActivity,
                            e.message ?: "Error al registrarse",
                            Toast.LENGTH_LONG
                        ).show()
                        btnRegister.isEnabled = true
                    }
            }
        }
    }
}