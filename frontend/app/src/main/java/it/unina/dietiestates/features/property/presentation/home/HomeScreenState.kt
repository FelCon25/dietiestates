package it.unina.dietiestates.features.property.presentation.home

import it.unina.dietiestates.features.property.domain.Property

data class HomeScreenState(
    // Nearby properties for sale (horizontal scroll)
    val nearbyForSale: List<Property> = emptyList(),
    val isLoadingForSale: Boolean = false,
    
    // Nearby properties for rent (horizontal scroll)
    val nearbyForRent: List<Property> = emptyList(),
    val isLoadingForRent: Boolean = false,
    
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    
    val error: String? = null
)

