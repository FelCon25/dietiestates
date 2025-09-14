package it.unina.dietiestates.features.agency.presentation.assistantScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
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
                agents = listOf(agent) + it.agents
            )
        }
    }
}