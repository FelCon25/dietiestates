package it.unina.dietiestates.features.property.data.remote

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.data.dto.GeocodeResponse

interface RemoteGeocodeDataSource {

    suspend fun getAddressesBySearch(query: String): Result<GeocodeResponse, DataError.Remote>

    suspend fun verifyAddress(city: String, province: String, postalCode: String, route: String, streetNumber: String): Result<GeocodeResponse, DataError.Remote>

    suspend fun getAddressByLocation(latitude: Double, longitude: Double): Result<String, DataError.Remote>
}