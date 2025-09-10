package it.unina.dietiestates.features.admin.domain

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AdminRepository {

    suspend fun getAgency(): Result<Agency, DataError.Remote>

    suspend fun getAssistants(): Result<List<Assistant>, DataError.Remote>

    suspend fun getAgents(): Result<List<Agent>, DataError.Remote>

    suspend fun addAssistant(email: String, firstName: String, lastName: String, password: String, phone: String?): Flow<Result<Assistant, DataError.Remote>>

    suspend fun addAgent(email: String, firstName: String, lastName: String, password: String, phone: String?): Flow<Result<Agent, DataError.Remote>>
}