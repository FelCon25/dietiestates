package it.unina.dietiestates.features.agency.presentation.assistantScreen

import it.unina.dietiestates.features.agency.domain.Agency
import it.unina.dietiestates.features.agency.domain.Agent

data class AssistantScreenState(
    val agency: Agency? = null,
    val agents: List<Agent> = emptyList(),
    val isLoading: Boolean = true
)