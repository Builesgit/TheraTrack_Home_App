package com.example.theratrackhome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.theratrackhome.fragments.DashboardPacienteFragment
import com.example.theratrackhome.fragments.HistorialFragment
import com.example.theratrackhome.fragments.PerfilPacienteFragment
import com.example.theratrackhome.fragments.RegistroDiarioFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class PacienteMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var fragmentoActualTag: String = "dashboard"

    private val permisoNotificacionesLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paciente_main)

        bottomNav = findViewById(R.id.bottomNav)
        configurarBottomNav()
        solicitarPermisoNotificacionesSiHaceFalta()

        if (savedInstanceState == null) {
            val fragmentInicial = intent.getStringExtra("fragment_inicial") ?: "dashboard"
            navegarA(fragmentInicial)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("fragment_inicial")?.let { tag -> navegarA(tag) }
    }

    fun navegarA(tag: String) {
        fragmentoActualTag = tag
        val fragment = crearFragment(tag)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
        actualizarBottomNavItem(tag)
    }

    private fun crearFragment(tag: String): Fragment = when (tag) {
        "dashboard" -> DashboardPacienteFragment()
        "historial" -> HistorialFragment()
        "sintomas"  -> RegistroDiarioFragment()
        "perfil"    -> PerfilPacienteFragment()
        else        -> DashboardPacienteFragment()
    }

    private fun actualizarBottomNavItem(tag: String) {
        val itemId = when (tag) {
            "dashboard" -> R.id.nav_inicio
            "historial" -> R.id.nav_historial
            "sintomas"  -> R.id.nav_sintomas
            "perfil"    -> R.id.nav_perfil
            else        -> R.id.nav_inicio
        }
        bottomNav.menu.findItem(itemId)?.isChecked = true
    }

    private fun configurarBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            val tag = when (item.itemId) {
                R.id.nav_inicio    -> "dashboard"
                R.id.nav_historial -> "historial"
                R.id.nav_sintomas  -> "sintomas"
                R.id.nav_perfil    -> "perfil"
                else               -> return@setOnItemSelectedListener false
            }
            if (fragmentoActualTag != tag) navegarA(tag)
            true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (fragmentoActualTag != "dashboard") {
            navegarA("dashboard")
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private fun solicitarPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT < 33) return
        val prefs = getSharedPreferences("preferencias_app", MODE_PRIVATE)
        if (prefs.getBoolean("permiso_notif_solicitado", false)) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        prefs.edit().putBoolean("permiso_notif_solicitado", true).apply()
        permisoNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
