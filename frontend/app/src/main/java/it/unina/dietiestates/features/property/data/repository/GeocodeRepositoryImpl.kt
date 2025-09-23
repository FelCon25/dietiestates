package it.unina.dietiestates.features.property.data.repository

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.map
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.property.data.mappers.toAddress
import it.unina.dietiestates.features.property.data.remote.RemoteGeocodeDataSource
import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.domain.GeocodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeocodeRepositoryImpl(
    private val remoteGeocodeDataSource: RemoteGeocodeDataSource
): GeocodeRepository {

    override suspend fun getAddressesBySearch(query: String): Flow<Result<List<Address>, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remoteGeocodeDataSource.getAddressesBySearch(query).map { geoResponse ->
                geoResponse.results.map {
                    it.toAddress()
                }
            })

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun verifyAddress(address: Address): Flow<Result<Address, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            remoteGeocodeDataSource.verifyAddress(
                city = address.city,
                province = address.province,
                postalCode = address.postalCode,
                route = address.route,
                streetNumber = address.streetNumber
            ).onSuccess { geoResponse ->
                if(geoResponse.results.isNotEmpty()){
                    val address = geoResponse.results.first().toAddress()

                    if(address.isComplete()){
                        emit(Result.Success(address))
                    }
                    else{
                        emit(Result.Error(DataError.Remote.Unknown))
                    }
                }
                else{
                    emit(Result.Error(DataError.Remote.Unknown))
                }
            }.onError {
                emit(Result.Error(it))
            }

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun getAddressByLocation(
        latitude: Double,
        longitude: Double
    ): Result<Address, DataError.Remote> {
        TODO("Not yet implemented")
    }
}