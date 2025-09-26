package it.unina.dietiestates.features.property.presentation.propertyDetails

import it.unina.dietiestates.features.property.domain.Property

data class PropertyDetailsScreenState(
    val property: Property? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)