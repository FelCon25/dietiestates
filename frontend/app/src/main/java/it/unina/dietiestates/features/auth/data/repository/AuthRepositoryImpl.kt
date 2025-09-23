package it.unina.dietiestates.features.auth.data.repository

import it.unina.dietiestates.core.data.mappers.toUser
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.Tokens
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.data.remote.RemoteAuthenticationDataSource
import it.unina.dietiestates.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val remoteAuthenticationDataSource: RemoteAuthenticationDataSource,
    private val tokenManager: TokenManager
): AuthRepository {


    override suspend fun signIn(email: String, password: String): Flow<Result<User, DataError.Remote>>{
        return flow {
            emit(Result.IsLoading(true))

            remoteAuthenticationDataSource.signIn(email = email, password = password)
                .onSuccess { user, tokens ->
                    saveTokens(tokens)
                    emit(Result.Success(user.toUser()))
                }
                .onError { error ->
                    emit(Result.Error(error = error))
                }

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun googleAuth(token: String): Flow<Result<User, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            remoteAuthenticationDataSource.googleAuth(token = token)
                .onSuccess { user, tokens ->
                    saveTokens(tokens)
                    emit(Result.Success(user.toUser()))
                }
                .onError { error ->
                    emit(Result.Error(error = error))
                }

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun register(email: String, firstName: String, lastName: String, password: String): Flow<Result<User, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            remoteAuthenticationDataSource.register(email = email, firstName = firstName, lastName = lastName, password = password)
                .onSuccess { user, tokens ->
                    saveTokens(tokens)
                    emit(Result.Success(user.toUser()))
                }
                .onError { error ->
                    emit(Result.Error(error = error))
                }

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun sendPasswordReset(email: String): EmptyResult<DataError.Remote> {
        return remoteAuthenticationDataSource.sendPasswordReset(email)
    }

    override suspend fun getMe(): Flow<Result<User, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            remoteAuthenticationDataSource.getMe()
                .onSuccess { user ->
                    emit(Result.Success(user.toUser()))
                }
                .onError { error ->
                    emit(Result.Error(error = error))
                }

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> {
        return remoteAuthenticationDataSource.logout().onSuccess { tokenManager.clearTokens() }
    }

    override suspend fun deleteSession(sessionId: Int): EmptyResult<DataError.Remote> {
        return remoteAuthenticationDataSource.deleteSession(sessionId)
    }

    private suspend fun saveTokens(tokens: Tokens){
        if(tokens.refresh != null)
            tokenManager.saveTokens(tokens.access, tokens.refresh)
    }

    override suspend fun sendPushNotificationToken(token: String): EmptyResult<DataError.Remote> {
        return remoteAuthenticationDataSource.sendPushNotificationToken(token)
    }
}