package it.unina.dietiestates.app

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object AuthGraph: Route

    @Serializable
    data object UserGraph: Route

    @Serializable
    data object Profile: Route

    @Serializable
    data object Home: Route

    @Serializable
    data object SavedSearches: Route

    @Serializable
    data object Bookmarks: Route

    @Serializable
    data object SignIn: Route

    @Serializable
    data object Register: Route
}