package it.unina.dietiestates.features.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.domain.AuthRepository
import it.unina.dietiestates.features.profile.domain.NotificationCategory
import it.unina.dietiestates.features.profile.domain.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _eventsChannel = Channel<ProfileScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            awaitAll(
                async {  getMe() }, async { getSessions() }, async { getNotificationPreferences() }
            ).apply {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun onEvent(event: ProfileScreenEvent){
        when(event){
            is ProfileScreenEvent.OnLogoutFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is ProfileScreenEvent.OnSendPasswordResetFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is ProfileScreenEvent.OnChangingNotificationStatusFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is ProfileScreenEvent.OnDeletingSessionRequested -> {
                _state.update {
                    it.copy(sessionToDelete = event.sessionId)
                }
            }

            is ProfileScreenEvent.OnDeletingSessionCanceled -> {
                _state.update {
                    it.copy(sessionToDelete = null)
                }
            }
            is ProfileScreenEvent.OnDeletingSessionConfirmed -> {
                _state.value.sessionToDelete?.let { sessionId ->
                    deleteSession(sessionId)
                }
            }
            is ProfileScreenEvent.OnDeletingSessionFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }

    private suspend fun getMe(){
        repository.getMe()
            .onSuccess { user ->
                _state.update {
                    it.copy(user = user)
                }
            }
            .onError {

            }
    }

    private suspend fun getSessions(){
        repository.getSessions()
            .onSuccess { (sessions, currentSessionId) ->
                _state.update {
                    it.copy(
                        sessions = sessions.filter { it.sessionId == currentSessionId } + sessions.filterNot { it.sessionId == currentSessionId },
                        currentSessionId = currentSessionId
                    )
                }
            }
            .onError {

            }
    }

    private suspend fun getNotificationPreferences(){
        repository.getNotificationPreferences()
            .onSuccess { notificationPreferences ->
                _state.update {
                    it.copy(
                        notificationPreferences = notificationPreferences
                    )
                }
            }
            .onError {

            }
    }

    fun changeProfilePic(image: Uri){
        viewModelScope.launch {
            repository.changeProfilePic(image).onSuccess { newPfpUrl ->
                _state.update {
                    it.copy(
                        user = _state.value.user?.copy(profilePic = newPfpUrl)
                    )
                }
            }
        }
    }

    fun logout(){
        viewModelScope.launch {
            authRepository.logout().apply {
                onError {
                    onEvent(ProfileScreenEvent.OnLogoutFailed("There was an error logging out."))
                }
            }
        }
    }

    fun sendPasswordReset(){
        val email = _state.value.user?.email ?: return

        viewModelScope.launch {
            authRepository.sendPasswordReset(email).apply {
                onError { error ->
                    when(error){
                        is DataError.Remote.TooManyRequest -> {
                            onEvent(ProfileScreenEvent.OnSendPasswordResetFailed("You have requested too many password resets. Please wait a few minutes before trying again."))
                        }
                        else -> {
                            onEvent(ProfileScreenEvent.OnSendPasswordResetFailed("There was an error sending the password reset email."))
                        }
                    }
                }
            }
        }
    }

    fun deleteSession(sessionId: Int){
        viewModelScope.launch {
            authRepository.deleteSession(sessionId).apply {
                onSuccess {
                    _state.update {
                        it.copy(
                            sessions = _state.value.sessions.filterNot { it.sessionId == sessionId }
                        )
                    }
                }

                onError {
                    onEvent(ProfileScreenEvent.OnDeletingSessionCanceled)
                    onEvent(ProfileScreenEvent.OnDeletingSessionFailed("There was an error deleting the session."))
                }
            }
        }
    }

    fun setPropertyNotificationStatus(enabled: Boolean){
        viewModelScope.launch {
            updateNotificationPreferencesState(NotificationCategory.NEW_PROPERTY_MATCH, enabled)

            repository.setPropertyNotificationStatus(enabled).collect { result ->
                result.apply {
                    onError {
                        updateNotificationPreferencesState(NotificationCategory.NEW_PROPERTY_MATCH, !enabled)
                        onEvent(ProfileScreenEvent.OnChangingNotificationStatusFailed("There was an error changing the notification status."))
                    }

                    onLoading { isLoading ->
                        _state.update {
                            it.copy(
                                isPropertyNotificationStatusChanging = isLoading
                            )
                        }
                    }
                }
            }
        }
    }

    fun setPromotionalNotificationStatus(enabled: Boolean){
        viewModelScope.launch {
            updateNotificationPreferencesState(NotificationCategory.PROMOTIONAL, enabled)

            repository.setPromotionalNotificationStatus(enabled).collect { result ->
                result.apply {
                    onError {
                        updateNotificationPreferencesState(NotificationCategory.PROMOTIONAL, !enabled)
                        onEvent(ProfileScreenEvent.OnChangingNotificationStatusFailed("There was an error changing the notification status."))
                    }

                    onLoading { isLoading ->
                        _state.update {
                            it.copy(
                                isPromotionalNotificationStatusChanging = isLoading
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateNotificationPreferencesState(category: NotificationCategory, enabled: Boolean){
        _state.update {
            it.copy(
                notificationPreferences = it.notificationPreferences.toMutableList().apply {
                    if(enabled)
                        add(category)
                    else
                        remove(category)
                }
            )
        }
    }

}