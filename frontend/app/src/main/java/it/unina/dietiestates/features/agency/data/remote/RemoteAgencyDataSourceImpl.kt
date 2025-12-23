package it.unina.dietiestates.features.agency.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.unina.dietiestates.BuildConfig.BASE_URL
import it.unina.dietiestates.core.data.safeCall
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.features.agency.data.dto.AgencyDto
import it.unina.dietiestates.features.agency.data.dto.AgentDto
import it.unina.dietiestates.features.agency.data.dto.AssistantDto
import it.unina.dietiestates.features.agency.data.dto.DeleteResponseDto
import it.unina.dietiestates.features.agency.data.dto.NewAgentDto
import it.unina.dietiestates.features.agency.data.dto.NewAssistantDto

class RemoteAgencyDataSourceImpl(
    private val httpClient: HttpClient
): RemoteAgencyDataSource {

    override suspend fun getAgency(): Result<AgencyDto, DataError.Remote> {
        return safeCall<AgencyDto> {
            httpClient.get("$BASE_URL/agency/by-admin")
        }
    }

    override suspend fun getAgencyByAssistant(): Result<AgencyDto, DataError.Remote> {
        return safeCall<AgencyDto> {
            httpClient.get("$BASE_URL/agency/by-assistant")
        }
    }

    override suspend fun getAgencyByAgent(): Result<AgencyDto, DataError.Remote> {
        return safeCall<AgencyDto> {
            httpClient.get("$BASE_URL/agency/by-agent")
        }
    }

    override suspend fun getAssistants(): Result<List<AssistantDto>, DataError.Remote> {
        return safeCall<List<AssistantDto>> {
            httpClient.get("$BASE_URL/agency-admin/assistants")
        }
    }

    override suspend fun getAgents(): Result<List<AgentDto>, DataError.Remote> {
        return safeCall<List<AgentDto>> {
            httpClient.get("$BASE_URL/agency-admin/agents")
        }
    }

    override suspend fun addAssistant(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?,
    ): Result<NewAssistantDto, DataError.Remote> {
        return safeCall<NewAssistantDto> {
            httpClient.post("$BASE_URL/agency-admin/assistant"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "password" to password,
                        "phone" to phone
                    )
                )
            }
        }
    }

    override suspend fun addAgent(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?
    ): Result<NewAgentDto, DataError.Remote> {
        return safeCall<NewAgentDto> {
            httpClient.post("$BASE_URL/agency-admin/agent"){
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "password" to password,
                        "phone" to phone
                    )
                )
            }
        }
    }

    override suspend fun deleteAssistant(userId: Int): Result<DeleteResponseDto, DataError.Remote> {
        return safeCall<DeleteResponseDto> {
            httpClient.delete("$BASE_URL/agency-admin/assistant/$userId")
        }
    }

    override suspend fun deleteAgent(userId: Int): Result<DeleteResponseDto, DataError.Remote> {
        return safeCall<DeleteResponseDto> {
            httpClient.delete("$BASE_URL/agency-admin/agent/$userId")
        }
    }
}
