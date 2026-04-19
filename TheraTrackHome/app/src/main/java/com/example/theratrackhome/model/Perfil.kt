package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String,
    val nombre: String,
    val rol: String,
    @SerialName("hospital_id") val hospitalId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
