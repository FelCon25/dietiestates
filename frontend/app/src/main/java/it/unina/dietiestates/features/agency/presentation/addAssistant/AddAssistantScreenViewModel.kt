package it.unina.dietiestates.features.agency.presentation.addAssistant

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

class AddAssistantScreenViewModel(
    private val repository: AgencyRepository
): ViewModel() {

    private val _eventsChannel = Channel<AddAssistantScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AddAssistantScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddAssistantScreenEvent){
        when(event){
            is AddAssistantScreenEvent.OnAddAssistantSucceeded -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
            is AddAssistantScreenEvent.OnAddAssistantFailed -> {
                _state.update { it.copy(errorMessage = event.message, successMessage = null) }
            }
            is AddAssistantScreenEvent.OnWrongValueTextField -> {
                _state.update { it.copy(errorMessage = event.message, successMessage = null) }
            }
        }
    }

    fun clearMessages() {
        _state.update { 
            it.copy(
                errorMessage = null, 
                successMessage = null,
                emailError = false,
                firstNameError = false,
                lastNameError = false,
                passwordError = false
            ) 
        }
    }

    fun addAssistant(){
        clearMessages()
        
        var hasError = false
        
        if(_state.value.email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()){
            _state.update { it.copy(emailError = true, errorMessage = "Please enter a valid email address") }
            hasError = true
        }
        if(_state.value.firstName.isEmpty()){
            _state.update { it.copy(firstNameError = true, errorMessage = if (hasError) it.errorMessage else "Please enter the first name") }
            hasError = true
        }
        if(_state.value.lastName.isEmpty()){
            _state.update { it.copy(lastNameError = true, errorMessage = if (hasError) it.errorMessage else "Please enter the last name") }
            hasError = true
        }
        if(_state.value.password.length < 8){
            _state.update { it.copy(passwordError = true, errorMessage = if (hasError) it.errorMessage else "Password must be at least 8 characters") }
            hasError = true
        }
        
        if (hasError) {
            // Show combined error message if multiple fields have errors
            if (listOf(_state.value.emailError, _state.value.firstNameError, _state.value.lastNameError, _state.value.passwordError).count { it } > 1) {
                _state.update { it.copy(errorMessage = "Please fill in all required fields correctly") }
            }
            return
        }

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
                        onEvent(AddAssistantScreenEvent.OnAddAssistantSucceeded(assistant))
                    }

                    onError { error ->
                        val message = when(error){
                            is DataError.Remote.CustomError -> error.errorMessage
                            DataError.Remote.NoInternet -> "No internet connection"
                            DataError.Remote.Server -> "Server error. Please try again later"
                            else -> "Failed to create assistant. Please try again."
                        }
                        _state.update { it.copy(errorMessage = message) }
                    }

                    onLoading { isLoading ->
                        _state.update { it.copy(isLoading = isLoading) }
                    }
                }
            }
        }
    }

    fun onInputEmailChange(email: String){
        _state.update { it.copy(email = email, emailError = false, errorMessage = null) }
    }

    fun onInputFirstNameChange(firstName: String){
        _state.update { it.copy(firstName = firstName, firstNameError = false, errorMessage = null) }
    }

    fun onInputLastNameChange(lastName: String){
        _state.update { it.copy(lastName = lastName, lastNameError = false, errorMessage = null) }
    }

    fun onInputPasswordChange(password: String){
        _state.update { it.copy(password = password, passwordError = false, errorMessage = null) }
    }

    fun onInputPhoneChange(phone: String){
        _state.update { it.copy(phone = phone) }
    }
}
