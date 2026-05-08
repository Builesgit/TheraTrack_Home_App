package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String,
    val nombre: String,
    val rol: String,
    @SerialName("hospital_id") val hospitalId: String? = null,
<<<<<<< HEAD
    val estado: String = "pendiente",
    val email: String? = null,
    val telefono: String? = null,
    val cargo: String? = null,
    @SerialName("motivo_solicitud") val motivoSolicitud: String? = null,
=======
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    @SerialName("created_at") val createdAt: String? = null
)
