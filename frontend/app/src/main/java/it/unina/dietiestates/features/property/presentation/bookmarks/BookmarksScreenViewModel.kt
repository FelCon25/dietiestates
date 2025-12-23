package it.unina.dietiestates.features.property.presentation.bookmarks

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

class BookmarksScreenViewModel(
    private val repository: PropertyRepository
): ViewModel() {

    private val _state = MutableStateFlow(BookmarksScreenState())
    val state = _state.asStateFlow()

    private val _eventsChannel = Channel<BookmarksScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    init {
        getSavedProperties()
    }

    fun onEvent(event: BookmarksScreenEvent){
        when(event){
            is BookmarksScreenEvent.OnRefresh -> {
                _state.update {
                    it.copy(
                        isRefreshing = true
                    )
                }
                getSavedProperties()
            }

            is BookmarksScreenEvent.OnError -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }

    private fun getSavedProperties(){
        viewModelScope.launch {
            repository.getSavedProperties().collect { result ->
                result.apply {
                    onSuccess { properties ->
                        _state.update {
                            it.copy(
                                properties = properties,
                                isRefreshing = false
                            )
                        }
                    }

                    onError {
                        _state.update {
                            it.copy(
                                isRefreshing = false
                            )
                        }
                        onEvent(BookmarksScreenEvent.OnError("Unable to get saved properties, please try again."))
                    }

                    onLoading {  isLoading ->
                        _state.update {
                            it.copy(
                                isLoading = isLoading
                            )
                        }
                    }
                }
            }
        }
    }

}