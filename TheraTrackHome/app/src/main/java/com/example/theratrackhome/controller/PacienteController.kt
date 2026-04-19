package com.example.theratrackhome.controller

import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.model.Recomendacion
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

object PacienteController {

    private val client = SupabaseClient.client

    suspend fun obtenerPacienteActual(): Result<Paciente> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay usuario autenticado")

        client.postgrest["pacientes"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Paciente>()
    }

    suspend fun obtenerPerfilActual(): Result<Perfil> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay usuario autenticado")

        client.postgrest["perfiles"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Perfil>()
    }

    suspend fun obtenerRecomendaciones(hospitalId: String): Result<List<Recomendacion>> = runCatching {
        client.postgrest["recomendaciones"].select {
            filter { eq("hospital_id", hospitalId) }
        }.decodeList<Recomendacion>()
    }

    suspend fun obtenerNombreHospital(hospitalId: String): Result<String> = runCatching {
        client.postgrest["hospitales"].select {
            filter { eq("id", hospitalId) }
        }.decodeSingle<HospitalNombre>().nombre
    }

    // Hospital-panel functions
    suspend fun obtenerPacientesHospital(hospitalId: String): Result<List<Paciente>> = runCatching {
        client.postgrest["pacientes"].select {
            filter { eq("hospital_id", hospitalId) }
        }.decodeList<Paciente>()
    }

    suspend fun obtenerAlertasNoLeidasPacientes(pacienteIds: List<String>): Result<List<Alerta>> = runCatching {
        val resultado = mutableListOf<Alerta>()
        for (pid in pacienteIds) {
            val alertas = client.postgrest["alertas"].select {
                filter {
                    eq("paciente_id", pid)
                    eq("leida", false)
                }
            }.decodeList<Alerta>()
            resultado.addAll(alertas)
        }
        resultado
    }

    suspend fun obtenerPacientePorId(pacienteId: String): Result<Paciente> = runCatching {
        client.postgrest["pacientes"].select {
            filter { eq("id", pacienteId) }
        }.decodeSingle<Paciente>()
    }

    suspend fun obtenerAlertasPaciente(pacienteId: String): Result<List<Alerta>> = runCatching {
        client.postgrest["alertas"].select {
            filter { eq("paciente_id", pacienteId) }
        }.decodeList<Alerta>()
    }

    suspend fun obtenerTodasAlertasHospital(hospitalId: String): Result<List<Alerta>> = runCatching {
        val pacientes = obtenerPacientesHospital(hospitalId).getOrThrow()
        val resultado = mutableListOf<Alerta>()
        for (p in pacientes) {
            val alertas = client.postgrest["alertas"].select {
                filter { eq("paciente_id", p.id) }
            }.decodeList<Alerta>()
            resultado.addAll(alertas)
        }
        resultado.sortedByDescending { it.createdAt }
    }

    suspend fun marcarAlertaLeida(alertaId: String): Result<Unit> = runCatching {
        client.postgrest["alertas"].update({ set("leida", true) }) {
            filter { eq("id", alertaId) }
        }
    }

    suspend fun cerrarSesion() {
        runCatching { client.auth.signOut() }
    }
}

@Serializable
private data class HospitalNombre(val nombre: String)
