package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alerta(
    val id: String? = null,
    @SerialName("paciente_id") val pacienteId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String = "info",
    val leida: Boolean = false,
<<<<<<< HEAD
    @SerialName("registro_diario_id") val registroDiarioId: String? = null,
=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    @SerialName("created_at") val createdAt: String? = null
)
