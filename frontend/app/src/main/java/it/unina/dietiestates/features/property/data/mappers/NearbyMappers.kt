package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.NearbyPinDto
import it.unina.dietiestates.features.property.domain.NearbyPin

fun NearbyPinDto.toNearbyPin(): NearbyPin = NearbyPin(
    propertyId = propertyId,
    latitude = latitude,
    longitude = longitude,
    price = price,
    insertionType = insertionType,
    distanceMeters = distanceMeters
) 