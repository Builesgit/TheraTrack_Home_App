package com.example.theratrackhome.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hospital(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
