package it.unina.dietiestates.features.property.presentation.savedSearches

import it.unina.dietiestates.features.property.domain.SavedSearch

data class SavedSearchesScreenState(
    val savedSearches: List<SavedSearch> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val showSaveDialog: Boolean = false,
    val searchNameInput: String = ""
)

