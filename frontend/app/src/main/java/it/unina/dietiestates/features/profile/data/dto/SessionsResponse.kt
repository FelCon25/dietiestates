package it.unina.dietiestates.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionsResponse(
    val sessions: List<SessionDto>,
    val currentSessionId: Int
)
