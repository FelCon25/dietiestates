package it.unina.dietiestates.core.data

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import it.unina.dietiestates.core.data.tokens.extractTokens
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.NoAccessTokenFoundException
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.ResultWithTokens
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.Serializable
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch(e: SocketTimeoutException) {
        return Result.Error(DataError.Remote.RequestTimeout)
    } catch(e: UnresolvedAddressException) {
        return Result.Error(DataError.Remote.NoInternet)
    } catch (e: Exception) {
        println("kekw")
        e.printStackTrace()
        coroutineContext.ensureActive()
        return Result.Error(DataError.Remote.Unknown)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> safeAuthCall(
    execute: () -> HttpResponse
): ResultWithTokens<T, DataError.Remote> {
    val response = try {
        execute()
    } catch(e: SocketTimeoutException) {
        return ResultWithTokens.Error(DataError.Remote.RequestTimeout)
    } catch(e: UnresolvedAddressException) {
        return ResultWithTokens.Error(DataError.Remote.NoInternet)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return ResultWithTokens.Error(DataError.Remote.Unknown)
    }

    return responseToResultWithTokens(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Remote> {
    return when(response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch(e: NoTransformationFoundException) {
                Result.Error(DataError.Remote.Serialization)
            }
        }
        401 -> Result.Error(DataError.Remote.Unauthorized)
        408 -> Result.Error(DataError.Remote.RequestTimeout)
        429 -> Result.Error(DataError.Remote.TooManyRequest)
        in 500..599 -> Result.Error(DataError.Remote.Server)
        else -> {
            Result.Error(DataError.Remote.Unknown)
        }
    }
}

suspend inline fun <reified T> responseToResultWithTokens(
    response: HttpResponse
): ResultWithTokens<T, DataError.Remote> {
    return when(response.status.value) {
        in 200..299 -> {
            try {
                ResultWithTokens.Success(response.body<T>(), response.extractTokens())
            }
            catch(e: NoTransformationFoundException) {
                ResultWithTokens.Error(DataError.Remote.Serialization)
            }
            catch (e: NoAccessTokenFoundException){
                ResultWithTokens.Error(DataError.Remote.Serialization)
            }
        }
        400 -> {
            try {
                ResultWithTokens.Error(DataError.Remote.CustomError(response.body<ErrorResponse>().message))
            }
            catch (e: Exception){
                ResultWithTokens.Error(DataError.Remote.Unknown)
            }
        }
        401 -> ResultWithTokens.Error(DataError.Remote.Unauthorized)
        408 -> ResultWithTokens.Error(DataError.Remote.RequestTimeout)
        429 -> ResultWithTokens.Error(DataError.Remote.TooManyRequest)
        in 500..599 -> ResultWithTokens.Error(DataError.Remote.Server)
        else -> {
            ResultWithTokens.Error(DataError.Remote.Unknown)
        }
    }
}

@Serializable
data class ErrorResponse(
    val message: String,
    val error: String,
    val statusCode: Int
)