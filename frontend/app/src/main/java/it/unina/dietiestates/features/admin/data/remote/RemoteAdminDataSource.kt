package it.unina.dietiestates.features.admin.data.remote

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.admin.data.dto.AgencyDto
import it.unina.dietiestates.features.admin.data.dto.AgentDto
import it.unina.dietiestates.features.admin.data.dto.AssistantDto
import it.unina.dietiestates.features.admin.data.dto.NewAgentDto
import it.unina.dietiestates.features.admin.data.dto.NewAssistantDto

interface RemoteAdminDataSource {

    suspend fun getAgency(): Result<AgencyDto, DataError.Remote>

    suspend fun getAssistants(): Result<List<AssistantDto>, DataError.Remote>

    suspend fun getAgents(): Result<List<AgentDto>, DataError.Remote>

    suspend fun addAssistant(email: String, firstName: String, lastName: String, password: String, phone: String?): Result<NewAssistantDto, DataError.Remote>

    suspend fun addAgent(email: String, firstName: String, lastName: String, password: String, phone: String?): Result<NewAgentDto, DataError.Remote>
}