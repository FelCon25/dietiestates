package it.unina.dietiestates.features.property.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import it.unina.dietiestates.BuildConfig.BASE_URL
import it.unina.dietiestates.core.data.FileInfo
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.property.data.dto.IsPropertySavedResponse
import it.unina.dietiestates.features.property.data.dto.PropertyDto
import it.unina.dietiestates.features.property.data.dto.NearbyPinDto
import it.unina.dietiestates.features.property.data.dto.SearchResultDto
import it.unina.dietiestates.features.property.data.dto.SavedSearchDto
import it.unina.dietiestates.features.property.data.dto.CreateSavedSearchDto
import it.unina.dietiestates.features.property.domain.NearbyFilters
import it.unina.dietiestates.features.property.domain.SearchFilters
import io.ktor.client.request.setBody

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

    override suspend fun getNearbyPins(latitude: Double, longitude: Double, radiusKm: Double, filters: NearbyFilters?): Result<List<NearbyPinDto>, DataError.Remote> {
        return safeCall<List<NearbyPinDto>> {
            httpClient.get(
                urlString = "$BASE_URL/property/nearby"
            ) {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("radiusKm", radiusKm)
                filters?.let { f ->
                    f.insertionType?.takeIf { it.isNotBlank() }?.let { v -> parameter("insertionType", v) }
                    f.minPrice?.let { v -> parameter("minPrice", v) }
                    f.maxPrice?.let { v -> parameter("maxPrice", v) }
                    f.minSurfaceArea?.let { v -> parameter("minSurfaceArea", v) }
                    f.maxSurfaceArea?.let { v -> parameter("maxSurfaceArea", v) }
                    f.minRooms?.let { v -> parameter("minRooms", v) }
                    f.maxRooms?.let { v -> parameter("maxRooms", v) }
                    f.type?.takeIf { it.isNotBlank() }?.let { v -> parameter("type", v) }
                    f.propertyCondition?.takeIf { it.isNotBlank() }?.let { v -> parameter("propertyCondition", v) }
                    f.elevator?.let { v -> parameter("elevator", v) }
                    f.airConditioning?.let { v -> parameter("airConditioning", v) }
                    f.concierge?.let { v -> parameter("concierge", v) }
                    f.furnished?.let { v -> parameter("furnished", v) }
                    f.energyClass?.takeIf { it.isNotBlank() }?.let { v -> parameter("energyClass", v) }
                    f.agencyId?.let { v -> parameter("agencyId", v) }
                    f.agentId?.let { v -> parameter("agentId", v) }
                }
            }
        }
    }

    override suspend fun searchProperties(filters: SearchFilters, page: Int, pageSize: Int): Result<SearchResultDto, DataError.Remote> {
        return safeCall<SearchResultDto> {
            httpClient.get(
                urlString = "$BASE_URL/property/search"
            ) {
                parameter("page", page)
                parameter("pageSize", pageSize)
                filters.insertionType?.takeIf { it.isNotBlank() }?.let { parameter("insertionType", it) }
                filters.address?.takeIf { it.isNotBlank() }?.let { parameter("address", it) }
                filters.city?.takeIf { it.isNotBlank() }?.let { parameter("city", it) }
                filters.province?.takeIf { it.isNotBlank() }?.let { parameter("province", it) }
                filters.country?.takeIf { it.isNotBlank() }?.let { parameter("country", it) }
                filters.postalCode?.takeIf { it.isNotBlank() }?.let { parameter("postalCode", it) }
                filters.minPrice?.let { parameter("minPrice", it) }
                filters.maxPrice?.let { parameter("maxPrice", it) }
                filters.minSurfaceArea?.let { parameter("minSurfaceArea", it) }
                filters.maxSurfaceArea?.let { parameter("maxSurfaceArea", it) }
                filters.minRooms?.let { parameter("minRooms", it) }
                filters.maxRooms?.let { parameter("maxRooms", it) }
                filters.type?.takeIf { it.isNotBlank() }?.let { parameter("type", it) }
                filters.propertyCondition?.takeIf { it.isNotBlank() }?.let { parameter("propertyCondition", it) }
                filters.elevator?.let { parameter("elevator", it) }
                filters.airConditioning?.let { parameter("airConditioning", it) }
                filters.concierge?.let { parameter("concierge", it) }
                filters.furnished?.let { parameter("furnished", it) }
                filters.energyClass?.takeIf { it.isNotBlank() }?.let { parameter("energyClass", it) }
                filters.agencyId?.let { parameter("agencyId", it) }
                filters.agentId?.let { parameter("agentId", it) }
                filters.sortBy?.let { parameter("sortBy", it) }
                filters.sortOrder?.let { parameter("sortOrder", it) }
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

    override suspend fun isPropertySaved(propertyId: Int): Result<IsPropertySavedResponse, DataError.Remote> {
        return safeCall<IsPropertySavedResponse> {
            httpClient.get(
                urlString = "$BASE_URL/property/saved/$propertyId"
            )
        }
    }

    override suspend fun saveProperty(propertyId: Int): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(
                urlString = "$BASE_URL/property/saved/$propertyId"
            )
        }
    }

    override suspend fun unsaveProperty(propertyId: Int): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.delete(
                urlString = "$BASE_URL/property/saved/$propertyId"
            )
        }
    }

    override suspend fun createSavedSearch(dto: CreateSavedSearchDto): Result<SavedSearchDto, DataError.Remote> {
        return safeCall<SavedSearchDto> {
            httpClient.post(
                urlString = "$BASE_URL/saved-searches"
            ) {
                setBody(dto)
            }
        }
    }

    override suspend fun getSavedSearches(): Result<List<SavedSearchDto>, DataError.Remote> {
        return safeCall<List<SavedSearchDto>> {
            httpClient.get(
                urlString = "$BASE_URL/saved-searches"
            )
        }
    }

    override suspend fun getSavedSearchById(searchId: Int): Result<SavedSearchDto, DataError.Remote> {
        return safeCall<SavedSearchDto> {
            httpClient.get(
                urlString = "$BASE_URL/saved-searches/$searchId"
            )
        }
    }

    override suspend fun deleteSavedSearch(searchId: Int): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.delete(
                urlString = "$BASE_URL/saved-searches/$searchId"
            )
        }
    }
}