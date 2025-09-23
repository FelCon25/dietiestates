package it.unina.dietiestates.features.profile.presentation

import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.profile.domain.NotificationCategory
import it.unina.dietiestates.features.profile.domain.Session

data class ProfileScreenState(
    val user: User? = null,
    val sessions: List<Session> = emptyList(),
    val currentSessionId: Int? = null,
    val notificationPreferences: List<NotificationCategory> = emptyList(),
    val isLoading: Boolean = true,
    val isPropertyNotificationStatusChanging: Boolean = false,
    val isPromotionalNotificationStatusChanging: Boolean = false,
    val sessionToDelete: Int? = null
)