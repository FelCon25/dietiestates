package it.unina.dietiestates.features.agency.presentation.agentScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgentScreenViewModel(
    private val agencyRepository: AgencyRepository,
    private val propertyRepository: PropertyRepository
): ViewModel() {

    private val _state = MutableStateFlow(AgentScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            awaitAll(async { getAgency() }, async { getProperties() }).apply {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }


    suspend fun getAgency(){
        agencyRepository.getAgencyByAgent().apply {
            onSuccess { agency ->
                _state.update {
                    it.copy(
                        agency = agency
                    )
                }
            }

            onError { error ->
                println("No agency found $error")
            }
        }
    }

    suspend fun getProperties(){
        propertyRepository.getAgentProperties().apply {
            onSuccess { properties ->
                _state.update {
                    it.copy(
                        properties = properties
                    )
                }
            }
        }
    }

    fun onNewPropertyAdded(property: Property){
        _state.update {
            it.copy(
                properties = listOf(property) + it.properties
            )
        }
    }
}