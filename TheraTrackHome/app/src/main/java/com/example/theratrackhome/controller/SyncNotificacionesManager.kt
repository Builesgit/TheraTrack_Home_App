package com.example.theratrackhome.controller

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SyncNotificacionesManager {
    private const val PREF = "sync_notificaciones"
    private const val KEY_ULTIMA_REVISION = "ultima_revision_iso"

    suspend fun revisarYNotificar(context: Context) {
        val perfil = AuthController.obtenerPerfilActual().getOrNull() ?: return
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val keyUltimaRevision = "${KEY_ULTIMA_REVISION}_${perfil.id}"
        val desdeIso = prefs.getString(keyUltimaRevision, null) ?: hace24HorasIso()
        val ahoraIso = ahoraIso()

        if (perfil.rol == "paciente") {
            val paciente = PacienteController.obtenerPacienteActual().getOrNull() ?: return
            PacienteController.obtenerAlertasPacienteDesde(paciente.id, desdeIso).getOrDefault(emptyList())
                .forEach { NotificacionesManager.mostrarNotificacionAlerta(context, it) }
            PacienteController.obtenerRecomendacionesPacienteDesde(paciente.hospitalId, paciente.id, desdeIso).getOrDefault(emptyList())
                .forEach { NotificacionesManager.mostrarNotificacionRecomendacion(context, it) }
        } else if (perfil.rol == "profesional") {
            val hospitalId = perfil.hospitalId ?: return
            PacienteController.obtenerAlertasHospitalDesde(hospitalId, desdeIso).getOrDefault(emptyList())
                .filter { it.tipo.lowercase().contains("crit") || it.tipo.lowercase().contains("alert") || !it.leida }
                .forEach { NotificacionesManager.mostrarNotificacionAlerta(context, it) }
        }

        prefs.edit().putString(keyUltimaRevision, ahoraIso).apply()
    }

    private fun formatter() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun ahoraIso(): String = formatter().format(Date())

    private fun hace24HorasIso(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.HOUR_OF_DAY, -24)
        return formatter().format(cal.time)
    }
}
