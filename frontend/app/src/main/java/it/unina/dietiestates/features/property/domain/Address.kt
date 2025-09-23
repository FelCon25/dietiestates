package it.unina.dietiestates.features.property.domain

data class Address(
    val city: String,
    val province: String,
    val postalCode: String,
    val route: String,
    val streetNumber: String,
    val formatted: String,
    val latitude: Double? = null,
    val longitude: Double? = null
){

    fun isComplete(): Boolean{
        return listOf(
            city,
            province,
            postalCode,
            route,
            streetNumber,
        ).all { it.isNotEmpty() } && latitude != null && longitude != null
    }
}