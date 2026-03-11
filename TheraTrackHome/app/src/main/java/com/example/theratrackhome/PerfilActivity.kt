package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val btnPaciente = findViewById<Button>(R.id.btnPaciente)

        btnPaciente.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}