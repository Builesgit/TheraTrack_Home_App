package com.example.theratrackhome.controller

import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AdminController {

    private val client = SupabaseClient.client

    /** Lista perfiles con estado='pendiente' del hospital dado. */
    suspend fun obtenerSolicitudesPendientes(hospitalId: String): Result<List<Perfil>> = runCatching {
        client.postgrest["perfiles"].select {
            filter {
                eq("hospital_id", hospitalId)
                eq("estado", "pendiente")
            }
        }.decodeList<Perfil>()
    }

    /**
     * Aprueba un perfil llamando a la RPC aprobar_perfil (SECURITY DEFINER).
     * Evita depender de la política UPDATE de perfiles, que en algunos casos
     * puede fallar si el profesional y el paciente no comparten hospital_id aún.
     */
    suspend fun aprobarSolicitud(perfilId: String): Result<Unit> = runCatching {
        client.postgrest.rpc(
            "aprobar_perfil",
            buildJsonObject { put("p_perfil_id", perfilId) }
        )
    }

    /** Rechaza un perfil llamando a la RPC rechazar_perfil (SECURITY DEFINER). */
    suspend fun rechazarSolicitud(perfilId: String): Result<Unit> = runCatching {
        client.postgrest.rpc(
            "rechazar_perfil",
            buildJsonObject { put("p_perfil_id", perfilId) }
        )
    }

    /**
     * Crea la ficha clínica de un paciente ya aprobado.
     * El profesional llamante debe ser aprobado del mismo hospital (RLS lo garantiza
     * en pacientes_insert).
     */
    suspend fun crearFichaPaciente(
        perfilId: String,
        radiofarmaco: String,
        dosisMbq: Double?,
        fechaTratamiento: String,
        fechaAlta: String,
        diasAislamiento: Int = 7,
        notasClinicas: String? = null
    ): Result<Unit> = runCatching {
        val profesionalId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay profesional autenticado")

        val perfil = client.postgrest["perfiles"].select {
            filter { eq("id", profesionalId) }
        }.decodeSingle<Perfil>()

        val hospitalId = perfil.hospitalId
            ?: throw Exception("El profesional no tiene hospital asignado")

        client.postgrest["pacientes"].insert(
            Paciente(
                id               = perfilId,
                hospitalId       = hospitalId,
                radiofarmaco     = radiofarmaco,
                dosisMbq         = dosisMbq,
                fechaTratamiento = fechaTratamiento,
                fechaAlta        = fechaAlta,
                diasAislamiento  = diasAislamiento,
                notasClinicas    = notasClinicas,
                creadoPor        = profesionalId
            )
        )
    }
}
