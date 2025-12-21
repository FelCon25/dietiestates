package it.unina.dietiestates.features.property.domain

import kotlinx.serialization.Serializable

@Serializable
data class NearbyFilters(
    val insertionType: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minSurfaceArea: Int? = null,
    val maxSurfaceArea: Int? = null,
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
    val agentId: Int? = null
) 