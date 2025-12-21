package it.unina.dietiestates.features.property.presentation.savedSearches

sealed interface SavedSearchesScreenEvent {
    data object OnRefresh : SavedSearchesScreenEvent
    data class OnError(val message: String) : SavedSearchesScreenEvent
    data class OnDeleteSearch(val searchId: Int) : SavedSearchesScreenEvent
    data object OnSearchDeleted : SavedSearchesScreenEvent
}

