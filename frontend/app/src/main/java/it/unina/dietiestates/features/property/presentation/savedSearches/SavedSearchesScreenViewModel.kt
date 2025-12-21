package it.unina.dietiestates.features.property.presentation.savedSearches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedSearchesScreenViewModel(
    private val repository: PropertyRepository
): ViewModel() {

    private val _state = MutableStateFlow(SavedSearchesScreenState())
    val state = _state.asStateFlow()

    private val _eventsChannel = Channel<SavedSearchesScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    init {
        getSavedSearches()
    }

    fun onEvent(event: SavedSearchesScreenEvent) {
        when(event) {
            is SavedSearchesScreenEvent.OnRefresh -> {
                _state.update {
                    it.copy(isRefreshing = true)
                }
                getSavedSearches()
            }

            is SavedSearchesScreenEvent.OnDeleteSearch -> {
                deleteSearch(event.searchId)
            }

            is SavedSearchesScreenEvent.OnError -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is SavedSearchesScreenEvent.OnSearchDeleted -> {
                // This event is only emitted by the ViewModel, not received from outside
                // No action needed here
            }
        }
    }

    private fun getSavedSearches() {
        viewModelScope.launch {
            repository.getSavedSearches().collect { result ->
                result.apply {
                    onSuccess { searches ->
                        _state.update {
                            it.copy(
                                savedSearches = searches,
                                isRefreshing = false
                            )
                        }
                    }

                    onError {
                        _state.update {
                            it.copy(isRefreshing = false)
                        }
                        onEvent(SavedSearchesScreenEvent.OnError("Unable to retrieve saved searches, please try again."))
                    }

                    onLoading { isLoading ->
                        _state.update {
                            it.copy(isLoading = isLoading)
                        }
                    }
                }
            }
        }
    }

    private fun deleteSearch(searchId: Int) {
        viewModelScope.launch {
            repository.deleteSavedSearch(searchId).collect { result ->
                result.apply {
                    onSuccess {
                        getSavedSearches()
                        viewModelScope.launch {
                            _eventsChannel.send(SavedSearchesScreenEvent.OnSearchDeleted)
                        }
                    }

                    onError {
                        onEvent(SavedSearchesScreenEvent.OnError("Unable to delete search, please try again."))
                    }
                }
            }
        }
    }
}