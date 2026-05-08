package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Paciente(
    val id: String,
    @SerialName("hospital_id") val hospitalId: String,
    val radiofarmaco: String,
    @SerialName("dosis_mbq") val dosisMbq: Double? = null,
    @SerialName("fecha_tratamiento") val fechaTratamiento: String,
    @SerialName("fecha_alta") val fechaAlta: String,
    @SerialName("dias_aislamiento") val diasAislamiento: Int = 7,
<<<<<<< HEAD
    @SerialName("notas_clinicas") val notasClinicas: String? = null,
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("created_at") val createdAt: String? = null
=======
    @SerialName("notas_clinicas") val notasClinivas: String? = null
>>>>>>> a73c1ab7e3afb368f22b6b676a1d3ee461c66f31
)
