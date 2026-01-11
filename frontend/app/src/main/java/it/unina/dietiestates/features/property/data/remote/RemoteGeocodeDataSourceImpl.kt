package it.unina.dietiestates.features.property.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import it.unina.dietiestates.BuildConfig
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.data.dto.GeocodeResponse

class RemoteGeocodeDataSourceImpl(
    private val httpClient: HttpClient
): RemoteGeocodeDataSource {

    override suspend fun getAddressesBySearch(query: String): Result<GeocodeResponse, DataError.Remote> {
        return safeCall<GeocodeResponse> {
            httpClient.get(
                urlString = "https://maps.googleapis.com/maps/api/geocode/json"
            ){
                url{
                    parameters.append("address", query)
                    parameters.append("key", BuildConfig.MAPS_API_KEY)
                    parameters.append("components", "country:it")
                    parameters.append("language", "en")
                }
            }
        }
    }

    override suspend fun verifyAddress(
        city: String,
        province: String,
        postalCode: String,
        route: String,
        streetNumber: String
    ): Result<GeocodeResponse, DataError.Remote> {
        return safeCall<GeocodeResponse> {
            httpClient.get(
                urlString = "https://maps.googleapis.com/maps/api/geocode/json"
            ){
                url{
                    parameters.append("address", "$route $streetNumber $city $province $postalCode")
                    parameters.append("key", BuildConfig.MAPS_API_KEY)
                    parameters.append("components", "country:it")
                    parameters.append("language", "en")
                }
            }
        }
    }

    override suspend fun getAddressByLocation(
        latitude: Double,
        longitude: Double
    ): Result<String, DataError.Remote> {
        TODO("Not yet implemented")
    }
}