package it.unina.dietiestates.features.auth.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.unina.dietiestates.BuildConfig.BASE_URL
import it.unina.dietiestates.core.data.dto.UserDto
import it.unina.dietiestates.core.data.safeAuthCall
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.ResultWithTokens
import it.unina.dietiestates.core.domain.onSuccess
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val logoutOtherDevices: Boolean
)

class RemoteAuthenticationDataSourceImpl(
    private val httpClient: HttpClient
): RemoteAuthenticationDataSource {

    override suspend fun signIn(email: String, password: String): ResultWithTokens<UserDto, DataError.Remote> {
        return safeAuthCall {
            httpClient.post(
                urlString = "${BASE_URL}/auth/login"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "password" to password
                    )
                )
            }
        }
    }

    override suspend fun googleAuth(token: String): ResultWithTokens<UserDto, DataError.Remote> {
        return safeAuthCall {
            httpClient.post(
                urlString = "$BASE_URL/auth/google-auth"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "token" to token,
                    )
                )
            }
        }
    }

    override suspend fun register(email: String, firstName: String, lastName: String, password: String): ResultWithTokens<UserDto, DataError.Remote> {
        return safeAuthCall {
            httpClient.post("$BASE_URL/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "password" to password
                    )
                )
            }
        }
    }

    override suspend fun sendPasswordReset(email: String): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post("$BASE_URL/auth/password/forgot"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email
                    )
                )
            }
        }
    }

    override suspend fun verifyPasswordResetCode(code: String): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post("$BASE_URL/auth/password/verify-code"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "code" to code
                    )
                )
            }
        }
    }

    override suspend fun passwordReset(code: String, newPassword: String): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post("$BASE_URL/auth/password/reset"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "code" to code,
                        "newPassword" to newPassword
                    )
                )
            }
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String, logoutOtherDevices: Boolean): EmptyResult<DataError.Remote> {
        return safeCall<Unit> {
            httpClient.put("$BASE_URL/auth/password/change"){
                contentType(ContentType.Application.Json)
                setBody(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        logoutOtherDevices = logoutOtherDevices
                    )
                )
            }
        }
    }

    override suspend fun getMe(): Result<UserDto, DataError.Remote> {
        return safeCall<UserDto> {
            httpClient.get("$BASE_URL/users/me")
        }
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> {
        return safeCall<Unit> {
            httpClient.post(urlString = "$BASE_URL/auth/logout")
        }.onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }

    override suspend fun deleteSession(sessionId: Int): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.delete(urlString = "$BASE_URL/auth/sessions/$sessionId")
        }
    }

    override suspend fun sendPushNotificationToken(token: String): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(urlString = "$BASE_URL/auth/sessions/notification-token") {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "notificationToken" to token
                    )
                )
            }
        }
    }
}