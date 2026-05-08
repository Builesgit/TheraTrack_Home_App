package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backButton        = findViewById<ImageView>(R.id.backButton)
        val etNombre          = findViewById<EditText>(R.id.etNombre)
        val etCipa            = findViewById<EditText>(R.id.etCipa)
        val btnAyudaCipa      = findViewById<ImageView>(R.id.btnAyudaCipa)
        val etCodigo          = findViewById<EditText>(R.id.etCodigo)
        val etPassword        = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val checkTerms        = findViewById<CheckBox>(R.id.checkTerms)
        val btnRegister       = findViewById<Button>(R.id.btnRegister)

        backButton.setOnClickListener { finish() }
        btnAyudaCipa.setOnClickListener { mostrarAyudaCipa() }

        btnRegister.setOnClickListener {
            val nombre  = etNombre.text.toString().trim()
            val cipa    = etCipa.text.toString().trim()
            val codigo  = etCodigo.text.toString().trim()
            val pin     = etPassword.text.toString().trim()
            val pinConf = etConfirmPassword.text.toString().trim()

            if (nombre.isEmpty() || cipa.isEmpty() || codigo.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!IdentificadorUtils.validarCipa(cipa)) {
                Toast.makeText(this, "El CIPA debe tener exactamente 10 dígitos numéricos", Toast.LENGTH_SHORT).show()
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
            if (!checkTerms.isChecked) {
                Toast.makeText(this, "Acepta los términos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email    = IdentificadorUtils.cipaToEmail(cipa)
            val password = IdentificadorUtils.generarPassword(cipa, pin)

            btnRegister.isEnabled = false
            lifecycleScope.launch {
                AuthController.registrarPaciente(nombre, email, password, codigo, cipa, "cipa")
                    .onSuccess {
                        val intent = Intent(this@RegisterActivity, EsperaAprobacionActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
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

    private fun mostrarAyudaCipa() {
        val view = layoutInflater.inflate(R.layout.dialog_ayuda_cipa, null)
        AlertDialog.Builder(this)
            .setTitle("¿Dónde encuentro mi CIPA?")
            .setView(view)
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
