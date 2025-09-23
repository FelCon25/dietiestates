package it.unina.dietiestates.features.property.presentation.addProperty

import it.unina.dietiestates.features.property.domain.Property

sealed class AddPropertyScreenEvent {

    data object OnNavigateToNextPageRequested: AddPropertyScreenEvent()

    data object OnNavigateToPrevPage: AddPropertyScreenEvent()

    data object OnNavigateToNextPage: AddPropertyScreenEvent()

    data class OnWrongValueInput(val message: String): AddPropertyScreenEvent()

    data object OnAddProperty: AddPropertyScreenEvent()

    data class OnPropertyAddedSuccessfully(val property: Property): AddPropertyScreenEvent()

    data object OnPropertyAddingFailed: AddPropertyScreenEvent()
}