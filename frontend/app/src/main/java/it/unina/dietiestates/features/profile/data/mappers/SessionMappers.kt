package it.unina.dietiestates.features.profile.data.mappers

import it.unina.dietiestates.features.profile.data.dto.SessionDto
import it.unina.dietiestates.features.profile.domain.Session

fun SessionDto.toSession() =
    Session(
        userId = userId,
        userAgent = userAgent,
        createdAt = createdAt,
        expiresAt = expiresAt,
        sessionId = sessionId
    )

fun Session.toSessionDto() =
    SessionDto(
        userId = userId,
        userAgent = userAgent,
        createdAt = createdAt,
        expiresAt = expiresAt,
        sessionId = sessionId
    )