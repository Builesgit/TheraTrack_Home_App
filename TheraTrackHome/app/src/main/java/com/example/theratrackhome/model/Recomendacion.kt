package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recomendacion(
    val id: String? = null,
    @SerialName("hospital_id") val hospitalId: String,
    val titulo: String,
    val contenido: String,
    val tipo: String,
    val prioridad: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
