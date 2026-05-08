package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodigoHospital(
    val id: String,
    @SerialName("hospital_id") val hospitalId: String,
    val codigo: String,
    val usado: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
