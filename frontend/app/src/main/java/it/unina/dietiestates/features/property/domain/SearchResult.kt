package it.unina.dietiestates.features.property.domain

data class SearchResult(
    val items: List<Property>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasMore: Boolean
)

