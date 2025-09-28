package it.unina.dietiestates.features.property.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class IsPropertySavedResponse(
    val isSaved: Boolean
)