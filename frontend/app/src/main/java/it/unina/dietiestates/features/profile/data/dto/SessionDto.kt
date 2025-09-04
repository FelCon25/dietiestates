package it.unina.dietiestates.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val userId: Int,
    val userAgent: String,
    val createdAt: String,
    val expiresAt: String,
    val sessionId: Int
)