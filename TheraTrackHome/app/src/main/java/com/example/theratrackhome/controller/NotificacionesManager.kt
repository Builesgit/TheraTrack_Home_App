package com.example.theratrackhome.controller

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.theratrackhome.AlertasActivity
import com.example.theratrackhome.PacienteMainActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Recomendacion

object NotificacionesManager {
    const val CANAL_ALERTAS_CRITICAS = "alertas_criticas"
    const val CANAL_RECOMENDACIONES = "recomendaciones"
    const val CANAL_RECORDATORIOS = "recordatorios"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val canales = listOf(
            NotificationChannel(CANAL_ALERTAS_CRITICAS, "Alertas críticas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas clínicas críticas"
                enableVibration(true)
            },
            NotificationChannel(CANAL_RECOMENDACIONES, "Recomendaciones", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CANAL_RECORDATORIOS, "Recordatorios", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannels(canales)
    }

    fun mostrarNotificacionAlerta(context: Context, alerta: Alerta) {
        notificar(
            context,
            CANAL_ALERTAS_CRITICAS,
            alerta.id?.hashCode() ?: alerta.hashCode(),
            alerta.titulo,
            alerta.mensaje,
            AlertasActivity::class.java,
            NotificationCompat.PRIORITY_HIGH
        )
    }

    fun mostrarNotificacionRecomendacion(context: Context, rec: Recomendacion) {
        val prefs = context.getSharedPreferences("preferencias_notif", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notif_recomendaciones", true)) return
        notificar(
            context,
            CANAL_RECOMENDACIONES,
            rec.id?.hashCode() ?: rec.hashCode(),
            rec.titulo,
            rec.descripcion,
            AlertasActivity::class.java,
            NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun mostrarRecordatorioSintomas(context: Context) {
        val prefs = context.getSharedPreferences("preferencias_notif", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notif_recordatorios", true)) return
        crearCanales(context.applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val id = "recordatorio_sintomas".hashCode()
        val intent = Intent(context, PacienteMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("fragment_inicial", "sintomas")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CANAL_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reporte diario de síntomas")
            .setContentText("Registra cómo te encuentras hoy para mantener informado a tu equipo médico.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Registra cómo te encuentras hoy para mantener informado a tu equipo médico."))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    fun cancelarTodas(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    private fun notificar(
        context: Context,
        canal: String,
        id: Int,
        title: String,
        text: String,
        target: Class<*>,
        priority: Int
    ) {
        crearCanales(context.applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, target).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(priority)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }
}
