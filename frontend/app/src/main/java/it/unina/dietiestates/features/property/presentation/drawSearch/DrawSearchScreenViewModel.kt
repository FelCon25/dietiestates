package it.unina.dietiestates.features.property.presentation.drawSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.domain.NearbyPin
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import it.unina.dietiestates.features.property.domain.Property

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
            _isLoadingProperty.value = true
            when(val res = propertyRepository.getPropertyById(propertyId)) {
                is Result.Success -> _selectedProperty.value = res.data
                is Result.Error -> _selectedProperty.value = null
                is Result.IsLoading -> { /* no-op */ }
            }
            _isLoadingProperty.value = false
        }
    }

    fun clearSelectedProperty() {
        _selectedProperty.value = null
    }
} 