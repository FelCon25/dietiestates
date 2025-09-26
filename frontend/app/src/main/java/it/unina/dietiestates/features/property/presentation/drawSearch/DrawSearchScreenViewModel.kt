package it.unina.dietiestates.features.property.presentation.drawSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.domain.NearbyPin
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DrawSearchScreenViewModel(
    private val propertyRepository: PropertyRepository
): ViewModel() {

    private val _pins = MutableStateFlow<List<NearbyPin>>(emptyList())
    val pins: StateFlow<List<NearbyPin>> = _pins

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty

    private val _isLoadingProperty = MutableStateFlow(false)
    val isLoadingProperty: StateFlow<Boolean> = _isLoadingProperty

    fun loadPins(latitude: Double, longitude: Double, radiusKm: Double, insertionType: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            when(val res = propertyRepository.getNearbyPins(latitude, longitude, radiusKm, insertionType)) {
                is Result.Success -> _pins.value = res.data
                is Result.Error -> _pins.value = emptyList()
                is Result.IsLoading -> { /* no-op */ }
            }
            _isLoading.value = false
        }
    }

    fun loadPropertyById(propertyId: Int) {
        viewModelScope.launch {
            propertyRepository.getPropertyById(propertyId).collect { result ->
                when(result) {
                    is Result.Success -> _selectedProperty.value = result.data
                    is Result.Error -> _selectedProperty.value = null
                    is Result.IsLoading -> { _isLoadingProperty.value =  result.isLoading}
                }
            }
        }
    }

    fun clearSelectedProperty() {
        _selectedProperty.value = null
    }
} 