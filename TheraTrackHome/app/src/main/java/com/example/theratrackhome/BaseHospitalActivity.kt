package com.example.theratrackhome

import android.content.Intent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseHospitalActivity : AppCompatActivity() {

    protected fun configurarNavegacion(itemSeleccionado: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavHospital) ?: return
        bottomNav.itemIconTintList = resources.getColorStateList(R.color.nav_hospital_color, theme)
        bottomNav.itemTextColor = resources.getColorStateList(R.color.nav_hospital_color, theme)
        bottomNav.menu.findItem(itemSeleccionado)?.isChecked = true

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == itemSeleccionado) return@setOnItemSelectedListener true
            val intent = when (item.itemId) {
                R.id.nav_dashboard  -> Intent(this, DashboardHospitalActivity::class.java)
                R.id.nav_pacientes  -> Intent(this, PacientesHospitalActivity::class.java)
                R.id.nav_alertas    -> Intent(this, AlertasHospitalActivity::class.java)
                R.id.nav_informes   -> Intent(this, InformesHospitalActivity::class.java)
                R.id.nav_ajustes    -> Intent(this, AjustesHospitalActivity::class.java)
                else                -> null
            }
            intent?.let {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(it)
            }
            true
        }
    }
}
