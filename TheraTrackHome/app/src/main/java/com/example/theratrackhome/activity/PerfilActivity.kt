package com.example.theratrackhome.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.theratrackhome.R

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val btnPaciente = findViewById<Button>(R.id.btnPaciente)
        val btnHospital = findViewById<Button>(R.id.btnHospital)

        btnPaciente.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnHospital.setOnClickListener {
            startActivity(Intent(this, LoginHospitalActivity::class.java))
        }
    }
}