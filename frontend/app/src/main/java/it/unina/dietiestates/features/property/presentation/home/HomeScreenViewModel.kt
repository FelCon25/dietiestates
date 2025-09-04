package it.unina.dietiestates.features.property.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.data.tokens.TokenManager
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val tokenManager: TokenManager
): ViewModel() {

    fun clearToken(){
        viewModelScope.launch {
            tokenManager.clearTokens()
        }
    }

}