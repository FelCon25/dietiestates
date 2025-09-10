package it.unina.dietiestates.features.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.domain.AuthRepository
import it.unina.dietiestates.features.profile.domain.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository
): ViewModel() {

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
            authRepository.logout().onError {

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

                }
            }
        }
    }

}