package it.unina.dietiestates.core.data

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import it.unina.dietiestates.BuildConfig
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine, tokenManager: TokenManager): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(UserAgent){
                agent = "${Build.MANUFACTURER} ${Build.MODEL}"
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 20_000
                requestTimeoutMillis = 20_000
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        println("Tokens loaded!")
                        val access = tokenManager.getAccessToken()
                        val refresh = tokenManager.getRefreshToken()

                        if (access != null && refresh != null) {
                            BearerTokens(access, refresh)
                        } else {
                            null
                        }
                    }

                    sendWithoutRequest { request ->
                        val path = request.url.encodedPath
                        !path.startsWith("/auth/refresh") &&
                        !path.startsWith("/auth/login") &&
                        !path.startsWith("/auth/register")
                    }

                    refreshTokens {
                        val refresh = tokenManager.getRefreshToken() ?: return@refreshTokens null

                        safeAuthCall<Unit> {
                            client.post("${BuildConfig.BASE_URL}/auth/refresh") {
                                headers {
                                    append(HttpHeaders.Authorization, "Bearer $refresh")
                                }
                                markAsRefreshTokenRequest()
                            }
                        }
                        .onSuccess { _, tokens ->
                            tokenManager.saveTokens(tokens.access, tokens.refresh ?: refresh)
                            return@refreshTokens BearerTokens(tokens.access, tokens.refresh ?: refresh)
                        }
                        .onError {
                            tokenManager.clearTokens()
                        }

                        return@refreshTokens null
                    }
                }
            }

            install(Logging) {
                level = LogLevel.HEADERS
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
