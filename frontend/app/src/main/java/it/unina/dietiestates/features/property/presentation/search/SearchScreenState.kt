package it.unina.dietiestates.features.property.presentation.search

import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.SearchFilters

data class SearchScreenState(
    val addressQuery: String = "",
    val addressSuggestions: List<Address> = emptyList(),
    val selectedAddress: Address? = null,
    val isLoadingAddresses: Boolean = false,
    val showAddressSuggestions: Boolean = false,
    
    val filters: SearchFilters = SearchFilters(),
    val properties: List<Property> = emptyList(),
    val isLoadingProperties: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val totalProperties: Int = 0,
    val hasMore: Boolean = false,
    val error: String? = null,
    
    val showSaveSearchDialog: Boolean = false,
    val searchName: String = "",
    val isSavingSearch: Boolean = false
)

