package it.unina.dietiestates.features.agency.presentation.addAssistant

import it.unina.dietiestates.features.agency.domain.Assistant

sealed class AddAssistantScreenEvent{
    data class OnAddAssistantSucceeded(val assistant: Assistant): AddAssistantScreenEvent()
    data class OnAddAssistantFailed(val message: String): AddAssistantScreenEvent()
    data class OnWrongValueTextField(val message: String): AddAssistantScreenEvent()
}