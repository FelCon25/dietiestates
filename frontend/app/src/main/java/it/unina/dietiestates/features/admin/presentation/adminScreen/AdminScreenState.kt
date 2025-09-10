package it.unina.dietiestates.features.admin.presentation.adminScreen

import it.unina.dietiestates.features.admin.domain.Agency
import it.unina.dietiestates.features.admin.domain.Agent
import it.unina.dietiestates.features.admin.domain.Assistant

data class AdminScreenState(
    val agency: Agency? = null,
    val assistants: List<Assistant> = emptyList(),
    val agents: List<Agent> = emptyList(),
    val isLoading: Boolean = true
)