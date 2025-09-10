package it.unina.dietiestates.features.admin.presentation.addAgent

import it.unina.dietiestates.features.admin.domain.Agent

sealed class AdminAddAgentScreenEvent{
    data class OnAddAgentSucceeded(val agent: Agent): AdminAddAgentScreenEvent()
    data class OnAddAgentFailed(val message: String): AdminAddAgentScreenEvent()
    data class OnWrongValueTextField(val message: String): AdminAddAgentScreenEvent()
}