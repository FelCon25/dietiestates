package it.unina.dietiestates.features.agency.domain

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AgencyRepository {

    suspend fun getAgency(): Result<Agency, DataError.Remote>

    suspend fun getAgencyByAssistant(): Result<Agency, DataError.Remote>

    suspend fun getAgencyByAgent(): Result<Agency, DataError.Remote>

    suspend fun getAssistants(): Result<List<Assistant>, DataError.Remote>

    suspend fun getAgents(): Result<List<Agent>, DataError.Remote>

    suspend fun addAssistant(email: String, firstName: String, lastName: String, password: String, phone: String?): Flow<Result<Assistant, DataError.Remote>>

    suspend fun addAgent(email: String, firstName: String, lastName: String, password: String, phone: String?): Flow<Result<Agent, DataError.Remote>>

    suspend fun deleteAssistant(userId: Int): Flow<Result<Int, DataError.Remote>>

    suspend fun deleteAgent(userId: Int): Flow<Result<Int, DataError.Remote>>
}
