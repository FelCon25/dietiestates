package it.unina.dietiestates.core.domain



data class User(
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val profilePic: String? = null,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)
