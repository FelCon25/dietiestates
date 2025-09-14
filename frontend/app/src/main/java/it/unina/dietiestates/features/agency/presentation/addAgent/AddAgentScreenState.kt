package it.unina.dietiestates.features.agency.presentation.addAgent

data class AddAgentScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false
)
