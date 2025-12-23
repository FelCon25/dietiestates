package it.unina.dietiestates.features.agency.data.remote

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.agency.data.dto.AgencyDto
import it.unina.dietiestates.features.agency.data.dto.AgentDto
import it.unina.dietiestates.features.agency.data.dto.AssistantDto
import it.unina.dietiestates.features.agency.data.dto.DeleteResponseDto
import it.unina.dietiestates.features.agency.data.dto.NewAgentDto
import it.unina.dietiestates.features.agency.data.dto.NewAssistantDto

interface RemoteAgencyDataSource {

    suspend fun getAgency(): Result<AgencyDto, DataError.Remote>

    suspend fun getAgencyByAssistant(): Result<AgencyDto, DataError.Remote>

    suspend fun getAgencyByAgent(): Result<AgencyDto, DataError.Remote>

    suspend fun getAssistants(): Result<List<AssistantDto>, DataError.Remote>

    suspend fun getAgents(): Result<List<AgentDto>, DataError.Remote>

    suspend fun addAssistant(email: String, firstName: String, lastName: String, password: String, phone: String?): Result<NewAssistantDto, DataError.Remote>

    suspend fun addAgent(email: String, firstName: String, lastName: String, password: String, phone: String?): Result<NewAgentDto, DataError.Remote>

    suspend fun deleteAssistant(userId: Int): Result<DeleteResponseDto, DataError.Remote>

    suspend fun deleteAgent(userId: Int): Result<DeleteResponseDto, DataError.Remote>
}
