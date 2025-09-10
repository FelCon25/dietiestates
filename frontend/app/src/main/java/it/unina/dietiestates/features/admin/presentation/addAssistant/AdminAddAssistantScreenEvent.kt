package it.unina.dietiestates.features.admin.presentation.addAssistant

import it.unina.dietiestates.features.admin.domain.Assistant

sealed class AdminAddAssistantScreenEvent{
    data class OnAddAssistantSucceeded(val assistant: Assistant): AdminAddAssistantScreenEvent()
    data class OnAddAssistantFailed(val message: String): AdminAddAssistantScreenEvent()
    data class OnWrongValueTextField(val message: String): AdminAddAssistantScreenEvent()
}