package it.unina.dietiestates.app

import kotlinx.serialization.Serializable

sealed interface Route {

    //Auth
    @Serializable
    data object AuthGraph: Route
    @Serializable
    data class SignIn(val passwordReset: Boolean = false): Route
    @Serializable
    data object Register: Route
    @Serializable
    data object ForgotPassword: Route
    @Serializable
    data object ResetPassword: Route


    //User
    @Serializable
    data object UserGraph: Route
    @Serializable
    data object Home: Route
    @Serializable
    data object SavedSearches: Route
    @Serializable
    data object Bookmarks: Route
    @Serializable
    data object Search: Route
    @Serializable
    data object SearchFilters: Route
    @Serializable
    data class DrawSearch(val centerOnCurrentLocation: Boolean = false): Route
    @Serializable
    data class PropertyDetails(val id: Int): Route


    //Admin
    @Serializable
    data object AdminGraph: Route
    @Serializable
    data object Admin: Route
    @Serializable
    data object AdminAddAssistant: Route
    @Serializable
    data object AdminAddAgent: Route


    //Assistant
    @Serializable
    data object AssistantGraph: Route
    @Serializable
    data object Assistant: Route
    @Serializable
    data object AssistantAddAgent: Route


    //Agent
    @Serializable
    data object AgentGraph: Route
    @Serializable
    data object Agent: Route
    @Serializable
    data object AddProperty: Route


    @Serializable
    data object Profile: Route
    @Serializable
    data object ChangePassword: Route
}