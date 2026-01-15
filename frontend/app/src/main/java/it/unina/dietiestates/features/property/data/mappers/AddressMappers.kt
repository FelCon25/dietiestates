package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.GeocodeResultsDto
import it.unina.dietiestates.features.property.domain.Address

fun GeocodeResultsDto.toAddress(): Address{
    // Find the province short code (e.g., "NA" for Naples, "RM" for Rome)
    // Use shortName which matches the database format
    val provinceShort = listOf(
        addressComponents.find { it.types.contains("administrative_area_level_2") }?.shortName,
        addressComponents.find { it.types.contains("administrative_area_level_1") }?.shortName,
    ).firstOrNull { !it.isNullOrBlank() } ?: ""
    
    // Find the city (locality, level 3, or level 2 if nothing else)
    val city = listOf(
        addressComponents.find { it.types.contains("locality") }?.longName,
        addressComponents.find { it.types.contains("administrative_area_level_3") }?.longName,
        addressComponents.find { it.types.contains("administrative_area_level_2") }?.longName,
    ).firstOrNull { !it.isNullOrBlank() } ?: ""
    
    return Address(
        city = city,
        province = provinceShort,
        postalCode = addressComponents.find { it.types.contains("postal_code") }?.longName ?: "",
        route = addressComponents.find { it.types.contains("route") }?.longName ?: "",
        streetNumber = addressComponents.find { it.types.contains("street_number") }?.longName ?: "",
        formatted = formattedAddress,
        latitude = geometry.location.lat,
        longitude = geometry.location.lng
    )
}