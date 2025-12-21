package it.unina.dietiestates.features.auth.presentation.resetPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordState(
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val codeVerified: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shouldNavigate: Boolean = false
)

class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state = _state.asStateFlow()

    fun onCodeChange(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _state.update { it.copy(code = code, errorMessage = null, successMessage = null) }
        }
    }

    fun onNewPasswordChange(password: String) {
        _state.update { it.copy(newPassword = password, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _state.update { it.copy(confirmPassword = password, errorMessage = null, successMessage = null) }
    }

    fun verifyCode() {
        viewModelScope.launch {
            val code = _state.value.code

            if (code.length != 6) {
                _state.update { it.copy(errorMessage = "Code must be 6 digits") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.verifyPasswordResetCode(code)
                .onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            codeVerified = true,
                            errorMessage = null
                        )
                    }
                }
                .onError { error ->
                    val message = when(error) {
                        is DataError.Remote.CustomError -> error.errorMessage
                        DataError.Remote.NoInternet -> "No internet connection"
                        DataError.Remote.Server -> "Server error. Please try again later"
                        DataError.Remote.RequestTimeout -> "Request timeout. Please check your connection"
                        else -> "Invalid or expired code"
                    }
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = message,
                            codeVerified = false
                        )
                    }
                }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            val code = _state.value.code
            val newPassword = _state.value.newPassword
            val confirmPassword = _state.value.confirmPassword

            // Reset messages
            _state.update { it.copy(errorMessage = null, successMessage = null) }

            if (newPassword.isEmpty()) {
                _state.update { it.copy(errorMessage = "Please enter a new password") }
                return@launch
            }

            if (newPassword.length < 8) {
                _state.update { it.copy(errorMessage = "Password must be at least 8 characters") }
                return@launch
            }

            if (newPassword != confirmPassword) {
                _state.update { it.copy(errorMessage = "Passwords do not match") }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            authRepository.passwordReset(code, newPassword)
                .onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            shouldNavigate = true
                        )
                    }
                }
                .onError { error ->
                    val message = when(error) {
                        is DataError.Remote.CustomError -> error.errorMessage
                        DataError.Remote.NoInternet -> "No internet connection"
                        DataError.Remote.Unauthorized -> "Invalid or expired code"
                        else -> "Failed to reset password"
                    }
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = message,
                            codeVerified = false // Go back to code entry if error
                        )
                    }
                }
        }
    }
}
