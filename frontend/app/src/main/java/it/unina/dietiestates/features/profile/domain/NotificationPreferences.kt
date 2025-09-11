package it.unina.dietiestates.features.profile.domain

data class NotificationPreferences(
    val enabled: Boolean,
    val category: NotificationType
)

enum class NotificationType{
    NEW_PROPERTY_MATCH,
    PROMOTIONAL
}