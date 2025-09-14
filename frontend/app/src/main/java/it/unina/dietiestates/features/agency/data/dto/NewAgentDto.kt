package it.unina.dietiestates.features.agency.data.dto

import it.unina.dietiestates.core.data.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class NewAgentDto(
    val user: UserDto
)