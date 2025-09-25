package it.unina.dietiestates.features.property.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NearbyPinDto(
    val propertyId: Int,
    val latitude: Double,
    val longitude: Double,
    val price: Double,
    val insertionType: String,
    val distanceMeters: Double
) 