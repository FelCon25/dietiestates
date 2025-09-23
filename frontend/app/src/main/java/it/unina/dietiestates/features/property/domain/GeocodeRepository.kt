package it.unina.dietiestates.features.property.domain

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import kotlinx.coroutines.flow.Flow


interface GeocodeRepository {

    suspend fun getAddressesBySearch(query: String): Flow<Result<List<Address>, DataError.Remote>>

    suspend fun verifyAddress(address: Address): Flow<Result<Address, DataError.Remote>>

    suspend fun getAddressByLocation(latitude: Double, longitude: Double): Result<Address, DataError.Remote>
}