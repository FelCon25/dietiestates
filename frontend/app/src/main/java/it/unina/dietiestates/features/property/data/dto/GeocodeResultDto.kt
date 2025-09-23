package it.unina.dietiestates.features.property.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodeResponse(
    val results: List<GeocodeResultsDto>
)

@Serializable
data class GeocodeResultsDto(
    @SerialName("address_components")
    val addressComponents: List<AddressComponentDto>,
    @SerialName("formatted_address")
    val formattedAddress: String,
    val geometry: GeometryDto
)

@Serializable
data class AddressComponentDto(
    @SerialName("long_name")
    val longName: String,
    @SerialName("short_name")
    val shortName: String,
    val types: List<String>
)

@Serializable
data class GeometryDto(
    val location: LocationDto
)

@Serializable
data class LocationDto(
    val lat: Double,
    val lng: Double
)