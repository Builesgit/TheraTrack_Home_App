package com.example.theratrackhome.controller

<<<<<<< HEAD
import android.util.Log
=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
<<<<<<< HEAD
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AuthController {

    /** Lanzada cuando las credenciales son correctas pero la cuenta está pendiente de aprobación. */
    class CuentaPendienteException : Exception("pendiente")

    private const val TAG = "AuthController"

    private val client = SupabaseClient.client

    // Para pruebas académicas/dev, desactivar en Supabase Dashboard:
    // Authentication -> Providers -> Email -> Confirm email = OFF.
    // Si está activo, Supabase Auth intentará enviar correos en signUpWith() y puede bloquear
    // el registro por rate limit; además no habrá sesión activa inmediatamente tras crear usuario.

    private const val EMAIL_RATE_LIMIT_MESSAGE =
        "Supabase está intentando enviar correos. Desactiva Confirm email en Authentication > Providers > Email o espera al rate limit."

    private const val NO_ACTIVE_SESSION_AFTER_SIGNUP_MESSAGE =
        "El usuario se ha creado pero no hay sesión activa. Desactiva Confirm email en Supabase para pruebas."

    private fun isEmailRateLimitError(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return message.contains("over_email_send_rate_limit") ||
            message.contains("email rate limit exceeded")
    }

    private fun signupError(error: Throwable): Exception {
        return if (isEmailRateLimitError(error)) {
            Exception(EMAIL_RATE_LIMIT_MESSAGE, error)
        } else {
            Exception(error.message ?: "Error al registrar usuario", error)
        }
    }

    /**
     * Inicia sesión.
     * - Result.success         → estado = 'aprobado'
     * - CuentaPendienteException → estado = 'pendiente'
     * - Exception("rechazado") → estado = 'rechazado'
     */
=======
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AuthController {

    private val client = SupabaseClient.client

    // Inicia sesión con email y contraseña
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
<<<<<<< HEAD

        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("Error al obtener la sesión")
=======
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
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31

        val perfil = client.postgrest["perfiles"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Perfil>()

<<<<<<< HEAD
        when (perfil.estado) {
            "pendiente" -> throw CuentaPendienteException()
            "rechazado" -> {
                client.auth.signOut()
                throw Exception("Tu solicitud fue rechazada. Contacta con el hospital.")
            }
        }
    }

    /**
     * Registra un nuevo paciente con código de hospital.
     * Flujo:
     *  1. validar_codigo  → obtiene hospital_id (no marca el código aún)
     *  2. signUpWith       → crea cuenta en Auth
     *  3. crear_perfil     → inserta la fila en perfiles (estado='pendiente')
     *  4. usar_codigo      → marca el código como usado
     */
    suspend fun registrarPaciente(
        nombre: String,
        email: String,
        password: String,
        codigoHospital: String,
        identificador: String = "",
        tipoIdentificador: String = "cipa"
    ): Result<Unit> = runCatching {
        // 1. Validar código y obtener hospital_id (callable por anon, SECURITY DEFINER)
        val hospitalId = client.postgrest.rpc(
            "validar_codigo",
            buildJsonObject { put("codigo_text", codigoHospital.trim().uppercase()) }
        ).decodeAs<String>()
        Log.d(TAG, "registrarPaciente: validar_codigo OK")

        // 2. Crear usuario en Auth
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("rol", "paciente")
                    put("hospital_id", hospitalId)
                }
            }
            Log.d(TAG, "registrarPaciente: signUpWith OK")
        } catch (error: Throwable) {
            Log.e(TAG, "registrarPaciente: signUpWith falló", error)
            throw signupError(error)
        }

        // 3. Crear perfil manualmente (SECURITY DEFINER, bypasa RLS)
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception(NO_ACTIVE_SESSION_AFTER_SIGNUP_MESSAGE)
        Log.d(TAG, "registrarPaciente: currentUser OK")

        client.postgrest.rpc(
            "crear_perfil",
            buildJsonObject {
                put("p_id", userId)
                put("p_nombre", nombre)
                put("p_rol", "paciente")
                put("p_hospital_id", hospitalId)
                put("p_email", email)
                if (identificador.isNotBlank()) put("p_identificador", identificador)
                if (tipoIdentificador.isNotBlank()) put("p_tipo_identificador", tipoIdentificador)
            }
        )
        Log.d(TAG, "registrarPaciente: crear_perfil OK")

        // 4. Marcar el código como usado (ahora sí, usuario ya creado)
        client.postgrest.rpc(
            "usar_codigo",
            buildJsonObject { put("codigo_text", codigoHospital.trim().uppercase()) }
        )
        Log.d(TAG, "registrarPaciente: usar_codigo OK")
    }

    /**
     * Solicita acceso como profesional sanitario.
     * No usa código de hospital (lo selecciona del Spinner de hospitales).
     * Flujo:
     *  1. signUpWith   → crea cuenta en Auth
     *  2. crear_perfil → inserta perfil con estado='pendiente'
     */
    suspend fun registrarProfesional(
        nombre: String,
        email: String,
        password: String,
        hospitalId: String,
        cargo: String,
        telefono: String,
        motivoSolicitud: String,
        identificador: String = "",
        tipoIdentificador: String = "colegiado"
    ): Result<Unit> = runCatching {
        // 1. Crear usuario en Auth
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("rol", "profesional")
                    put("hospital_id", hospitalId)
                    put("cargo", cargo)
                    put("telefono", telefono)
                    put("motivo_solicitud", motivoSolicitud)
                }
            }
            Log.d(TAG, "registrarProfesional: signUpWith OK")
        } catch (error: Throwable) {
            Log.e(TAG, "registrarProfesional: signUpWith falló", error)
            throw signupError(error)
        }

        // 2. Crear perfil manualmente (SECURITY DEFINER)
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception(NO_ACTIVE_SESSION_AFTER_SIGNUP_MESSAGE)
        Log.d(TAG, "registrarProfesional: currentUser OK")

        client.postgrest.rpc(
            "crear_perfil",
            buildJsonObject {
                put("p_id", userId)
                put("p_nombre", nombre)
                put("p_rol", "profesional")
                put("p_hospital_id", hospitalId)
                put("p_email", email)
                put("p_cargo", cargo)
                put("p_telefono", telefono)
                put("p_motivo", motivoSolicitud)
                if (identificador.isNotBlank()) put("p_identificador", identificador)
                if (tipoIdentificador.isNotBlank()) put("p_tipo_identificador", tipoIdentificador)
            }
        )
        Log.d(TAG, "registrarProfesional: crear_perfil OK")
    }

    /** Cierra la sesión actual. */
    suspend fun logout(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    /** Devuelve true si hay sesión activa (sin llamada a red). */
    fun sesionActiva(): Boolean = client.auth.currentSessionOrNull() != null

    /** Devuelve el perfil completo del usuario autenticado. */
    suspend fun obtenerPerfilActual(): Result<Perfil> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("No hay usuario autenticado")

        client.postgrest["perfiles"].select {
            filter { eq("id", userId) }
        }.decodeSingle<Perfil>()
    }

    /** Devuelve el rol ('paciente' o 'profesional') del usuario autenticado. */
    suspend fun obtenerRolActual(): Result<String> = runCatching {
        obtenerPerfilActual().getOrThrow().rol
    }
}
=======
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
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
