package it.unina.dietiestates.features.auth.presentation.register

data class RegisterScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)