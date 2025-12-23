package it.unina.dietiestates.features.agency.presentation.adminScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import it.unina.dietiestates.features.agency.domain.Agent
import it.unina.dietiestates.features.agency.domain.Assistant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminScreenViewModel(
    private val repository: AgencyRepository
): ViewModel() {

    private val _state = MutableStateFlow(AdminScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            awaitAll(async { getAgency() }, async { getAssistants() }, async { getAgents() }).apply {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    suspend fun getAgency(){
        repository.getAgency().apply {
            onSuccess { agency ->
                _state.update {
                    it.copy(
                        agency = agency
                    )
                }
            }

            onError { error ->

            }
        }
    }

    suspend fun getAssistants(){
        repository.getAssistants().apply {
            onSuccess { assistants ->
                _state.update {
                    it.copy(
                        assistants = assistants
                    )
                }
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

    fun onNewAssistantAdded(assistant: Assistant){
        _state.update {
            it.copy(
                assistants = listOf(assistant) + it.assistants,
                successMessage = "Assistant added successfully"
            )
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

    fun deleteAssistant(userId: Int) {
        viewModelScope.launch {
            repository.deleteAssistant(userId).collect { result ->
                result.apply {
                    onSuccess { deletedUserId ->
                        _state.update {
                            it.copy(
                                assistants = it.assistants.filter { assistant -> assistant.userId != deletedUserId },
                                successMessage = "Assistant deleted successfully"
                            )
                        }
                    }

                    onError { error ->
                        val message = when (error) {
                            is DataError.Remote.CustomError -> error.errorMessage
                            DataError.Remote.NoInternet -> "No internet connection"
                            DataError.Remote.Server -> "Server error. Please try again later"
                            else -> "Failed to delete assistant"
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
