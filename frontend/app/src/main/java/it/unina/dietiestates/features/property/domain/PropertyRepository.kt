package it.unina.dietiestates.features.property.domain

import android.net.Uri
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    suspend fun createProperty(property: Property, images: List<Uri>): Flow<Result<Property, DataError.Remote>>

    suspend fun getAgentProperties(): Result<List<Property>, DataError.Remote>

    suspend fun getNearbyPins(latitude: Double, longitude: Double, radiusKm: Double, filters: NearbyFilters? = null): Result<List<NearbyPin>, DataError.Remote>

    suspend fun searchProperties(filters: SearchFilters, page: Int, pageSize: Int): Flow<Result<SearchResult, DataError.Remote>>

    suspend fun getPropertyById(propertyId: Int): Flow<Result<Property, DataError.Remote>>

    suspend fun getSavedProperties(): Flow<Result<List<Property>, DataError.Remote>>

    suspend fun isPropertySaved(propertyId: Int): Result<Boolean, DataError.Remote>

    suspend fun toggleSavedProperty(propertyId: Int, isSaved: Boolean): Result<Unit, DataError.Remote>

    // Saved searches
    suspend fun createSavedSearch(name: String, filters: SearchFilters): Flow<Result<SavedSearch, DataError.Remote>>

    suspend fun getSavedSearches(): Flow<Result<List<SavedSearch>, DataError.Remote>>

    suspend fun getSavedSearchById(searchId: Int): Flow<Result<SavedSearch, DataError.Remote>>

    suspend fun deleteSavedSearch(searchId: Int): Flow<Result<Unit, DataError.Remote>>
}