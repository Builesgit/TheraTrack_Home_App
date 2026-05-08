package com.example.theratrackhome.controller

import com.example.theratrackhome.model.Alerta
<<<<<<< HEAD
import com.example.theratrackhome.model.Hospital
=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.model.Recomendacion
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

object PacienteController {

    private val client = SupabaseClient.client

<<<<<<< HEAD
    /**
     * Devuelve la ficha clínica del paciente autenticado.
     * Si el perfil está aprobado pero el hospital aún no ha creado la ficha,
     * devuelve Result.failure con un mensaje explicativo en lugar de un error genérico.
     */
=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun obtenerPacienteActual(): Result<Paciente> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay usuario autenticado")

<<<<<<< HEAD
        val lista = client.postgrest["pacientes"].select {
            filter { eq("id", userId) }
        }.decodeList<Paciente>()

        if (lista.isEmpty()) throw Exception(
            "Tu ficha clínica aún no ha sido creada por el hospital. " +
            "Contacta con tu equipo médico."
        )

        lista.first()
=======
        client.postgrest["pacientes"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Paciente>()
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
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

<<<<<<< HEAD
    suspend fun obtenerHospitalPorId(hospitalId: String): Result<Hospital> = runCatching {
        client.postgrest["hospitales"].select {
            filter { eq("id", hospitalId) }
        }.decodeSingle<Hospital>()
    }

    /** Devuelve la lista de todos los hospitales registrados (usado para Spinners de registro). */
    suspend fun obtenerHospitales(): Result<List<Hospital>> = runCatching {
        client.postgrest["hospitales"].select().decodeList<Hospital>()
    }

=======
    // Hospital-panel functions
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
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

<<<<<<< HEAD
    suspend fun obtenerPerfilPorId(perfilId: String): Result<Perfil> = runCatching {
        client.postgrest["perfiles"].select {
            filter { eq("id", perfilId) }
        }.decodeSingle<Perfil>()
    }

=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun obtenerAlertasPaciente(pacienteId: String): Result<List<Alerta>> = runCatching {
        client.postgrest["alertas"].select {
            filter { eq("paciente_id", pacienteId) }
        }.decodeList<Alerta>()
    }

<<<<<<< HEAD
    suspend fun obtenerAlertasPacienteDesde(pacienteId: String, desdeIso: String): Result<List<Alerta>> = runCatching {
        client.postgrest["alertas"].select {
            filter {
                eq("paciente_id", pacienteId)
                gte("created_at", desdeIso)
            }
        }.decodeList<Alerta>()
    }

    suspend fun obtenerAlertasPacienteEnRango(pacienteId: String, desde: String, hasta: String): Result<List<Alerta>> = runCatching {
        client.postgrest["alertas"].select {
            filter {
                eq("paciente_id", pacienteId)
                gte("created_at", "${desde}T00:00:00")
                lte("created_at", "${hasta}T23:59:59")
            }
        }.decodeList<Alerta>()
    }

=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
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

<<<<<<< HEAD
    suspend fun obtenerAlertasHospitalDesde(hospitalId: String, desdeIso: String): Result<List<Alerta>> = runCatching {
        val pacientes = obtenerPacientesHospital(hospitalId).getOrThrow()
        val resultado = mutableListOf<Alerta>()
        for (p in pacientes) {
            val alertas = client.postgrest["alertas"].select {
                filter {
                    eq("paciente_id", p.id)
                    gte("created_at", desdeIso)
                }
            }.decodeList<Alerta>()
            resultado.addAll(alertas)
        }
        resultado.sortedByDescending { it.createdAt }
    }

=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun marcarAlertaLeida(alertaId: String): Result<Unit> = runCatching {
        client.postgrest["alertas"].update({ set("leida", true) }) {
            filter { eq("id", alertaId) }
        }
    }

<<<<<<< HEAD
    suspend fun obtenerRecomendacionesPaciente(hospitalId: String): Result<List<Recomendacion>> = runCatching {
        client.postgrest["recomendaciones"].select {
            filter { eq("hospital_id", hospitalId) }
        }.decodeList<Recomendacion>()
    }

    suspend fun obtenerRecomendacionesPacienteDesde(hospitalId: String, pacienteId: String, desdeIso: String): Result<List<Recomendacion>> = runCatching {
        val recomendaciones = client.postgrest["recomendaciones"].select {
            filter {
                eq("hospital_id", hospitalId)
                gte("created_at", desdeIso)
            }
        }.decodeList<Recomendacion>()

        recomendaciones.filter { it.pacienteId == null || it.pacienteId == pacienteId }
    }

    suspend fun marcarRecomendacionLeida(id: String): Result<Unit> = runCatching {
        client.postgrest["recomendaciones"].update({ set("leida", true) }) {
            filter { eq("id", id) }
        }
    }

=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun cerrarSesion() {
        runCatching { client.auth.signOut() }
    }
}

@Serializable
private data class HospitalNombre(val nombre: String)
