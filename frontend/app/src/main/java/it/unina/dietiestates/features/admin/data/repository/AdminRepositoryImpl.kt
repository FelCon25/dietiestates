package it.unina.dietiestates.features.admin.data.repository

import it.unina.dietiestates.core.data.mappers.toUser
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.map
import it.unina.dietiestates.features.admin.data.mappers.toAgency
import it.unina.dietiestates.features.admin.data.remote.RemoteAdminDataSource
import it.unina.dietiestates.features.admin.domain.AdminRepository
import it.unina.dietiestates.features.admin.domain.Agency
import it.unina.dietiestates.features.admin.domain.Agent
import it.unina.dietiestates.features.admin.domain.Assistant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AdminRepositoryImpl(
    private val remoteAdminDataSource: RemoteAdminDataSource
): AdminRepository {


    override suspend fun getAgency(): Result<Agency, DataError.Remote> {
        return remoteAdminDataSource.getAgency().map { it.toAgency() }
    }

    override suspend fun getAssistants(): Result<List<Assistant>, DataError.Remote> {
        return remoteAdminDataSource.getAssistants().map { assistants ->
                assistants.map { it.toUser() } }
    }

    override suspend fun getAgents(): Result<List<Agent>, DataError.Remote> {
        return remoteAdminDataSource.getAgents().map { agents ->
                agents.map { it.toUser() } }
    }

    override suspend fun addAssistant(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?,
    ): Flow<Result<Assistant, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remoteAdminDataSource.addAssistant(email, firstName, lastName, password, phone).map { it.user.toUser() })

            emit(Result.IsLoading(false))
        }
    }

    override suspend fun addAgent(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?,
    ): Flow<Result<Assistant, DataError.Remote>> {
        return flow {
            emit(Result.IsLoading(true))

            emit(remoteAdminDataSource.addAgent(email, firstName, lastName, password, phone).map { it.user.toUser() })

            emit(Result.IsLoading(false))
        }
    }
}