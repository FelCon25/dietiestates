package it.unina.dietiestates.core.domain

data class Tokens(
    val access: String,
    val refresh: String? = null
)
