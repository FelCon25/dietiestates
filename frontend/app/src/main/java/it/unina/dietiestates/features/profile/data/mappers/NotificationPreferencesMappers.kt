package it.unina.dietiestates.features.profile.data.mappers

import it.unina.dietiestates.features.profile.data.dto.NotificationPreferencesDto
import it.unina.dietiestates.features.profile.domain.NotificationPreferences

fun NotificationPreferencesDto.toNotificationPreferences() =
    NotificationPreferences(
        enabled = enabled,
        category = category
    )

fun NotificationPreferences.toNotificationPreferencesDto() =
    NotificationPreferencesDto(
        enabled = enabled,
        category = category
    )