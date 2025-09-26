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

        getProperty(propertyId)
    }

    fun getProperty(propertyId: Int){
        viewModelScope.launch {
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
    }
}
