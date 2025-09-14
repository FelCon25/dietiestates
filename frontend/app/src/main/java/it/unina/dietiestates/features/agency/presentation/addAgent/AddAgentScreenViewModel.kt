package it.unina.dietiestates.features.agency.presentation.addAgent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAgentScreenViewModel(
    private val repository: AgencyRepository
): ViewModel() {

    private val _eventsChannel = Channel<AddAgentScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AddAgentScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddAgentScreenEvent){
        when(event){
            is AddAgentScreenEvent.OnAddAgentSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AddAgentScreenEvent.OnAddAgentFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AddAgentScreenEvent.OnWrongValueTextField -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }


    fun addAssistant(){
        if(_state.value.email.isEmpty()){
            onEvent(AddAgentScreenEvent.OnWrongValueTextField("Please insert a valid email"))
        }
        else if(_state.value.firstName.isEmpty()){
            onEvent(AddAgentScreenEvent.OnWrongValueTextField("Please insert a valid first name"))
        }
        else if(_state.value.lastName.isEmpty()){
            onEvent(AddAgentScreenEvent.OnWrongValueTextField("Please insert a valid last name"))
        }
        else if(_state.value.password.length < 8){
            onEvent(AddAgentScreenEvent.OnWrongValueTextField("Please enter a password of at least 8 characters!"))
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
                            onEvent(AddAgentScreenEvent.OnAddAgentSucceeded(agent))
                        }

                        onError { error ->
                            when(error){
                                is DataError.Remote.CustomError -> onEvent(AddAgentScreenEvent.OnAddAgentFailed(error.errorMessage))
                                else -> onEvent(AddAgentScreenEvent.OnAddAgentFailed("There was an error while creating the new agent, please try again."))
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