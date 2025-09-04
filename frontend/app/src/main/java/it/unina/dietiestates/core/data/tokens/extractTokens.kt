package it.unina.dietiestates.core.data.tokens

import io.ktor.client.statement.HttpResponse
import io.ktor.http.Cookie
import io.ktor.http.setCookie
import it.unina.dietiestates.core.domain.NoAccessTokenFoundException
import it.unina.dietiestates.core.domain.Tokens


fun HttpResponse.extractAccessToken(): String{
    val cookies: List<Cookie> = this.setCookie()

    return cookies.find { it.name == "accessToken" }?.value ?: throw NoAccessTokenFoundException("No access token found")
}

fun HttpResponse.extractTokens(): Tokens{
    val cookies: List<Cookie> = this.setCookie()

    val accessToken = cookies.find { it.name == "accessToken" }?.value
    val refreshToken = cookies.find { it.name == "refreshToken" }?.value

    return if (accessToken != null)
        Tokens(access = accessToken, refresh = refreshToken)
    else
        throw NoAccessTokenFoundException("No access tokens found")
}