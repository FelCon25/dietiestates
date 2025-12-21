package it.unina.dietiestates.features.auth.presentation.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false
)

sealed interface ForgotPasswordScreenEvent {
    data object OnCodeSent : ForgotPasswordScreenEvent
    data class OnError(val message: String) : ForgotPasswordScreenEvent
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    private val eventsChannel = Channel<ForgotPasswordScreenEvent>()
    val eventsChannelFlow = eventsChannel.receiveAsFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun sendResetCode() {
        viewModelScope.launch {
            val email = _state.value.email.trim()

            if (email.isEmpty()) {
                eventsChannel.send(ForgotPasswordScreenEvent.OnError("Please enter your email"))
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                eventsChannel.send(ForgotPasswordScreenEvent.OnError("Please enter a valid email"))
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            authRepository.sendPasswordReset(email)
                .onSuccess {
                    eventsChannel.send(ForgotPasswordScreenEvent.OnCodeSent)
                }
                .onError { error ->
                    val message = when(error) {
                        is DataError.Remote.CustomError -> error.errorMessage
                        DataError.Remote.NoInternet -> "No internet connection"
                        DataError.Remote.TooManyRequest -> "Too many requests. Please try again later"
                        DataError.Remote.Server -> "Server error. Please try again later"
                        DataError.Remote.RequestTimeout -> "Request timeout. Please check your connection"
                        else -> "Failed to send reset code. Please try again"
                    }
                    eventsChannel.send(ForgotPasswordScreenEvent.OnError(message))
                }

            _state.update { it.copy(isLoading = false) }
        }
    }
}

