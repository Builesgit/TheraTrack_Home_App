package com.example.theratrackhome.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.theratrackhome.databinding.ActivityConexionDispositivoBinding

class ConexionDispositivoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConexionDispositivoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConexionDispositivoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.progress = 65
        binding.tvProgress.text = "65%"

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}