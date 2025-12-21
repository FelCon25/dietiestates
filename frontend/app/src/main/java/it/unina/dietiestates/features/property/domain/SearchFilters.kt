package it.unina.dietiestates.features.property.domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchFilters(
    val address: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val locationSearch: String? = null,
    val insertionType: String? = "SALE",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minSurfaceArea: Double? = null,
    val maxSurfaceArea: Double? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val type: String? = null,
    val propertyCondition: String? = null,
    val elevator: Boolean? = null,
    val airConditioning: Boolean? = null,
    val concierge: Boolean? = null,
    val furnished: Boolean? = null,
    val energyClass: String? = null,
    val agencyId: Int? = null,
    val agentId: Int? = null,
    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc"
)

