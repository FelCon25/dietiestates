package it.unina.dietiestates.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
    val tokenManager: TokenManager,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _eventsChannel = Channel<MainScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tokenManager.onTokenCleared {
                if(_state.value.user != null){
                    println("Token cleared")
                    onEvent(MainScreenEvent.OnLogout)
                }
            }
        }

        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken()
            _state.update {
                it.copy(
                    startDestination = if (accessToken != null) Route.UserGraph else Route.AuthGraph
                )
            }

            if(accessToken != null){
                getMe()
            }
        }
    }

    fun onEvent(event: MainScreenEvent){
        when(event){
            is MainScreenEvent.OnLogout -> {
                viewModelScope.launch {
                    _eventsChannel.send(MainScreenEvent.OnLogout)
                }
            }
        }
    }

    fun addUserFromAuth(user: User){
        _state.update {
            it.copy(user = user)
        }
    }


    suspend fun getMe(){
        authRepository.getMe().collect { result ->
            when(result){
                is Result.Success -> {
                    _state.update {
                        it.copy(user = result.data)
                    }

                    println("User fetched: ${_state.value.user?.email}")
                }

                is Result.Error -> {
                    //println("Error: ${result.error.name}")
                }

                is Result.IsLoading -> {

                }
            }
        }
    }

}