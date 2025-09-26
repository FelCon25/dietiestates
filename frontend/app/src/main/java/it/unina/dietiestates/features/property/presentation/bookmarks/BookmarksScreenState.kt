package it.unina.dietiestates.features.property.presentation.bookmarks

import it.unina.dietiestates.features.property.domain.Property

data class BookmarksScreenState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)
