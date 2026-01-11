package it.unina.dietiestates.features.property.presentation.drawSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.location.LocationRepository
import it.unina.dietiestates.features.property.domain.NearbyFilters
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawSearchScreenViewModel(
    private val propertyRepository: PropertyRepository,
    private val locationRepository: LocationRepository
): ViewModel() {

    private val _state = MutableStateFlow(DrawSearchScreenState())
    val state = _state.asStateFlow()

    fun requestCurrentLocation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLocation = true) }
            when (val result = locationRepository.getCurrentLocation()) {
                is Result.Success -> _state.update { 
                    it.copy(
                        currentLocation = result.data,
                        isLoadingLocation = false
                    ) 
                }
                is Result.Error -> _state.update { 
                    it.copy(
                        currentLocation = null,
                        isLoadingLocation = false
                    ) 
                }
                is Result.IsLoading -> { /* no-op */ }
            }
        }
    }

    fun loadPins(latitude: Double, longitude: Double, radiusKm: Double, filters: NearbyFilters?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val res = propertyRepository.getNearbyPins(latitude, longitude, radiusKm, filters)) {
                is Result.Success -> _state.update { 
                    it.copy(
                        pins = res.data,
                        isLoading = false
                    ) 
                }
                is Result.Error -> _state.update { 
                    it.copy(
                        pins = emptyList(),
                        isLoading = false
                    ) 
                }
                is Result.IsLoading -> { /* no-op */ }
            }
        }
    }

    fun loadPropertyById(propertyId: Int) {
        viewModelScope.launch {
            propertyRepository.getPropertyById(propertyId).collect { result ->
                when (result) {
                    is Result.Success -> _state.update { 
                        it.copy(
                            selectedProperty = result.data,
                            selectedProperties = listOf(result.data)
                        ) 
                    }
                    is Result.Error -> _state.update { 
                        it.copy(
                            selectedProperty = null,
                            selectedProperties = emptyList()
                        ) 
                    }
                    is Result.IsLoading -> _state.update { 
                        it.copy(isLoadingProperty = result.isLoading) 
                    }
                }
            }
        }
    }

    fun loadPropertiesByIds(propertyIds: List<Int>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingProperty = true) }
            val properties = mutableListOf<it.unina.dietiestates.features.property.domain.Property>()
            
            for (id in propertyIds) {
                propertyRepository.getPropertyById(id).collect { result ->
                    when (result) {
                        is Result.Success -> properties.add(result.data)
                        is Result.Error -> { /* skip failed loads */ }
                        is Result.IsLoading -> { /* no-op */ }
                    }
                }
            }
            
            _state.update { 
                it.copy(
                    selectedProperties = properties,
                    selectedProperty = properties.firstOrNull(),
                    isLoadingProperty = false
                ) 
            }
        }
    }

    fun clearSelectedProperty() {
        _state.update { 
            it.copy(
                selectedProperty = null,
                selectedProperties = emptyList()
            ) 
        }
    }
}
