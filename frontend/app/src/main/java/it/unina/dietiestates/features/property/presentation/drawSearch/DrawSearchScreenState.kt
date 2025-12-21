package it.unina.dietiestates.features.property.presentation.drawSearch

import it.unina.dietiestates.core.domain.location.Location
import it.unina.dietiestates.features.property.domain.NearbyPin
import it.unina.dietiestates.features.property.domain.Property

data class DrawSearchScreenState(
    val pins: List<NearbyPin> = emptyList(),
    val isLoading: Boolean = false,
    val selectedProperty: Property? = null,
    val isLoadingProperty: Boolean = false,
    val currentLocation: Location? = null,
    val isLoadingLocation: Boolean = false
)

