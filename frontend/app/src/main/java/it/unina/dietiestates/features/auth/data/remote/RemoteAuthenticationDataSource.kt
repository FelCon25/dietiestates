package it.unina.dietiestates.features.auth.data.remote

import it.unina.dietiestates.core.data.dto.UserDto
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.ResultWithTokens

interface RemoteAuthenticationDataSource {

    suspend fun signIn(email: String, password: String): ResultWithTokens<UserDto, DataError.Remote>

    suspend fun googleAuth(token: String): ResultWithTokens<UserDto, DataError.Remote>

    suspend fun register(email: String, firstName: String, lastName: String, password: String): ResultWithTokens<UserDto, DataError.Remote>

    suspend fun getMe(): Result<UserDto, DataError.Remote>

    suspend fun logout(): EmptyResult<DataError.Remote>

    suspend fun deleteSession(sessionId: Int): EmptyResult<DataError.Remote>
}