package it.unina.dietiestates.features.profile.data.repository

import android.net.Uri
import it.unina.dietiestates.core.data.FileReader
import it.unina.dietiestates.core.data.mappers.toUser
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.core.domain.map
import it.unina.dietiestates.features.profile.data.mappers.toNotificationPreferences
import it.unina.dietiestates.features.profile.data.mappers.toSession
import it.unina.dietiestates.features.profile.data.remote.RemoteProfileDataSource
import it.unina.dietiestates.features.profile.domain.NotificationPreferences
import it.unina.dietiestates.features.profile.domain.ProfileRepository
import it.unina.dietiestates.features.profile.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProfileRepositoryImpl(
    private val remoteProfileDataSource: RemoteProfileDataSource,
    private val fileReader: FileReader
): ProfileRepository {

    override suspend fun getMe(): Result<User, DataError.Remote> {
        return remoteProfileDataSource.getMe()
            .map { user ->
                user.toUser()
            }
    }

    override suspend fun getSessions(): Result<Pair<List<Session>, Int>, DataError.Remote> {
        return remoteProfileDataSource.getSessions()
            .map { sessionResult ->
                Pair(sessionResult.sessions.map { it.toSession() }, sessionResult.currentSessionId)
            }
    }

    override suspend fun changeProfilePic(image: Uri): Result<String, DataError.Remote> {
        val fileInfo = fileReader.uriToFileInfo(image)
        return remoteProfileDataSource.changeProfilePic(imageBytes = fileInfo.bytes, fileName = fileInfo.name, imageExt = fileInfo.mimeType).map { it.profilePic }
    }

    override suspend fun getNotificationPreferences(): Result<List<NotificationPreferences>, DataError.Remote> {
        return remoteProfileDataSource.getNotificationPreferences().map {
            it.map { it.toNotificationPreferences() }
        }
    }

    override suspend fun setPropertyNotificationStatus(enabled: Boolean): Flow<EmptyResult<DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remoteProfileDataSource.setPropertyNotificationStatus(enabled))


            emit(Result.IsLoading(false))
        }
    }

    override suspend fun setPromotionalNotificationStatus(enabled: Boolean): Flow<EmptyResult<DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remoteProfileDataSource.setPromotionalNotificationStatus(enabled))

            emit(Result.IsLoading(false))
        }
    }
}