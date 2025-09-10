package it.unina.dietiestates.features.admin.presentation.addAgent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.admin.domain.AdminRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminAddAgentScreenViewModel(
    private val repository: AdminRepository
): ViewModel() {

    private val _eventsChannel = Channel<AdminAddAgentScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AdminAddAgentScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: AdminAddAgentScreenEvent){
        when(event){
            is AdminAddAgentScreenEvent.OnAddAgentSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AdminAddAgentScreenEvent.OnAddAgentFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AdminAddAgentScreenEvent.OnWrongValueTextField -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }


    fun addAssistant(){
        if(_state.value.email.isEmpty()){
            onEvent(AdminAddAgentScreenEvent.OnWrongValueTextField("Please insert a valid email"))
        }
        else if(_state.value.firstName.isEmpty()){
            onEvent(AdminAddAgentScreenEvent.OnWrongValueTextField("Please insert a valid first name"))
        }
        else if(_state.value.lastName.isEmpty()){
            onEvent(AdminAddAgentScreenEvent.OnWrongValueTextField("Please insert a valid last name"))
        }
        else if(_state.value.password.length < 8){
            onEvent(AdminAddAgentScreenEvent.OnWrongValueTextField("Please enter a password of at least 8 characters!"))
        }
        else{
            viewModelScope.launch {
                repository.addAgent(
                    email = _state.value.email,
                    firstName = _state.value.firstName,
                    lastName = _state.value.lastName,
                    password = _state.value.password,
                    phone = _state.value.phone
                ).collect { result ->
                    result.apply {
                        onSuccess { agent ->
                            onEvent(AdminAddAgentScreenEvent.OnAddAgentSucceeded(agent))
                        }

                        onError { error ->
                            when(error){
                                is DataError.Remote.CustomError -> onEvent(AdminAddAgentScreenEvent.OnAddAgentFailed(error.errorMessage))
                                else -> onEvent(AdminAddAgentScreenEvent.OnAddAgentFailed("There was an error while creating the new agent, please try again."))
                            }
                        }

                        onLoading { isLoading ->
                            _state.update {
                                it.copy(
                                    isLoading = isLoading
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    fun onInputEmailChange(email: String){
        _state.update {
            it.copy(email = email)
        }
    }

    fun onInputFirstNameChange(firstName: String){
        _state.update {
            it.copy(firstName = firstName)
        }
    }

    fun onInputLastNameChange(lastName: String){
        _state.update {
            it.copy(lastName = lastName)
        }
    }

    fun onInputPasswordChange(password: String){
        _state.update {
            it.copy(password = password)
        }
    }

    fun onInputPhoneChange(phone: String){
        _state.update {
            it.copy(phone = phone)
        }
    }
}