package it.unina.dietiestates.features.agency.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteResponseDto(
    val message: String,
    val userId: Int
)

