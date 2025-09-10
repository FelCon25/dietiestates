package it.unina.dietiestates.features.admin.data.dto

import it.unina.dietiestates.core.data.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class NewAssistantDto(
    val user: UserDto
)

