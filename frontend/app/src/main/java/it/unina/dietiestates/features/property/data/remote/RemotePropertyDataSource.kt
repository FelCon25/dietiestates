package it.unina.dietiestates.features.property.data.remote

import it.unina.dietiestates.core.data.FileInfo
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.data.dto.PropertyDto
import it.unina.dietiestates.features.property.data.dto.NearbyPinDto

interface RemotePropertyDataSource {

    suspend fun createProperty(property: PropertyDto, images: List<FileInfo>): Result<PropertyDto, DataError.Remote>

    suspend fun getAgentProperties(): Result<List<PropertyDto>, DataError.Remote>

    suspend fun getNearbyPins(latitude: Double, longitude: Double, radiusKm: Double, insertionType: String?): Result<List<NearbyPinDto>, DataError.Remote>

    suspend fun getPropertyById(propertyId: Int): Result<PropertyDto, DataError.Remote>

    suspend fun getSavedProperties(): Result<List<PropertyDto>, DataError.Remote>
}