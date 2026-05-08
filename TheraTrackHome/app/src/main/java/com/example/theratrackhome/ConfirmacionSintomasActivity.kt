package com.example.theratrackhome

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfirmacionSintomasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_sintomas)

        val hoy = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")).format(Date())
        findViewById<TextView>(R.id.tvFechaRegistro).text = hoy

        findViewById<MaterialButton>(R.id.btnVolverInicio).setOnClickListener {
<<<<<<< HEAD
            val intent = Intent(this, PacienteMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("fragment_inicial", "dashboard")
=======
            val intent = Intent(this, DashboardPacienteActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.tvVerResumen).setOnClickListener {
<<<<<<< HEAD
            val intent = Intent(this, PacienteMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("fragment_inicial", "historial")
=======
            val intent = Intent(this, HistorialActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.menu.findItem(R.id.nav_sintomas)?.isChecked = true
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
<<<<<<< HEAD
                R.id.nav_inicio -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "dashboard")
                    }
                    startActivity(i); finish(); true
                }
                R.id.nav_historial -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "historial")
                    }
                    startActivity(i); finish(); true
                }
                R.id.nav_sintomas -> true
                R.id.nav_perfil -> {
                    val i = Intent(this, PacienteMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("fragment_inicial", "perfil")
                    }
                    startActivity(i); finish(); true
                }
=======
                R.id.nav_inicio    -> { startActivity(Intent(this, DashboardPacienteActivity::class.java)); finish(); true }
                R.id.nav_historial -> { startActivity(Intent(this, HistorialActivity::class.java)); finish(); true }
                R.id.nav_sintomas  -> true
                R.id.nav_perfil    -> { startActivity(Intent(this, PerfilPacienteActivity::class.java)); true }
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
                else -> false
            }
        }
    }
}
