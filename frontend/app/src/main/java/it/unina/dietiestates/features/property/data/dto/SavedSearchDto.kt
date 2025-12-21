package it.unina.dietiestates.features.property.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SavedSearchDto(
    val searchId: Int,
    val userId: Int,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null,
    val minSurfaceArea: Int? = null,
    val maxSurfaceArea: Int? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val propertyCondition: String? = null,
    val elevator: Boolean? = null,
    val airConditioning: Boolean? = null,
    val concierge: Boolean? = null,
    val energyClass: String? = null,
    val furnished: Boolean? = null,
    val propertyType: String? = null,
    val insertionType: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null
)

@Serializable
data class CreateSavedSearchDto(
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null,
    val minSurfaceArea: Int? = null,
    val maxSurfaceArea: Int? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val propertyCondition: String? = null,
    val elevator: Boolean? = null,
    val airConditioning: Boolean? = null,
    val concierge: Boolean? = null,
    val energyClass: String? = null,
    val furnished: Boolean? = null,
    val propertyType: String? = null,
    val insertionType: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null
)

