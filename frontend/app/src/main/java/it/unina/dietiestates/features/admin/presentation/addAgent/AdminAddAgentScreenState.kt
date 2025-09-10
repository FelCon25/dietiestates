package it.unina.dietiestates.features.admin.presentation.addAgent

data class AdminAddAgentScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false
)
