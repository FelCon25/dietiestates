package it.unina.dietiestates.features.auth.presentation.login

import it.unina.dietiestates.core.domain.User

sealed class SignInScreenEvent {
    data class OnSignInSucceeded(val user: User): SignInScreenEvent()
    data class OnSignFailed(val message: String): SignInScreenEvent()
    data object OnGoogleAuthFailed: SignInScreenEvent()
    data class OnWrongValueTextField(val message: String): SignInScreenEvent()
}