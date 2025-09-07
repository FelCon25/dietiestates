package it.unina.dietiestates.core.presentation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation._compontents.BottomNavigationBar
import it.unina.dietiestates.core.presentation._compontents.TopBar
import it.unina.dietiestates.features.admin.presentation.ManageAssistantsScreen
import it.unina.dietiestates.features.auth.presentation.authGraph
import it.unina.dietiestates.features.profile.presentation.ProfileScreen
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreen
import it.unina.dietiestates.features.property.presentation.home.HomeScreen
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreen

@Composable
fun MainNavGraph(navController: NavHostController, viewModel: MainScreenViewModel){

    val state by viewModel.state.collectAsState()

    state.startDestination?.let {
        NavHost(
            navController = navController,
            startDestination = it
        ){
            authGraph(
                navController = navController,
                onAuthSucceeded = { user ->
                    viewModel.addUserFromAuth(user)

                    navController.navigate(viewModel.getStartDestinationFromRole(user.role)){
                        popUpTo<Route.AuthGraph> {
                            inclusive = true
                        }
                    }
                }
            )

            userScreens(
                topBar = {
                    state.user?.let { user ->
                        TopBar(
                            user = user,
                            onEditProfileNavigation = {
                                navController.navigate(Route.Profile)
                            }
                        )
                    }
                },
                bottomBar = { route, onScroll ->
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = route,
                        scrollTop = onScroll
                    )
                }
            )

            adminScreens(
                topBar = {
                    state.user?.let { user ->
                        TopBar(
                            user = user,
                            onEditProfileNavigation = {
                                navController.navigate(Route.Profile)
                            }
                        )
                    }
                },
                bottomBar = { _, _ ->

                }
            )

            composable<Route.Profile> {
                state.user?.let { user ->
                    ProfileScreen(
                        onBackNavigation = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

private fun NavGraphBuilder.userScreens(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit
){

    navigation<Route.UserGraph>(
        startDestination = Route.Home
    ){
        composable<Route.Home> {
            HomeScreen(
                topBar = topBar,
                bottomBar = bottomBar
            )
        }

        composable<Route.SavedSearches> {
            SavedSearchesScreen(
                topBar = topBar,
                bottomBar = bottomBar
            )
        }

        composable<Route.Bookmarks> {
            BookmarksScreen(bottomBar = bottomBar)
        }
    }
}

private fun NavGraphBuilder.adminScreens(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit
){

    navigation<Route.AdminGraph>(
        startDestination = Route.ManageAssistants
    ){

        composable<Route.ManageAssistants> {
            ManageAssistantsScreen(topBar = topBar, bottomBar = bottomBar)
        }

        composable<Route.ManageAgents> {

        }

    }

}