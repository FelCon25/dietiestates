package it.unina.dietiestates.features.property.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.domain.GeocodeRepository
import it.unina.dietiestates.features.property.domain.PropertyRepository
import it.unina.dietiestates.features.property.domain.SearchFilters
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchScreenViewModel(
    private val propertyRepository: PropertyRepository,
    private val geocodeRepository: GeocodeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val addressQueryFlow = MutableStateFlow("")
    
    var onSearchSaved: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            addressQueryFlow
                .debounce(400)
                .collect { query ->
                    if (query.length >= 2) {
                        searchAddresses(query)
                    } else {
                        _state.value = _state.value.copy(
                            addressSuggestions = emptyList(),
                            showAddressSuggestions = false,
                            isLoadingAddresses = false
                        )
                    }
                }
        }
    }

    fun onAddressQueryChange(query: String) {
        _state.value = _state.value.copy(
            addressQuery = query,
            showAddressSuggestions = query.length >= 2,
            selectedAddress = null,
            isLoadingAddresses = query.length >= 2
        )
        addressQueryFlow.value = query
    }

    private fun searchAddresses(query: String) {
        viewModelScope.launch {
            geocodeRepository.getAddressesBySearch(query).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Filter and show valid addresses from Google
                        // Accept addresses with at least city OR province (to support regions)
                        val filteredAddresses = result.data.filter { address ->
                            (address.city.isNotBlank() || address.province.isNotBlank()) &&
                            address.formatted.trim() != "," &&
                            address.formatted.isNotBlank()
                        }
                        _state.value = _state.value.copy(
                            addressSuggestions = filteredAddresses,
                            isLoadingAddresses = false
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            addressSuggestions = emptyList(),
                            isLoadingAddresses = false
                        )
                    }
                    is Result.IsLoading -> {
                        _state.value = _state.value.copy(
                            isLoadingAddresses = result.isLoading
                        )
                    }
                }
            }
        }
    }

    fun onAddressSelected(address: Address) {
        val displayText = if (address.route.isNotBlank() && address.streetNumber.isNotBlank()) {
            "${address.route} ${address.streetNumber}, ${address.city}"
        } else if (address.route.isNotBlank()) {
            "${address.route}, ${address.city}"
        } else {
            "${address.city}, ${address.province}"
        }

        val searchText = if (address.route.isNotBlank() && address.streetNumber.isNotBlank()) {
            "${address.route} ${address.streetNumber}"
        } else if (address.route.isNotBlank()) {
            address.route
        } else {
            null
        }

        // Keep existing filters (price, type, etc.) and update only the location
        val currentFilters = _state.value.filters
        _state.value = _state.value.copy(
            selectedAddress = address,
            addressQuery = displayText,
            addressSuggestions = emptyList(),
            showAddressSuggestions = false,
            filters = currentFilters.copy(
                address = searchText,
                city = address.city,
                province = address.province,
                postalCode = address.postalCode,
                locationSearch = null
            ),
            currentPage = 1,
            properties = emptyList()
        )
        
        // Automatically start the search
        searchProperties()
    }

    fun onFiltersApplied(filters: SearchFilters) {
        val currentState = _state.value
        val mergedFilters = filters.copy(
            address = currentState.filters.address,
            city = currentState.filters.city,
            province = currentState.filters.province,
            postalCode = currentState.filters.postalCode,
            locationSearch = currentState.filters.locationSearch
        )
        
        _state.value = _state.value.copy(
            filters = mergedFilters,
            currentPage = 1,
            properties = emptyList()
        )
        
        // Automatically start the search with new filters
        searchProperties()
    }

    fun searchProperties() {
        viewModelScope.launch {
            val selectedAddress = _state.value.selectedAddress
            val currentFilters = _state.value.filters
            
            // MUST have a selected address to search
            if (selectedAddress == null) {
                _state.value = _state.value.copy(
                    error = "Please select an address from suggestions",
                    isLoadingProperties = false
                )
                return@launch
            }

            propertyRepository.searchProperties(
                filters = currentFilters,
                page = _state.value.currentPage,
                pageSize = 20
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            properties = if (_state.value.currentPage == 1) {
                                result.data.items
                            } else {
                                _state.value.properties + result.data.items
                            },
                            totalPages = result.data.totalPages,
                            totalProperties = result.data.total,
                            hasMore = result.data.hasMore,
                            isLoadingProperties = false,
                            error = null,
                            showAddressSuggestions = false
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoadingProperties = false,
                            error = "Failed to load properties",
                            showAddressSuggestions = false
                        )
                    }
                    is Result.IsLoading -> {
                        _state.value = _state.value.copy(
                            isLoadingProperties = result.isLoading
                        )
                    }
                }
            }
        }
    }

    fun loadMoreProperties() {
        viewModelScope.launch {
            if (!_state.value.isLoadingProperties && _state.value.hasMore) {
                // Delay to test pagination
                kotlinx.coroutines.delay(2000)
                _state.value = _state.value.copy(currentPage = _state.value.currentPage + 1)
                searchProperties()
            }
        }
    }

    fun clearSearch() {
        _state.value = SearchScreenState()
        addressQueryFlow.value = ""
    }

    fun showSaveSearchDialog() {
        _state.value = _state.value.copy(showSaveSearchDialog = true)
    }

    fun hideSaveSearchDialog() {
        _state.value = _state.value.copy(
            showSaveSearchDialog = false,
            searchName = ""
        )
    }

    fun onSearchNameChange(name: String) {
        _state.value = _state.value.copy(searchName = name)
    }

    fun saveSearch() {
        val searchName = _state.value.searchName.trim()
        if (searchName.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingSearch = true)
            
            propertyRepository.createSavedSearch(
                name = searchName,
                filters = _state.value.filters
            ).collect { result ->
                result.onSuccess {
                    _state.value = _state.value.copy(
                        isSavingSearch = false,
                        showSaveSearchDialog = false,
                        searchName = ""
                    )
                    onSearchSaved?.invoke()
                }.onError {
                    _state.value = _state.value.copy(
                        isSavingSearch = false,
                        error = "Unable to save search"
                    )
                }
            }
        }
    }
    
    fun applySavedSearch(filters: SearchFilters) {
        // Create a fake address for display purposes
        val displayAddress = buildString {
            filters.address?.let { append(it) }
            if (isNotEmpty() && filters.city != null) append(", ")
            filters.city?.let { append(it) }
        }
        
        // Create a fake Address object
        val fakeAddress = Address(
            formatted = displayAddress,
            route = filters.address ?: "",
            streetNumber = "",
            city = filters.city ?: "",
            province = filters.province ?: "",
            postalCode = filters.postalCode ?: "",
            latitude = 0.0,
            longitude = 0.0
        )
        
        _state.value = _state.value.copy(
            selectedAddress = fakeAddress,
            addressQuery = displayAddress,
            addressSuggestions = emptyList(),
            showAddressSuggestions = false,
            filters = filters,
            currentPage = 1,
            properties = emptyList()
        )
        
        // Automatically search with the saved filters
        searchProperties()
    }
}

