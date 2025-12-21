package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.GeocodeResultsDto
import it.unina.dietiestates.features.property.domain.Address

fun GeocodeResultsDto.toAddress(): Address{
    // Trova la provincia (level 2 o level 1 per regioni)
    val provinceLong = listOf(
        addressComponents.find { it.types.contains("administrative_area_level_2") }?.longName,
        addressComponents.find { it.types.contains("administrative_area_level_1") }?.longName,
    ).firstOrNull { !it.isNullOrBlank() } ?: ""
    
    val provinceClean = provinceLong
        .replace("Province of ", "")
        .replace("Provincia di ", "")
        .replace("Metropolitan City of ", "")
        .replace("Città metropolitana di ", "")
    
    // Trova la città (locality, level 3, o level 2 se non c'è altro)
    val city = listOf(
        addressComponents.find { it.types.contains("locality") }?.longName,
        addressComponents.find { it.types.contains("administrative_area_level_3") }?.longName,
        addressComponents.find { it.types.contains("administrative_area_level_2") }?.longName,
    ).firstOrNull { !it.isNullOrBlank() } ?: ""
    
    return Address(
        city = city,
        province = provinceClean,
        postalCode = addressComponents.find { it.types.contains("postal_code") }?.longName ?: "",
        route = addressComponents.find { it.types.contains("route") }?.longName ?: "",
        streetNumber = addressComponents.find { it.types.contains("street_number") }?.longName ?: "",
        formatted = formattedAddress,
        latitude = geometry.location.lat,
        longitude = geometry.location.lng
    )
}