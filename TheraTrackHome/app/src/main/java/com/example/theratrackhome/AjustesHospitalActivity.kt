package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.controller.PacienteController
import kotlinx.coroutines.launch

class AjustesHospitalActivity : BaseHospitalActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes_hospital)
        configurarNavegacion(R.id.nav_ajustes)

        val tvNombrePerfil = findViewById<TextView>(R.id.tvNombrePerfil)
        val itemCerrarSesion = findViewById<LinearLayout>(R.id.itemCerrarSesion)

        lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvNombrePerfil.text = "${perfil.nombre} · ${perfil.rol}"
                }
        }

        itemCerrarSesion.setOnClickListener {
            lifecycleScope.launch {
                PacienteController.cerrarSesion()
                val intent = Intent(this@AjustesHospitalActivity, LoginHospitalActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}
