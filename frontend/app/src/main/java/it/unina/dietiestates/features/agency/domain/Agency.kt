package it.unina.dietiestates.features.agency.domain

data class Agency(
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
    val createdAt: String,
    val updatedAt: String,
    val agencyAdminId: Int
)

fun getEmptyAgency() = Agency(
    agencyId = 0,
    businessName = "",
    legalName = "",
    vatNumber = "",
    email = "",
    pec = "",
    phone = "",
    website = "",
    address = "",
    city = "",
    postalCode = "",
    province = "",
    country = "",
    latitude = "",
    longitude = "",
    createdAt = "",
    updatedAt = "",
    agencyAdminId = 0
)