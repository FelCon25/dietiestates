package it.unina.dietiestates.features.auth.presentation.register

import it.unina.dietiestates.core.domain.User


sealed class RegisterScreenEvent {
    data class OnRegisterSucceeded(val user: User): RegisterScreenEvent()
    data class OnRegisterFailed(val message: String): RegisterScreenEvent()
    data object OnGoogleAuthFailed: RegisterScreenEvent()
    data class OnWrongValueTextField(val message: String): RegisterScreenEvent()
}