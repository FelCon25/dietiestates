package it.unina.dietiestates.core.presentation

import it.unina.dietiestates.core.domain.User


sealed class MainScreenEvent{
    data class OnSignIn(val user: User): MainScreenEvent()

    data object OnLogout: MainScreenEvent()

    data object OnSendPushNotificationToken: MainScreenEvent()

    data class OnReceivedPushNotification(val propertyId: Int): MainScreenEvent()
    
    data object OnRetryConnection: MainScreenEvent()
}