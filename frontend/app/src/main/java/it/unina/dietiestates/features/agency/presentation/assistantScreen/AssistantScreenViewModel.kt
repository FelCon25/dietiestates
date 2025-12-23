package it.unina.dietiestates.features.agency.presentation.assistantScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import it.unina.dietiestates.features.agency.domain.Agent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantScreenViewModel(
    private val repository: AgencyRepository
): ViewModel() {

    private val _state = MutableStateFlow(AssistantScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            awaitAll(async { getAgency() }, async { getAgents() }).apply {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    suspend fun getAgency(){
        repository.getAgencyByAssistant().apply {
            onSuccess { agency ->
                _state.update {
                    it.copy(
                        agency = agency
                    )
                }
            }

            onError {

            }
        }
    }

    suspend fun getAgents(){
        repository.getAgents().apply {
            onSuccess { agents ->
                _state.update {
                    it.copy(
                        agents = agents
                    )
                }
            }
        }
    }

    fun onNewAgentAdded(agent: Agent){
        _state.update {
            it.copy(
                agents = listOf(agent) + it.agents,
                successMessage = "Agent added successfully"
            )
        }
    }

    fun deleteAgent(userId: Int) {
        viewModelScope.launch {
            repository.deleteAgent(userId).collect { result ->
                result.apply {
                    onSuccess { deletedUserId ->
                        _state.update {
                            it.copy(
                                agents = it.agents.filter { agent -> agent.userId != deletedUserId },
                                successMessage = "Agent deleted successfully"
                            )
                        }
                    }

                    onError { error ->
                        val message = when (error) {
                            is DataError.Remote.CustomError -> error.errorMessage
                            DataError.Remote.NoInternet -> "No internet connection"
                            DataError.Remote.Server -> "Server error. Please try again later"
                            else -> "Failed to delete agent"
                        }
                        _state.update { it.copy(errorMessage = message) }
                    }

                    onLoading { isLoading ->
                        _state.update { it.copy(isDeleting = isLoading) }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
