package it.unina.dietiestates.features.admin.presentation.addAssistant

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

class AdminAddAssistantScreenViewModel(
    private val repository: AdminRepository
): ViewModel() {

    private val _eventsChannel = Channel<AdminAddAssistantScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AdminAddAssistantScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: AdminAddAssistantScreenEvent){
        when(event){
            is AdminAddAssistantScreenEvent.OnAddAssistantSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
            is AdminAddAssistantScreenEvent.OnAddAssistantFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
            is AdminAddAssistantScreenEvent.OnWrongValueTextField -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }


    fun addAssistant(){
        if(_state.value.email.isEmpty()){
            onEvent(AdminAddAssistantScreenEvent.OnWrongValueTextField("Please insert a valid email"))
        }
        else if(_state.value.firstName.isEmpty()){
            onEvent(AdminAddAssistantScreenEvent.OnWrongValueTextField("Please insert a valid first name"))
        }
        else if(_state.value.lastName.isEmpty()){
            onEvent(AdminAddAssistantScreenEvent.OnWrongValueTextField("Please insert a valid last name"))
        }
        else if(_state.value.password.length < 8){
            onEvent(AdminAddAssistantScreenEvent.OnWrongValueTextField("Please enter a password of at least 8 characters!"))
        }
        else{
            viewModelScope.launch {
                repository.addAssistant(
                    email = _state.value.email,
                    firstName = _state.value.firstName,
                    lastName = _state.value.lastName,
                    password = _state.value.password,
                    phone = _state.value.phone
                ).collect { result ->
                    result.apply {
                        onSuccess { assistant ->
                            onEvent(AdminAddAssistantScreenEvent.OnAddAssistantSucceeded(assistant))
                        }

                        onError { error ->
                            when(error){
                                is DataError.Remote.CustomError -> onEvent(
                                    AdminAddAssistantScreenEvent.OnAddAssistantFailed(error.errorMessage))
                                else -> onEvent(AdminAddAssistantScreenEvent.OnAddAssistantFailed("There was an error while creating the new assistant, please try again."))
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