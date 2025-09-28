package it.unina.dietiestates.features.property.presentation.propertyDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PropertyDetailsScreenViewModel(
    private val repository: PropertyRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _state = MutableStateFlow(PropertyDetailsScreenState())
    val state = _state.asStateFlow()

    init {
        val propertyId = savedStateHandle.toRoute<Route.PropertyDetails>().id

        viewModelScope.launch {
            async { getProperty(propertyId) }.await()
            async { isPropertySaved(propertyId) }.await()
        }
    }

    suspend fun getProperty(propertyId: Int){
        repository.getPropertyById(propertyId).collect { result ->
            result.apply {
                onSuccess { property ->
                    _state.update {
                        it.copy(
                            property = property
                        )
                    }
                }
                onError { error ->

                }
                onLoading { isLoading ->
                    _state.update {
                        it.copy(
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }

    private suspend fun isPropertySaved(propertyId: Int){
        repository.isPropertySaved(propertyId).apply {
            onSuccess { isSaved ->
                _state.update {
                    it.copy(
                        isSaved = isSaved
                    )
                }
            }
        }
    }

    fun toggleSavedProperty(){
        _state.value.property?.let { property ->
            viewModelScope.launch {
                repository.toggleSavedProperty(propertyId = property.propertyId, isSaved = !_state.value.isSaved).apply {
                    onSuccess {
                        _state.update {
                            it.copy(
                                isSaved = !it.isSaved
                            )
                        }
                    }
                }
            }
        }
    }
}
