package it.unina.dietiestates.features.auth.domain

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Flow<Result<User, DataError.Remote>>

    suspend fun googleAuth(token: String): Flow<Result<User, DataError.Remote>>

    suspend fun register(email: String, firstName: String, lastName: String, password: String): Flow<Result<User, DataError.Remote>>

    suspend fun sendPasswordReset(email: String): EmptyResult<DataError.Remote>

    suspend fun verifyPasswordResetCode(code: String): EmptyResult<DataError.Remote>

    suspend fun passwordReset(code: String, newPassword: String): EmptyResult<DataError.Remote>

    suspend fun changePassword(currentPassword: String, newPassword: String, logoutOtherDevices: Boolean): EmptyResult<DataError.Remote>

    suspend fun getMe(): Flow<Result<User, DataError.Remote>>

    suspend fun logout(): EmptyResult<DataError.Remote>

    suspend fun deleteSession(sessionId: Int): EmptyResult<DataError.Remote>

    suspend fun sendPushNotificationToken(token: String): EmptyResult<DataError.Remote>
}