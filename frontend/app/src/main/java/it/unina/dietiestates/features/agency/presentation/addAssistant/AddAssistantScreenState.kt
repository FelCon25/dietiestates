package it.unina.dietiestates.features.agency.presentation.addAssistant

data class AddAssistantScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false
)