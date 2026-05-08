package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistroDiario(
    val id: String? = null,
    @SerialName("paciente_id") val pacienteId: String,
    val fecha: String,
    val temperatura: Double? = null,
    val pulso: Int? = null,
    @SerialName("nivel_fatiga") val nivelFatiga: Int? = null,
    @SerialName("nivel_dolor") val nivelDolor: Int? = null,
    val sintomas: String? = null,
    val notas: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
