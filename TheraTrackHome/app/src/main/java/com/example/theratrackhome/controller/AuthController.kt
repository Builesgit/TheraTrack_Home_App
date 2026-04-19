package com.example.theratrackhome.controller

import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AuthController {

    private val client = SupabaseClient.client

    // Inicia sesión con email y contraseña
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    // Registra un nuevo paciente: valida código → crea auth user → crea perfil → marca código usado
    suspend fun register(
        nombre: String,
        email: String,
        codigoHospital: String,
        password: String
    ): Result<Unit> = runCatching {

        // 1. Verificar que el código existe y no ha sido usado
        val codigos = client.postgrest["codigos_hospital"].select {
            filter {
                eq("codigo", codigoHospital.trim().uppercase())
                eq("usado", false)
            }
        }.decodeList<CodigoHospital>()

        if (codigos.isEmpty()) throw Exception("Código de hospital no válido o ya utilizado")
        val codigo = codigos.first()

        // 2. Crear usuario en Supabase Auth
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("Error al obtener el usuario tras el registro")

        // 3. Crear el perfil en la tabla perfiles
        client.postgrest["perfiles"].insert(
            Perfil(
                id = userId,
                nombre = nombre,
                rol = "paciente",
                hospitalId = codigo.hospitalId
            )
        )

        // 4. Marcar el código como usado
        client.postgrest["codigos_hospital"].update({
            set("usado", true)
        }) {
            filter {
                eq("id", codigo.id)
            }
        }
    }

    // Cierra la sesión actual
    suspend fun logout(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    // Comprueba si hay sesión activa (sin llamada a red)
    fun sesionActiva(): Boolean {
        return client.auth.currentSessionOrNull() != null
    }

    // Obtiene el rol del usuario actual desde la tabla perfiles
    suspend fun obtenerRolActual(): Result<String> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay usuario autenticado")

        val perfil = client.postgrest["perfiles"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Perfil>()

        perfil.rol
    }
}

// Clase interna solo para leer la tabla codigos_hospital durante el registro
@Serializable
private data class CodigoHospital(
    val id: String,
    val codigo: String,
    @SerialName("hospital_id") val hospitalId: String,
    val usado: Boolean
)
