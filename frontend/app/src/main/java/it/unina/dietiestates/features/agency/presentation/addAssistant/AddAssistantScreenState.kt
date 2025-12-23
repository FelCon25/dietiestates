package it.unina.dietiestates.features.agency.presentation.addAssistant

data class AddAssistantScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Field-specific errors
    val emailError: Boolean = false,
    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val passwordError: Boolean = false
)
