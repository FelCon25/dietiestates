package it.unina.dietiestates.features.profile.presentation

import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.profile.domain.NotificationPreferences
import it.unina.dietiestates.features.profile.domain.Session

data class ProfileScreenState(
    val user: User? = null,
    val sessions: List<Session> = emptyList(),
    val currentSessionId: Int? = null,
    val notificationPreferences: List<NotificationPreferences> = emptyList(),
    val isLoading: Boolean = true
)