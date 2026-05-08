package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EsperaAprobacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_espera_aprobacion)

        findViewById<MaterialButton>(R.id.btnVolverInicio).setOnClickListener {
            irAInicio()
        }

        findViewById<MaterialButton>(R.id.btnCerrarSesion).setOnClickListener {
            lifecycleScope.launch {
                AuthController.logout()
                    .onSuccess { irAInicio() }
                    .onFailure {
                        Toast.makeText(
                            this@EsperaAprobacionActivity,
                            "Error al cerrar sesión",
                            Toast.LENGTH_SHORT
                        ).show()
                        irAInicio()
                    }
            }
        }
    }

    private fun irAInicio() {
        val intent = Intent(this, PerfilActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
