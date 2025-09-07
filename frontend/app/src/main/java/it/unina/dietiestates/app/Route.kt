package it.unina.dietiestates.app

import kotlinx.serialization.Serializable

sealed interface Route {

    //Auth
    @Serializable
    data object AuthGraph: Route
    @Serializable
    data object SignIn: Route
    @Serializable
    data object Register: Route


    //User
    @Serializable
    data object UserGraph: Route
    @Serializable
    data object Home: Route
    @Serializable
    data object SavedSearches: Route
    @Serializable
    data object Bookmarks: Route



    //Admin
    @Serializable
    data object AdminGraph: Route
    @Serializable
    data object ManageAssistants: Route
    @Serializable
    data object ManageAgents: Route


    //Assistant
    @Serializable
    data object AssistantGraph: Route


    //Agent
    @Serializable
    data object AgentGraph: Route


    @Serializable
    data object Profile: Route
}