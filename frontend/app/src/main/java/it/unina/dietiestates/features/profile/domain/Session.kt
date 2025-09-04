package it.unina.dietiestates.features.profile.domain

data class Session(
    val userId: Int,
    val userAgent: String,
    val createdAt: String,
    val expiresAt: String,
    val sessionId: Int
)
