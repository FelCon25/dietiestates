package it.unina.dietiestates.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val profilePic: String? = null,
    val role: String,
    val createdAt: String,
    val updatedAt: String,
    val provider: String? = null
)

