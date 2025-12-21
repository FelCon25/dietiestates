package it.unina.dietiestates.features.agency.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AgencyDto(
    val agencyId: Int,
    val businessName: String,
    val legalName: String,
    val vatNumber: String,
    val email: String,
    val pec: String?,
    val phone: String?,
    val website: String?,
    val address: String,
    val city: String,
    val postalCode: String,
    val province: String,
    val country: String,
    val latitude: String?,
    val longitude: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val agencyAdminId: Int? = null
)