package com.example.theratrackhome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class ConfiguracionNotificacionesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion_notificaciones)
        val prefs = getSharedPreferences("preferencias_notif", MODE_PRIVATE)
        val swAlertas = findViewById<SwitchMaterial>(R.id.switchAlertasCriticas)
        val swRecomendaciones = findViewById<SwitchMaterial>(R.id.switchRecomendaciones)
        val swRecordatorios = findViewById<SwitchMaterial>(R.id.switchRecordatorios)

        swAlertas.isChecked = true
        swRecomendaciones.isChecked = prefs.getBoolean("notif_recomendaciones", true)
        swRecordatorios.isChecked = prefs.getBoolean("notif_recordatorios", true)

        swRecomendaciones.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("notif_recomendaciones", checked).apply()
        }
        swRecordatorios.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("notif_recordatorios", checked).apply()
        }
        findViewById<ImageView>(R.id.btnBackNotif).setOnClickListener { finish() }
    }
}
