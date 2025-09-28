package it.unina.dietiestates.core.presentation

import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.domain.User

data class MainScreenState(
    val startDestination: Route? = null,
    val user: User? = null,
    val isReady: Boolean = false,
    val propertyIdFromNotification: Int? = null
)