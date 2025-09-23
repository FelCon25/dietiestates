package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.GeocodeResultsDto
import it.unina.dietiestates.features.property.domain.Address

fun GeocodeResultsDto.toAddress(): Address{
    return Address(
        city = listOf(
            addressComponents.find { it.types.contains("locality") }?.longName,
            addressComponents.find { it.types.contains("administrative_area_level_3") }?.longName,
        ).firstOrNull() ?: "",
        province = addressComponents.find { it.types.contains("administrative_area_level_2") }?.longName?.replace("Provincia di ", "") ?: "",
        postalCode = addressComponents.find { it.types.contains("postal_code") }?.longName ?: "",
        route = addressComponents.find { it.types.contains("route") }?.longName ?: "",
        streetNumber = addressComponents.find { it.types.contains("street_number") }?.longName ?: "",
        formatted = formattedAddress,
        latitude = geometry.location.lat,
        longitude = geometry.location.lng
    )
}