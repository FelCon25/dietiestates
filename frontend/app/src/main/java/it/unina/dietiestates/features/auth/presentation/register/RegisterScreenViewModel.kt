package it.unina.dietiestates.features.auth.presentation.register

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

class RegisterScreenViewModel(
    private val repository: AuthRepository
): ViewModel() {

    private val _eventsChannel = Channel<RegisterScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(RegisterScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: RegisterScreenEvent){
        when(event){
            is RegisterScreenEvent.OnRegisterSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(RegisterScreenEvent.OnRegisterSucceeded(event.user))
                }
            }
            is RegisterScreenEvent.OnRegisterFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(RegisterScreenEvent.OnRegisterFailed(event.message))
                }
            }
            is RegisterScreenEvent.OnGoogleAuthFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(RegisterScreenEvent.OnGoogleAuthFailed)
                }
            }
            is RegisterScreenEvent.OnWrongValueTextField -> {
                viewModelScope.launch {
                    _eventsChannel.send(RegisterScreenEvent.OnWrongValueTextField(event.message))
                }
            }
        }
    }

    fun onInputEmailValueChange(value: String){
        _state.update {
            it.copy(
                email = value
            )
        }
    }

    fun onInputFirstNameValueChange(value: String){
        _state.update {
            it.copy(
                firstName = value
            )
        }
    }

    fun onInputLastNameValueChange(value: String){
        _state.update {
            it.copy(
                lastName = value
            )
        }
    }

    fun onInputPasswordValueChange(value: String){
        _state.update {
            it.copy(
                password = value
            )
        }
    }

    fun submitRegister(){
        if(_state.value.email.isEmpty()){
            onEvent(RegisterScreenEvent.OnWrongValueTextField("Please insert an email!"))
        }
        else if(_state.value.firstName.isEmpty()){
            onEvent(RegisterScreenEvent.OnWrongValueTextField("Please insert a first name!"))
        }
        else if(_state.value.lastName.isEmpty()){
            onEvent(RegisterScreenEvent.OnWrongValueTextField("Please insert a last name!"))
        }
        else if(_state.value.password.length < 8){
            onEvent(RegisterScreenEvent.OnWrongValueTextField("Please enter a password of at least 8 characters!"))
        }
        else{
            viewModelScope.launch {
                repository.register(email = _state.value.email, firstName = _state.value.firstName, lastName = _state.value.lastName, password = _state.value.password).collect { result ->

                    result.apply {
                        onSuccess { user ->
                            onEvent(RegisterScreenEvent.OnRegisterSucceeded(user))
                        }

                        onError { error ->
                            when(error){
                                is DataError.Remote.CustomError -> onEvent(RegisterScreenEvent.OnRegisterFailed(error.errorMessage))
                                else -> onEvent(RegisterScreenEvent.OnRegisterFailed("There was a problem with registration, please try again."))
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
                        onEvent(RegisterScreenEvent.OnRegisterSucceeded(result.data))
                    }
                    is Result.Error -> {
                        onEvent(RegisterScreenEvent.OnGoogleAuthFailed)
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