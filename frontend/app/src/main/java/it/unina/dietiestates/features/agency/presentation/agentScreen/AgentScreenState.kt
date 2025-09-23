package it.unina.dietiestates.features.agency.presentation.agentScreen

import it.unina.dietiestates.features.agency.domain.Agency
import it.unina.dietiestates.features.property.domain.Property

data class AgentScreenState(
    val agency: Agency? = null,
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = true
)