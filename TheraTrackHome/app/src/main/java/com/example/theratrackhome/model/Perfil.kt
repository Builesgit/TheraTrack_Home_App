package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String,
    val nombre: String,
    val rol: String,
    @SerialName("hospital_id") val hospitalId: String? = null,
    val estado: String = "pendiente",
    val email: String? = null,
    val telefono: String? = null,
    val cargo: String? = null,
    @SerialName("motivo_solicitud") val motivoSolicitud: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
