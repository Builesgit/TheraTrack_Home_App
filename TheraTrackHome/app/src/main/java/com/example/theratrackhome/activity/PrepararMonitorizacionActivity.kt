package com.example.theratrackhome.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.theratrackhome.databinding.ActivityPrepararMonitorizacionBinding

class PrepararMonitorizacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrepararMonitorizacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrepararMonitorizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.btnIniciarRevision.setOnClickListener {
            val intent = Intent(this, ConexionDispositivoActivity::class.java)
            startActivity(intent)
        }
    }
}