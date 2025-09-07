package it.unina.dietiestates.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
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

class SignInScreenViewModel(
    private val repository: AuthRepository
): ViewModel() {

    private val _eventsChannel = Channel<SignInScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()
    private val _state = MutableStateFlow(SignInScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: SignInScreenEvent){
        when(event){
            is SignInScreenEvent.OnSignInSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(SignInScreenEvent.OnSignInSucceeded(user = event.user))
                }
            }

            is SignInScreenEvent.OnSignFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(SignInScreenEvent.OnSignFailed(event.message))
                }
            }

            is SignInScreenEvent.OnGoogleAuthFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(SignInScreenEvent.OnGoogleAuthFailed)
                }
            }

            is SignInScreenEvent.OnWrongValueTextField -> {
                viewModelScope.launch {
                    _eventsChannel.send(SignInScreenEvent.OnWrongValueTextField(event.message))
                }
            }
        }
    }

    fun onInputEmailChange(email: String){
        _state.update {
            it.copy(email = email)
        }
    }

    fun onInputPasswordChange(password: String){
        _state.update {
            it.copy(password = password)
        }
    }

    fun submitSignIn(){
        if(_state.value.email.isEmpty()){
            onEvent(SignInScreenEvent.OnWrongValueTextField("Please insert an email!"))
        }
        else if(_state.value.password.isEmpty()){
            onEvent(SignInScreenEvent.OnWrongValueTextField("Please insert a password!"))
        }
        else if(_state.value.password.length < 8){
            onEvent(SignInScreenEvent.OnWrongValueTextField("Please enter a password of at least 8 characters!"))
        }
        else{
            viewModelScope.launch {

                repository.signIn(email = _state.value.email, password = _state.value.password).collect{ result ->
                    result.apply {

                        onSuccess { user ->
                            onEvent(SignInScreenEvent.OnSignInSucceeded(user))
                        }

                        onError { error ->
                            when(error){
                                is DataError.Remote.CustomError -> onEvent(SignInScreenEvent.OnSignFailed(error.errorMessage))
                                else -> onEvent(SignInScreenEvent.OnSignFailed("There was a problem with authentication, please try again."))
                            }
                        }

                        onLoading { isLoading ->
                            _state.update {
                                it.copy(isLoading = isLoading)
                            }
                        }
                    }
                }
            }
        }
    }

    fun sendGoogleAuth(token: String){
        viewModelScope.launch {
            repository.googleAuth(token).collect { result ->
                when(result){
                    is Result.Success -> {
                        onEvent(SignInScreenEvent.OnSignInSucceeded(result.data))
                    }
                    is Result.Error -> {
                        onEvent(SignInScreenEvent.OnGoogleAuthFailed)
                    }
                    is Result.IsLoading -> {
                        _state.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }
                }
            }
        }
    }
}