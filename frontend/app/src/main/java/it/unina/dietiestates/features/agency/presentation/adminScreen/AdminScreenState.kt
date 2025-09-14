package it.unina.dietiestates.features.agency.presentation.adminScreen

import it.unina.dietiestates.features.agency.domain.Agency
import it.unina.dietiestates.features.agency.domain.Agent
import it.unina.dietiestates.features.agency.domain.Assistant

data class AdminScreenState(
    val agency: Agency? = null,
    val assistants: List<Assistant> = emptyList(),
    val agents: List<Agent> = emptyList(),
    val isLoading: Boolean = true
)