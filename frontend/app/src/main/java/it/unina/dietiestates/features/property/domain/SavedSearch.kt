package it.unina.dietiestates.features.property.domain

data class SavedSearch(
    val searchId: Int,
    val userId: Int,
    val name: String,
    val filters: SearchFilters
)

