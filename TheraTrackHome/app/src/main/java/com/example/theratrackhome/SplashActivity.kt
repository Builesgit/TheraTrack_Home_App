package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.AuthController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(2000)

            if (AuthController.sesionActiva()) {
                AuthController.obtenerPerfilActual()
                    .onSuccess { perfil ->
                        val destino = when {
                            perfil.estado == "pendiente" -> EsperaAprobacionActivity::class.java
                            perfil.estado == "rechazado" -> {
                                AuthController.logout()
                                PerfilActivity::class.java
                            }
                            perfil.rol == "profesional"  -> HospitalMainActivity::class.java
                            else                         -> PacienteMainActivity::class.java
                        }
                        startActivity(Intent(this@SplashActivity, destino))
                    }
                    .onFailure {
                        startActivity(Intent(this@SplashActivity, PerfilActivity::class.java))
                    }
            } else {
                startActivity(Intent(this@SplashActivity, PerfilActivity::class.java))
            }
            finish()
        }
    }
}
