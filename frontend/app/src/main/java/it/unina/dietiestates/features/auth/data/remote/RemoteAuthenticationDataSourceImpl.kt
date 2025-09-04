package it.unina.dietiestates.features.auth.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
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

    override suspend fun getMe(): Result<UserDto, DataError.Remote> {
        return safeCall<UserDto> {
            httpClient.get("$BASE_URL/users/me")
        }
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(urlString = "$BASE_URL/auth/logout")
        }
    }

    override suspend fun deleteSession(sessionId: Int): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.delete(urlString = "$BASE_URL/auth/sessions/$sessionId")
        }
    }
}