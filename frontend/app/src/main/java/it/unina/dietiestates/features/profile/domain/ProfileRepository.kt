package it.unina.dietiestates.features.profile.domain

import android.net.Uri
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.EmptyResult
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    suspend fun getMe(): Result<User, DataError.Remote>

    suspend fun getSessions(): Result<Pair<List<Session>, Int>, DataError.Remote>

    suspend fun changeProfilePic(image: Uri): Result<String, DataError.Remote>

    suspend fun getNotificationPreferences(): Result<List<NotificationCategory>, DataError.Remote>

    suspend fun setPropertyNotificationStatus(enabled: Boolean): Flow<EmptyResult<DataError.Remote>>

    suspend fun setPromotionalNotificationStatus(enabled: Boolean): Flow<EmptyResult<DataError.Remote>>
}