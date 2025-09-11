package it.unina.dietiestates.features.profile.presentation

sealed class ProfileScreenEvent {
    data class OnLogoutFailed(val message: String): ProfileScreenEvent()
    data class OnSendPasswordResetFailed(val message: String): ProfileScreenEvent()
    data class OnChangingNotificationStatusFailed(val message: String): ProfileScreenEvent()
    data class OnDeletingSessionRequested(val sessionId: Int): ProfileScreenEvent()
    data class OnDeletingSessionFailed(val message: String): ProfileScreenEvent()
    data object OnDeletingSessionConfirmed: ProfileScreenEvent()
    data object OnDeletingSessionCanceled: ProfileScreenEvent()
}