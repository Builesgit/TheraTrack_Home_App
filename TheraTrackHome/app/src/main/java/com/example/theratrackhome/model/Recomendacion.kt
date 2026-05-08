package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recomendacion(
    val id: String? = null,
<<<<<<< HEAD
    @SerialName("hospital_id") val hospitalId: String? = null,
    @SerialName("paciente_id") val pacienteId: String? = null,
    val titulo: String,
    @SerialName("contenido") val descripcion: String,
    val tipo: String,
    val prioridad: Int = 0,
    val leida: Boolean = false,
    @SerialName("creado_por") val creadoPor: String? = null,
=======
    @SerialName("hospital_id") val hospitalId: String,
    val titulo: String,
    val contenido: String,
    val tipo: String,
    val prioridad: Int = 0,
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
    @SerialName("created_at") val createdAt: String? = null
)
