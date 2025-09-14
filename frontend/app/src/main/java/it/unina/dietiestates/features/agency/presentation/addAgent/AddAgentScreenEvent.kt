package it.unina.dietiestates.features.agency.presentation.addAgent

import it.unina.dietiestates.features.agency.domain.Agent

sealed class AddAgentScreenEvent{
    data class OnAddAgentSucceeded(val agent: Agent): AddAgentScreenEvent()
    data class OnAddAgentFailed(val message: String): AddAgentScreenEvent()
    data class OnWrongValueTextField(val message: String): AddAgentScreenEvent()
}