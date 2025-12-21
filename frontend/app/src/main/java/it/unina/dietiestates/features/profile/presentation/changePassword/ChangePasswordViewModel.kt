package it.unina.dietiestates.features.profile.presentation.changePassword

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

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val logoutOtherDevices: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ChangePasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state = _state.asStateFlow()

    fun onCurrentPasswordChange(password: String) {
        _state.update { it.copy(currentPassword = password, errorMessage = null, successMessage = null) }
    }

    fun onNewPasswordChange(password: String) {
        _state.update { it.copy(newPassword = password, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _state.update { it.copy(confirmPassword = password, errorMessage = null, successMessage = null) }
    }

    fun onLogoutOtherDevicesChange(logout: Boolean) {
        _state.update { it.copy(logoutOtherDevices = logout) }
    }

    fun changePassword() {
        viewModelScope.launch {
            val currentPassword = _state.value.currentPassword
            val newPassword = _state.value.newPassword
            val confirmPassword = _state.value.confirmPassword

            // Reset messages
            _state.update { it.copy(errorMessage = null, successMessage = null) }

            if (currentPassword.isEmpty()) {
                _state.update { it.copy(errorMessage = "Please enter your current password") }
                return@launch
            }

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

            if (currentPassword == newPassword) {
                _state.update { it.copy(errorMessage = "New password must be different from current password") }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            val logoutOtherDevices = _state.value.logoutOtherDevices

            authRepository.changePassword(currentPassword, newPassword, logoutOtherDevices)
                .onSuccess {
                    val successMsg = if (logoutOtherDevices) {
                        "Password changed successfully! Other devices logged out."
                    } else {
                        "Password changed successfully!"
                    }
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = successMsg,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = ""
                        )
                    }
                }
                .onError { error ->
                    val message = when(error) {
                        is DataError.Remote.CustomError -> error.errorMessage
                        DataError.Remote.NoInternet -> "No internet connection"
                        DataError.Remote.Unauthorized -> "Session expired. Please login again"
                        else -> "Failed to change password"
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }
}


