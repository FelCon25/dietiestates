package it.unina.dietiestates.features.property.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val items: List<PropertyDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasMore: Boolean
)

