package it.unina.dietiestates.features.property.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import it.unina.dietiestates.BuildConfig.BASE_URL
import it.unina.dietiestates.core.data.FileInfo
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.data.dto.PropertyDto
import it.unina.dietiestates.features.property.data.dto.NearbyPinDto

class RemotePropertyDataSourceImpl(
    private val httpClient: HttpClient
): RemotePropertyDataSource {

    override suspend fun createProperty(property: PropertyDto, images: List<FileInfo>): Result<PropertyDto, DataError.Remote> {
        return safeCall<PropertyDto> {
            httpClient.submitFormWithBinaryData(
                url = "$BASE_URL/property",
                formData = formData {
                    images.map {
                        append(
                            key = "images",
                            value = it.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, it.mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=${it.name}")
                            }
                        )
                    }

                    append("description", property.description)
                    append("price", property.price.toString())
                    append("surfaceArea", property.surfaceArea.toString())
                    append("rooms", property.rooms.toString())
                    append("floors", property.floors.toString())
                    append("elevator", property.elevator.toString())
                    append("energyClass", property.energyClass)
                    append("concierge", property.concierge.toString())
                    append("airConditioning", property.airConditioning.toString())
                    append("insertionType", property.insertionType)
                    append("propertyType", property.propertyType)
                    append("address", property.address)
                    append("city", property.city)
                    append("postalCode", property.postalCode)
                    append("province", property.province)
                    append("country", property.country)
                    append("latitude", property.latitude.toString())
                    append("longitude", property.longitude.toString())
                    append("agentId", property.agentId.toString())
                    append("furnished", property.furnished.toString())
                    append("propertyCondition", property.propertyCondition)
                }
            )
        }
    }

    override suspend fun getAgentProperties(): Result<List<PropertyDto>, DataError.Remote> {
        return safeCall<List<PropertyDto>> {
            httpClient.get(
                urlString = "$BASE_URL/property/by-agent"
            )
        }
    }

    override suspend fun getNearbyPins(latitude: Double, longitude: Double, radiusKm: Double, insertionType: String?): Result<List<NearbyPinDto>, DataError.Remote> {
        return safeCall<List<NearbyPinDto>> {
            httpClient.get(
                urlString = "$BASE_URL/property/nearby"
            ) {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("radiusKm", radiusKm)
                if(!insertionType.isNullOrBlank()) parameter("insertionType", insertionType)
            }
        }
    }

    override suspend fun getPropertyById(propertyId: Int): Result<PropertyDto, DataError.Remote> {
        return safeCall<PropertyDto> {
            httpClient.get(
                urlString = "$BASE_URL/property/$propertyId"
            )
        }
    }

    override suspend fun getSavedProperties(): Result<List<PropertyDto>, DataError.Remote> {
        return safeCall<List<PropertyDto>> {
            httpClient.get(
                urlString = "$BASE_URL/property/saved"
            )
        }
    }
}