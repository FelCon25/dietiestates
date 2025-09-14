package it.unina.dietiestates.features.agency.data.repository

import it.unina.dietiestates.core.data.mappers.toUser
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.map
import it.unina.dietiestates.features.agency.data.mappers.toAgency
import it.unina.dietiestates.features.agency.data.remote.RemoteAgencyDataSource
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import it.unina.dietiestates.features.agency.domain.Agency
import it.unina.dietiestates.features.agency.domain.Agent
import it.unina.dietiestates.features.agency.domain.Assistant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AgencyRepositoryImpl(
    private val remoteAdminDataSource: RemoteAgencyDataSource
): AgencyRepository {


    override suspend fun getAgency(): Result<Agency, DataError.Remote> {
        return remoteAdminDataSource.getAgency().map { it.toAgency() }
    }

    override suspend fun getAgencyByAssistant(): Result<Agency, DataError.Remote> {
        return remoteAdminDataSource.getAgencyByAssistant().map { it.toAgency() }
    }

    override suspend fun getAgencyByAgent(): Result<Agency, DataError.Remote> {
        return remoteAdminDataSource.getAgencyByAgent().map { it.toAgency() }
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