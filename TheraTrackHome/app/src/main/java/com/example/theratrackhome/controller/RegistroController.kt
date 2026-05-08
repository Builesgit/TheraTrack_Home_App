package com.example.theratrackhome.controller

import com.example.theratrackhome.model.RegistroDiario
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object RegistroController {

    private val client = SupabaseClient.client
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun obtenerRegistros(pacienteId: String, diasAtras: Int): Result<List<RegistroDiario>> = runCatching {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -diasAtras)
        val fechaDesde = sdf.format(cal.time)

        client.postgrest["registros_diarios"].select {
            filter {
                eq("paciente_id", pacienteId)
                gte("fecha", fechaDesde)
            }
        }.decodeList<RegistroDiario>()
    }

<<<<<<< HEAD
    suspend fun obtenerRegistrosRango(pacienteId: String, desde: String, hasta: String): Result<List<RegistroDiario>> = runCatching {
        client.postgrest["registros_diarios"].select {
            filter {
                eq("paciente_id", pacienteId)
                gte("fecha", desde)
                lte("fecha", hasta)
            }
        }.decodeList<RegistroDiario>().sortedBy { it.fecha }
    }

=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun guardarRegistro(registro: RegistroDiario): Result<Unit> = runCatching {
        client.postgrest["registros_diarios"].insert(registro)
    }
}
