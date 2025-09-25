package it.unina.dietiestates.features.property.domain

data class NearbyPin(
    val propertyId: Int,
    val latitude: Double,
    val longitude: Double,
    val price: Double,
    val insertionType: String,
    val distanceMeters: Double
) 