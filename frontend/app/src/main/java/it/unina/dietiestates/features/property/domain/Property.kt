package it.unina.dietiestates.features.property.domain

import it.unina.dietiestates.features.agency.domain.Agency

data class Property(
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
    val furnished: Boolean,
    val propertyType: PropertyType,
    val insertionType: InsertionType,
    val address: String,
    val city: String,
    val postalCode: String,
    val province: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val agentId: Int,
    val propertyCondition: PropertyCondition,
    val createdAt: String,
    val agency: Agency
)