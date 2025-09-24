package it.unina.dietiestates.core.presentation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import it.unina.dietiestates.app.BottomBarScreen
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation._compontents.BottomNavigationBar
import it.unina.dietiestates.core.presentation._compontents.TopBar
import it.unina.dietiestates.features.agency.presentation.addAgent.AddAgentScreen
import it.unina.dietiestates.features.agency.presentation.addAssistant.AdminAddAssistantScreen
import it.unina.dietiestates.features.agency.presentation.adminScreen.AdminScreen
import it.unina.dietiestates.features.agency.presentation.adminScreen.AdminScreenViewModel
import it.unina.dietiestates.features.agency.presentation.agentScreen.AgentScreen
import it.unina.dietiestates.features.agency.presentation.agentScreen.AgentScreenViewModel
import it.unina.dietiestates.features.agency.presentation.assistantScreen.AssistantScreen
import it.unina.dietiestates.features.agency.presentation.assistantScreen.AssistantScreenViewModel
import it.unina.dietiestates.features.auth.presentation.authGraph
import it.unina.dietiestates.features.profile.presentation.ProfileScreen
import it.unina.dietiestates.features.property.presentation.SearchFiltersScreen
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreen
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreen
import it.unina.dietiestates.features.property.presentation.drawSearch.DrawSearchScreen
import it.unina.dietiestates.features.property.presentation.home.HomeScreen
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreen
import org.koin.compose.viewmodel.koinViewModel

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
                navController = navController,
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
                        screens = listOf(BottomBarScreen.Home, BottomBarScreen.SavedSearches, BottomBarScreen.Bookmarks),
                        navController = navController,
                        currentRoute = route,
                        scrollTop = onScroll
                    )
                }
            )

            adminScreens(
                navController = navController,
                topBar = {
                    state.user?.let { user ->
                        TopBar(
                            user = user,
                            onEditProfileNavigation = {
                                navController.navigate(Route.Profile)
                            }
                        )
                    }
                }
            )

            assistantScreens(
                navController = navController,
                topBar = {
                    state.user?.let { user ->
                        TopBar(
                            user = user,
                            onEditProfileNavigation = {
                                navController.navigate(Route.Profile)
                            }
                        )
                    }
                }
            )

            agentScreens(
                navController = navController,
                topBar = {
                    state.user?.let { user ->
                        TopBar(
                            user = user,
                            onEditProfileNavigation = {
                                navController.navigate(Route.Profile)
                            }
                        )
                    }
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
    navController: NavHostController,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit
){

    navigation<Route.UserGraph>(
        startDestination = Route.Home
    ){
        composable<Route.Home> {
            HomeScreen(
                topBar = topBar,
                bottomBar = bottomBar,
                onDrawSearchNavigation = {
                    navController.navigate(Route.DrawSearch)
                },
                onSearchNearYouNavigation = {
                    //todo
                }
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

        composable<Route.DrawSearch> {
            DrawSearchScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onConfirmNavigation = {
                    navController.navigate(Route.SearchFilters)
                }
            )
        }

        composable<Route.SearchFilters> {
            SearchFiltersScreen(
                onBackNavigation = {
                    navController.navigateUp()
                }
            )
        }
    }
}

private fun NavGraphBuilder.adminScreens(
    navController: NavController,
    topBar: @Composable () -> Unit
){

    navigation<Route.AdminGraph>(
        startDestination = Route.Admin
    ){

        composable<Route.Admin> {
            val viewModel = it.sharedKoinViewModel<AdminScreenViewModel>(navController = navController)

            AdminScreen(
                viewModel = viewModel,
                topBar = topBar,
                onAddNewAssistantNavigation = {
                    navController.navigate(Route.AdminAddAssistant)
                },
                onAddNewAgentNavigation = {
                    navController.navigate(Route.AdminAddAgent)
                }
            )
        }

        composable<Route.AdminAddAssistant> {
            val viewModel = it.sharedKoinViewModel<AdminScreenViewModel>(navController = navController)

            AdminAddAssistantScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onNewAssistantAdded = { assistant ->
                    viewModel.onNewAssistantAdded(assistant)
                    navController.navigateUp()
                }
            )
        }

        composable<Route.AdminAddAgent> {
            val viewModel = it.sharedKoinViewModel<AdminScreenViewModel>(navController = navController)

            AddAgentScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onNewAgentAdded = { agent ->
                    viewModel.onNewAgentAdded(agent)
                    navController.navigateUp()
                }
            )
        }
    }
}

private fun NavGraphBuilder.assistantScreens(
    navController: NavController,
    topBar: @Composable () -> Unit
){
    navigation<Route.AssistantGraph>(
        startDestination = Route.Assistant
    ){
        composable<Route.Assistant>{
            val viewModel = it.sharedKoinViewModel<AssistantScreenViewModel>(navController = navController)

            AssistantScreen(
                viewModel = viewModel,
                topBar = topBar,
                onAddNewAgentNavigation = {
                    navController.navigate(Route.AssistantAddAgent)
                }
            )
        }

        composable<Route.AssistantAddAgent> {
            val viewModel = it.sharedKoinViewModel<AssistantScreenViewModel>(navController = navController)

            AddAgentScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onNewAgentAdded = { agent ->
                    viewModel.onNewAgentAdded(agent)
                    navController.navigateUp()
                }
            )
        }

    }
}

private fun NavGraphBuilder.agentScreens(
    navController: NavController,
    topBar: @Composable () -> Unit
){
    navigation<Route.AgentGraph>(
        startDestination = Route.Agent
    ){
        composable<Route.Agent>{
            val viewModel = it.sharedKoinViewModel<AgentScreenViewModel>(navController = navController)

            AgentScreen(
                viewModel = viewModel,
                topBar = topBar,
                onNewPropertyNavigation = {
                    navController.navigate(Route.AddProperty)
                }
            )
        }

        composable<Route.AddProperty> {
            val viewModel = it.sharedKoinViewModel<AgentScreenViewModel>(navController = navController)

            AddPropertyScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onNewPropertyAdded = { property ->
                    viewModel.onNewPropertyAdded(property)
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}