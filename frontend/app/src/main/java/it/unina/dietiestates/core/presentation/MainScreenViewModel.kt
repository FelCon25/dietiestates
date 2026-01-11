package it.unina.dietiestates.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

            if(accessToken == null){
                _state.update {
                    it.copy(
                        startDestination = Route.AuthGraph,
                        isReady = true
                    )
                }
            }
            else{
                getMe()
            }
        }
    }

    fun onEvent(event: MainScreenEvent){
        when(event){
            is MainScreenEvent.OnSignIn -> {
                _state.update {
                    it.copy(user = event.user)
                }
                if(event.user.role == "USER"){
                    onEvent(MainScreenEvent.OnSendPushNotificationToken)
                }
            }

            is MainScreenEvent.OnLogout -> {
                _state.update {
                    it.copy(
                        user = null,
                        startDestination = Route.AuthGraph,
                        isReady = true
                    )
                }
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is MainScreenEvent.OnReceivedPushNotification -> {
                _state.update {
                    it.copy(
                        propertyIdFromNotification = event.propertyId
                    )
                }
            }

            is MainScreenEvent.OnSendPushNotificationToken -> {
                viewModelScope.launch {
                    val token = FirebaseMessaging.getInstance().token.await()
                    authRepository.sendPushNotificationToken(token)
                }
            }
        }
    }

    suspend fun getMe(){
        authRepository.getMe().collect { result ->

            result.apply {
                onSuccess { user ->
                    _state.update {
                        it.copy(
                            user = user,
                            startDestination = getStartDestinationFromRole(user.role)
                        )
                    }
                    if(user.role == "USER"){
                        onEvent(MainScreenEvent.OnSendPushNotificationToken)
                    }
                }

                onError { error ->
                    when(error){
                        is DataError.Remote.Unauthorized -> {
                            _state.update {
                                it.copy(startDestination = Route.AuthGraph)
                            }
                        }
                        else -> {
                            println("Error: $error")
                        }
                    }
                }

                onLoading { isLoading ->
                    if(!isLoading){
                        _state.update {
                            it.copy(isReady = true)
                        }
                    }
                }
            }
        }
    }

    fun getStartDestinationFromRole(role: String): Route{
        return when(role){
            "USER" -> Route.UserGraph
            "ASSISTANT" -> Route.AssistantGraph
            "AGENT" -> Route.AgentGraph
            "ADMIN_AGENCY" -> Route.AdminGraph
            else -> Route.UserGraph
        }
    }
}
