package it.unina.dietiestates.features.auth.presentation.login

data class SignInScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)
