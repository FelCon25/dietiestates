package it.unina.dietiestates.features.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesDto(
    val category: String
)