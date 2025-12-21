package it.unina.dietiestates.features.property.presentation.home

sealed interface HomeScreenEvent {
    data object Refresh : HomeScreenEvent
    data class OnLocationReceived(val latitude: Double, val longitude: Double) : HomeScreenEvent
}

