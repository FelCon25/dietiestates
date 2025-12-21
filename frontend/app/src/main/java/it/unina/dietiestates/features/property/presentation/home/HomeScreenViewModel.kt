package it.unina.dietiestates.features.property.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.domain.PropertyRepository
import it.unina.dietiestates.features.property.domain.SearchFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val repository: PropertyRepository
): ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    private val nearbyPageSize = 20

    init {
        loadNearbyForSale()
        loadNearbyForRent()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            HomeScreenEvent.Refresh -> refresh()
            is HomeScreenEvent.OnLocationReceived -> {
                _state.update {
                    it.copy(
                        userLatitude = event.latitude,
                        userLongitude = event.longitude
                    )
                }
                loadNearbyForSale()
                loadNearbyForRent()
            }
        }
    }

    private fun loadNearbyForSale() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingForSale = true) }
            
            val filters = SearchFilters(
                insertionType = "SALE",
                sortBy = "createdAt",
                sortOrder = "desc"
            )

            repository.searchProperties(
                filters = filters,
                page = 1,
                pageSize = nearbyPageSize
            ).collect { result ->
                result.apply {
                    onSuccess { searchResult ->
                        // Filter only SALE properties
                        val saleProperties = searchResult.items.filter { property ->
                            property.insertionType == InsertionType.SALE
                        }
                        _state.update {
                            it.copy(
                                nearbyForSale = saleProperties,
                                isLoadingForSale = false
                            )
                        }
                    }
                    onError { error ->
                        _state.update {
                            it.copy(
                                isLoadingForSale = false,
                                error = error.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadNearbyForRent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingForRent = true) }
            
            val filters = SearchFilters(
                insertionType = "RENT",
                sortBy = "createdAt",
                sortOrder = "desc"
            )

            repository.searchProperties(
                filters = filters,
                page = 1,
                pageSize = nearbyPageSize
            ).collect { result ->
                result.apply {
                    onSuccess { searchResult ->
                        // Filter only RENT properties
                        val rentProperties = searchResult.items.filter { property ->
                            property.insertionType == InsertionType.RENT
                        }
                        _state.update {
                            it.copy(
                                nearbyForRent = rentProperties,
                                isLoadingForRent = false
                            )
                        }
                    }
                    onError { error ->
                        _state.update {
                            it.copy(
                                isLoadingForRent = false,
                                error = error.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun refresh() {
        _state.update {
            it.copy(
                nearbyForSale = emptyList(),
                nearbyForRent = emptyList()
            )
        }
        loadNearbyForSale()
        loadNearbyForRent()
    }
}