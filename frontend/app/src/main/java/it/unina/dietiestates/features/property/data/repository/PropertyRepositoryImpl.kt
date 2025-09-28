package it.unina.dietiestates.features.property.data.repository

import android.net.Uri
import it.unina.dietiestates.core.data.FileReader
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.map
import it.unina.dietiestates.features.property.data.mappers.toNearbyPin
import it.unina.dietiestates.features.property.data.mappers.toProperty
import it.unina.dietiestates.features.property.data.mappers.toPropertyDto
import it.unina.dietiestates.features.property.data.remote.RemotePropertyDataSource
import it.unina.dietiestates.features.property.domain.NearbyPin
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PropertyRepositoryImpl(
    private val remotePropertyDataSource: RemotePropertyDataSource,
    private val fileReader: FileReader
): PropertyRepository {


    override suspend fun createProperty(property: Property, images: List<Uri>): Flow<Result<Property, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            val imagesInfoList = images.map { fileReader.uriToFileInfo(it) }

            emit(
                remotePropertyDataSource.createProperty(
                    property.toPropertyDto(),
                    imagesInfoList
                ).map { it.toProperty() }
            )

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun getAgentProperties(): Result<List<Property>, DataError.Remote> {
        return remotePropertyDataSource.getAgentProperties().map {
            it.map { it.toProperty() }
        }
    }

    override suspend fun getNearbyPins(latitude: Double, longitude: Double, radiusKm: Double, insertionType: String?): Result<List<NearbyPin>, DataError.Remote> {
        return remotePropertyDataSource.getNearbyPins(latitude, longitude, radiusKm, insertionType).map {
            it.map { it.toNearbyPin() }
        }
    }

    override suspend fun getPropertyById(propertyId: Int): Flow<Result<Property, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remotePropertyDataSource.getPropertyById(propertyId).map {
                it.toProperty()
            })

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun getSavedProperties(): Flow<Result<List<Property>, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remotePropertyDataSource.getSavedProperties().map { properties ->
                properties.map { it.toProperty() }
            })

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun isPropertySaved(propertyId: Int): Result<Boolean, DataError.Remote> {
        return remotePropertyDataSource.isPropertySaved(propertyId).map { it.isSaved }
    }

    override suspend fun toggleSavedProperty(propertyId: Int, isSaved: Boolean): Result<Unit, DataError.Remote> {
        return if(isSaved){
            remotePropertyDataSource.saveProperty(propertyId)
        }
        else{
            remotePropertyDataSource.unsaveProperty(propertyId)
        }
    }
}