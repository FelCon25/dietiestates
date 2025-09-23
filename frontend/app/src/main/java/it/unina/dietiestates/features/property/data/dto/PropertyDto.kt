package it.unina.dietiestates.features.property.data.dto

import it.unina.dietiestates.features.agency.data.dto.AgencyDto
import kotlinx.serialization.Serializable


@Serializable
data class PropertyDto(
    val propertyId: Int,
    val agencyId: Int,
    val description: String,
    val price: Double,
    val surfaceArea: Int,
    val rooms: Int,
    val floors: Int,
    val elevator: Boolean,
    val energyClass: String,
    val concierge: Boolean,
    val airConditioning: Boolean,
    val insertionType: String,
    val propertyType: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val province: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val agentId: Int,
    val furnished: Boolean,
    val propertyCondition: String,
    val createdAt: String,
    val images: List<String>,
    val agency: AgencyDto
)