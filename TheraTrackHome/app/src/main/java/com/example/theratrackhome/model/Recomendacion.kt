package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recomendacion(
    val id: String? = null,
    @SerialName("hospital_id") val hospitalId: String? = null,
    @SerialName("paciente_id") val pacienteId: String? = null,
    val titulo: String,
    @SerialName("contenido") val descripcion: String,
    val tipo: String,
    val prioridad: Int = 0,
    val leida: Boolean = false,
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
