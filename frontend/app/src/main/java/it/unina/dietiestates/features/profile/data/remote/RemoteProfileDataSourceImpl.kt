package it.unina.dietiestates.features.profile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import it.unina.dietiestates.BuildConfig.BASE_URL
import it.unina.dietiestates.core.data.dto.UserDto
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.profile.data.dto.NotificationPreferencesDto
import it.unina.dietiestates.features.profile.data.dto.ProfilePictureDto
import it.unina.dietiestates.features.profile.data.dto.SessionsResponse

class RemoteProfileDataSourceImpl(
    private val httpClient: HttpClient
): RemoteProfileDataSource {

    override suspend fun getMe(): Result<UserDto, DataError.Remote> {
        return safeCall<UserDto> {
            httpClient.get("$BASE_URL/users/me")
        }
    }

    override suspend fun getSessions(): Result<SessionsResponse, DataError.Remote> {
        return safeCall<SessionsResponse> {
            httpClient.get("$BASE_URL/auth/sessions")
        }
    }

    override suspend fun changeProfilePic(imageBytes: ByteArray, fileName: String, imageExt: String): Result<ProfilePictureDto, DataError.Remote> {
        return safeCall<ProfilePictureDto> {
            httpClient.submitFormWithBinaryData(
                url = "$BASE_URL/users/me/profile-pic",
                formData = formData {
                    append(
                        key = "file",
                        value = imageBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, imageExt)
                            append(HttpHeaders.ContentDisposition, "filename=$fileName.${imageExt.substringAfter('/')}")
                        }
                    )
                }
            ){
                method = HttpMethod.Patch
            }
        }
    }

    override suspend fun getNotificationPreferences(): Result<List<NotificationPreferencesDto>, DataError.Remote> {
        return safeCall<List<NotificationPreferencesDto>> {
            httpClient.get(urlString = "$BASE_URL/notification-preferences")
        }
    }

    override suspend fun setPropertyNotificationStatus(enabled: Boolean): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(urlString = "$BASE_URL/notification-preferences/new-property-match"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "enabled" to enabled
                    )
                )
            }
        }
    }

    override suspend fun setPromotionalNotificationStatus(enabled: Boolean): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(urlString = "$BASE_URL/notification-preferences/promotional"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "enabled" to enabled
                    )
                )
            }
        }
    }
}