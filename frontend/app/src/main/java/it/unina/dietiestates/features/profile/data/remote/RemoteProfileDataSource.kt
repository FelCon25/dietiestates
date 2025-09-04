package it.unina.dietiestates.features.profile.data.remote

import it.unina.dietiestates.core.data.dto.UserDto
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.profile.data.dto.NotificationPreferencesDto
import it.unina.dietiestates.features.profile.data.dto.ProfilePictureDto
import it.unina.dietiestates.features.profile.data.dto.SessionsResponse

interface RemoteProfileDataSource {

    suspend fun getMe(): Result<UserDto, DataError.Remote>

    suspend fun getSessions(): Result<SessionsResponse, DataError.Remote>

    suspend fun changeProfilePic(imageBytes: ByteArray, fileName: String, imageExt: String): Result<ProfilePictureDto, DataError.Remote>

    suspend fun getNotificationPreferences(): Result<List<NotificationPreferencesDto>, DataError.Remote>
}