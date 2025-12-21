package it.unina.dietiestates.core.presentation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import it.unina.dietiestates.features.profile.presentation.changePassword.ChangePasswordScreen
import it.unina.dietiestates.features.property.presentation.SearchFiltersScreen
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreen
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreen
import it.unina.dietiestates.features.property.presentation.drawSearch.DrawSearchScreen
import it.unina.dietiestates.features.property.presentation.home.HomeScreen
import it.unina.dietiestates.features.property.presentation.propertyDetails.PropertyDetailsScreen
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreen
import it.unina.dietiestates.features.property.presentation.search.SearchScreen
import it.unina.dietiestates.features.property.presentation.search.SearchScreenViewModel
import it.unina.dietiestates.features.property.domain.NearbyFilters
import it.unina.dietiestates.features.property.domain.SearchFilters
import it.unina.dietiestates.features.property.domain.toNearbyFilters
import it.unina.dietiestates.features.property.domain.toSearchFilters
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavGraph(navController: NavHostController, viewModel: MainScreenViewModel){

    val state by viewModel.state.collectAsStateWithLifecycle()

    state.startDestination?.let {
        NavHost(
            navController = navController,
            startDestination = it
        ){
            authGraph(
                navController = navController,
                onAuthSucceeded = { user ->
                    viewModel.onEvent(MainScreenEvent.OnSignIn(user = user))

                    navController.navigate(viewModel.getStartDestinationFromRole(user.role)){
                        popUpTo<Route.AuthGraph> {
                            inclusive = true
                        }
                    }
                }
            )

            userScreens(
                navController = navController,
                startDestination = state.propertyIdFromNotification?.let { Route.PropertyDetails(it) } ?: Route.Home,
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
                        },
                        onNavigateToChangePassword = {
                            navController.navigate(Route.ChangePassword)
                        }
                    )
                }
            }

            composable<Route.ChangePassword> {
                ChangePasswordScreen(
                    onBackNavigation = {
                        navController.navigateUp()
                    },
                    onPasswordChanged = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.userScreens(
    navController: NavHostController,
    startDestination: Route,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit
){

    navigation<Route.UserGraph>(
        startDestination = startDestination
    ){
        composable<Route.Home> {
            HomeScreen(
                topBar = topBar,
                bottomBar = bottomBar,
                onDrawSearchNavigation = {
                    navController.navigate(Route.DrawSearch)
                },
                onSearchNearYouNavigation = {
                    navController.navigate(Route.Search)
                },
                onPropertyDetailsNavigate = { propertyId ->
                    navController.navigate(Route.PropertyDetails(propertyId))
                }
            )
        }

        composable<Route.SavedSearches> {
            val savedSearchesEntry = remember(navController.currentBackStackEntry) {
                navController.currentBackStackEntry
            }
            
            var refreshTrigger by remember { mutableStateOf(false) }

            SavedSearchesScreen(
                topBar = topBar,
                bottomBar = bottomBar,
                onSearchNavigate = { savedSearch ->
                    // Pass the saved search filters via current entry's savedStateHandle
                    val handle = savedSearchesEntry?.savedStateHandle
                    
                    handle?.set("saved_search_filters_address", savedSearch.filters.address)
                    handle?.set("saved_search_filters_city", savedSearch.filters.city)
                    handle?.set("saved_search_filters_province", savedSearch.filters.province)
                    handle?.set("saved_search_filters_postalCode", savedSearch.filters.postalCode)
                    handle?.set("saved_search_filters_minPrice", savedSearch.filters.minPrice)
                    handle?.set("saved_search_filters_maxPrice", savedSearch.filters.maxPrice)
                    handle?.set("saved_search_filters_minSurface", savedSearch.filters.minSurfaceArea)
                    handle?.set("saved_search_filters_maxSurface", savedSearch.filters.maxSurfaceArea)
                    handle?.set("saved_search_filters_minRooms", savedSearch.filters.minRooms)
                    handle?.set("saved_search_filters_maxRooms", savedSearch.filters.maxRooms)
                    handle?.set("saved_search_filters_type", savedSearch.filters.type)
                    handle?.set("saved_search_filters_condition", savedSearch.filters.propertyCondition)
                    handle?.set("saved_search_filters_elevator", savedSearch.filters.elevator)
                    handle?.set("saved_search_filters_airConditioning", savedSearch.filters.airConditioning)
                    handle?.set("saved_search_filters_concierge", savedSearch.filters.concierge)
                    handle?.set("saved_search_filters_furnished", savedSearch.filters.furnished)
                    handle?.set("saved_search_filters_energyClass", savedSearch.filters.energyClass)
                    handle?.set("apply_saved_search", true)
                    
                    navController.navigate(Route.Search)
                },
                refreshTrigger = refreshTrigger,
                onRefreshConsumed = { refreshTrigger = false }
            )
            
            // Listen for search saved event to refresh the list
            LaunchedEffect(savedSearchesEntry) {
                savedSearchesEntry?.savedStateHandle?.getStateFlow<Boolean>("search_was_saved", false)?.collect { wasSaved ->
                    if (wasSaved) {
                        refreshTrigger = true
                        savedSearchesEntry.savedStateHandle.remove<Boolean>("search_was_saved")
                    }
                }
            }
        }

        composable<Route.Bookmarks> {
            BookmarksScreen(
                topBar = topBar,
                bottomBar = bottomBar,
                onPropertyDetailsNavigate = { propertyId ->
                    navController.navigate(Route.PropertyDetails(propertyId))
                }
            )
        }

        composable<Route.Search> {
            var appliedFilters by remember { mutableStateOf<SearchFilters?>(null) }
            var savedSearchFilters by remember { mutableStateOf<SearchFilters?>(null) }
            var currentFilters by remember { mutableStateOf(SearchFilters()) }
            
            val viewModel: SearchScreenViewModel = koinViewModel()
            
            // Set up callback to notify SavedSearches screen when a search is saved
            viewModel.onSearchSaved = {
                try {
                    val savedSearchesEntry = navController.getBackStackEntry(Route.SavedSearches)
                    savedSearchesEntry.savedStateHandle["search_was_saved"] = true
                } catch (e: Exception) {
                    // SavedSearches not in back stack, ignore
                }
            }
            
            // Check if we came from SavedSearches with filters
            val previousEntry = remember(navController.previousBackStackEntry) {
                navController.previousBackStackEntry
            }
            
            LaunchedEffect(previousEntry) {
                val prevHandle = previousEntry?.savedStateHandle
                val shouldApply = prevHandle?.get<Boolean>("apply_saved_search") ?: false
                
                if (shouldApply) {
                    val filters = SearchFilters(
                        address = prevHandle?.get<String>("saved_search_filters_address"),
                        city = prevHandle?.get<String>("saved_search_filters_city"),
                        province = prevHandle?.get<String>("saved_search_filters_province"),
                        postalCode = prevHandle?.get<String>("saved_search_filters_postalCode"),
                        minPrice = prevHandle?.get<Double>("saved_search_filters_minPrice"),
                        maxPrice = prevHandle?.get<Double>("saved_search_filters_maxPrice"),
                        minSurfaceArea = prevHandle?.get<Double>("saved_search_filters_minSurface"),
                        maxSurfaceArea = prevHandle?.get<Double>("saved_search_filters_maxSurface"),
                        minRooms = prevHandle?.get<Int>("saved_search_filters_minRooms"),
                        maxRooms = prevHandle?.get<Int>("saved_search_filters_maxRooms"),
                        type = prevHandle?.get<String>("saved_search_filters_type"),
                        propertyCondition = prevHandle?.get<String>("saved_search_filters_condition"),
                        elevator = prevHandle?.get<Boolean>("saved_search_filters_elevator"),
                        airConditioning = prevHandle?.get<Boolean>("saved_search_filters_airConditioning"),
                        concierge = prevHandle?.get<Boolean>("saved_search_filters_concierge"),
                        furnished = prevHandle?.get<Boolean>("saved_search_filters_furnished"),
                        energyClass = prevHandle?.get<String>("saved_search_filters_energyClass")
                    )
                    
                    currentFilters = filters
                    savedSearchFilters = filters
                    prevHandle?.remove<Boolean>("apply_saved_search")
                }
            }

            SearchScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onPropertyClick = { propertyId ->
                    navController.navigate(Route.PropertyDetails(propertyId))
                },
                onFiltersClick = { filters ->
                    currentFilters = filters
                    val nearbyFilters = filters.toNearbyFilters()
                    val handle = navController.currentBackStackEntry?.savedStateHandle
                    handle?.set("current_search_filters_insertionType", nearbyFilters.insertionType)
                    handle?.set("current_search_filters_minPrice", nearbyFilters.minPrice)
                    handle?.set("current_search_filters_maxPrice", nearbyFilters.maxPrice)
                    handle?.set("current_search_filters_minSurface", nearbyFilters.minSurfaceArea)
                    handle?.set("current_search_filters_maxSurface", nearbyFilters.maxSurfaceArea)
                    handle?.set("current_search_filters_minRooms", nearbyFilters.minRooms)
                    handle?.set("current_search_filters_maxRooms", nearbyFilters.maxRooms)
                    handle?.set("current_search_filters_type", nearbyFilters.type)
                    handle?.set("current_search_filters_condition", nearbyFilters.propertyCondition)
                    handle?.set("current_search_filters_concierge", nearbyFilters.concierge)
                    handle?.set("current_search_filters_airConditioning", nearbyFilters.airConditioning)
                    handle?.set("current_search_filters_elevator", nearbyFilters.elevator)
                    handle?.set("current_search_filters_furnished", nearbyFilters.furnished)
                    handle?.set("current_search_filters_energyClass", nearbyFilters.energyClass)
                    handle?.set("is_from_search_screen", true)
                    navController.navigate(Route.SearchFilters)
                },
                appliedFilters = appliedFilters,
                onFiltersConsumed = {
                    appliedFilters = null
                },
                savedSearchFilters = savedSearchFilters,
                onSavedSearchConsumed = {
                    savedSearchFilters = null
                },
                viewModel = viewModel
            )

            val handle = navController.currentBackStackEntry?.savedStateHandle
            LaunchedEffect(handle) {
                // Listen for filter changes from SearchFilters screen
                handle?.getStateFlow<Boolean>("search_filters_applied", false)?.collect { applied ->
                    if (applied) {
                        val insertionType = handle.get<String>("filter_insertionType")
                        val minPrice = handle.get<Int>("filter_minPrice")
                        val maxPrice = handle.get<Int>("filter_maxPrice")
                        val minSurface = handle.get<Int>("filter_minSurface")
                        val maxSurface = handle.get<Int>("filter_maxSurface")
                        val minRooms = handle.get<Int>("filter_minRooms")
                        val maxRooms = handle.get<Int>("filter_maxRooms")
                        val type = handle.get<String>("filter_type")
                        val condition = handle.get<String>("filter_condition")
                        val concierge = handle.get<Boolean>("filter_concierge")
                        val airConditioning = handle.get<Boolean>("filter_airConditioning")
                        val elevator = handle.get<Boolean>("filter_elevator")
                        val furnished = handle.get<Boolean>("filter_furnished")
                        val energyClass = handle.get<String>("filter_energyClass")

                        val nearbyFilters = NearbyFilters(
                            insertionType = insertionType,
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            minSurfaceArea = minSurface,
                            maxSurfaceArea = maxSurface,
                            minRooms = minRooms,
                            maxRooms = maxRooms,
                            type = type,
                            propertyCondition = condition,
                            concierge = concierge,
                            airConditioning = airConditioning,
                            elevator = elevator,
                            furnished = furnished,
                            energyClass = energyClass
                        )

                        val newSearchFilters = nearbyFilters.toSearchFilters(
                            address = currentFilters.address,
                            city = currentFilters.city,
                            province = currentFilters.province,
                            postalCode = currentFilters.postalCode,
                            locationSearch = currentFilters.locationSearch
                        )

                        currentFilters = newSearchFilters
                        appliedFilters = newSearchFilters
                        handle.remove<Boolean>("search_filters_applied")
                    }
                }
            }
        }

        composable<Route.DrawSearch> {
            var appliedFilters by remember { mutableStateOf<NearbyFilters?>(null) }
            var currentFilters by remember { mutableStateOf(NearbyFilters(insertionType = "SALE")) }
            var currentRadius by remember { mutableStateOf(10000f) }

            val handle = navController.currentBackStackEntry?.savedStateHandle
            val savedRadius = handle?.get<Float>("saved_radius")
            if (savedRadius != null && savedRadius != currentRadius) {
                currentRadius = savedRadius
                handle.remove<Float>("saved_radius")
            }

            DrawSearchScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onConfirmNavigation = { filters, radius ->
                    currentFilters = filters
                    currentRadius = radius
                    val handle = navController.currentBackStackEntry?.savedStateHandle
                    handle?.set("current_filters_insertion", filters.insertionType)
                    handle?.set("current_filters_minPrice", filters.minPrice)
                    handle?.set("current_filters_maxPrice", filters.maxPrice)
                    handle?.set("current_filters_minSurface", filters.minSurfaceArea)
                    handle?.set("current_filters_maxSurface", filters.maxSurfaceArea)
                    handle?.set("current_filters_minRooms", filters.minRooms)
                    handle?.set("current_filters_concierge", filters.concierge)
                    handle?.set("current_filters_airConditioning", filters.airConditioning)
                    handle?.set("current_filters_elevator", filters.elevator)
                    handle?.set("current_filters_energyClass", filters.energyClass)
                    handle?.set("saved_radius", radius)
                    navController.navigate(Route.SearchFilters)
                },
                appliedFilters = appliedFilters,
                onFiltersConsumed = {
                    appliedFilters = null
                },
                initialRadius = currentRadius,
                onPropertyDetailsNavigate = { propertyId ->
                    navController.navigate(Route.PropertyDetails(propertyId))
                }
            )

            LaunchedEffect(handle) {
                handle?.getStateFlow<Boolean>("filters_applied", false)?.collect { applied ->
                    if (applied) {
                        val insertionType = handle.get<String>("filter_insertionType")
                        val minPrice = handle.get<Int>("filter_minPrice")
                        val maxPrice = handle.get<Int>("filter_maxPrice")
                        val minSurface = handle.get<Int>("filter_minSurface")
                        val maxSurface = handle.get<Int>("filter_maxSurface")
                        val minRooms = handle.get<Int>("filter_minRooms")
                        val concierge = handle.get<Boolean>("filter_concierge")
                        val airConditioning = handle.get<Boolean>("filter_airConditioning")
                        val elevator = handle.get<Boolean>("filter_elevator")
                        val energyClass = handle.get<String>("filter_energyClass")

                        val newFilters = NearbyFilters(
                            insertionType = insertionType,
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            minSurfaceArea = minSurface,
                            maxSurfaceArea = maxSurface,
                            minRooms = minRooms,
                            concierge = concierge,
                            airConditioning = airConditioning,
                            elevator = elevator,
                            energyClass = energyClass
                        )
                        currentFilters = newFilters
                        appliedFilters = newFilters
                        handle.remove<Boolean>("filters_applied")
                    }
                }
            }
        }

        composable<Route.SearchFilters> {
            val previousEntry = remember(navController.previousBackStackEntry) {
                navController.previousBackStackEntry
            }

            val isFromSearchScreen = previousEntry?.savedStateHandle?.get<Boolean>("is_from_search_screen") ?: false

            val initialFilters = if (isFromSearchScreen) {
                NearbyFilters(
                    insertionType = previousEntry?.savedStateHandle?.get<String>("current_search_filters_insertionType"),
                    minPrice = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_minPrice"),
                    maxPrice = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_maxPrice"),
                    minSurfaceArea = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_minSurface"),
                    maxSurfaceArea = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_maxSurface"),
                    minRooms = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_minRooms"),
                    maxRooms = previousEntry?.savedStateHandle?.get<Int>("current_search_filters_maxRooms"),
                    type = previousEntry?.savedStateHandle?.get<String>("current_search_filters_type"),
                    propertyCondition = previousEntry?.savedStateHandle?.get<String>("current_search_filters_condition"),
                    concierge = previousEntry?.savedStateHandle?.get<Boolean>("current_search_filters_concierge"),
                    airConditioning = previousEntry?.savedStateHandle?.get<Boolean>("current_search_filters_airConditioning"),
                    elevator = previousEntry?.savedStateHandle?.get<Boolean>("current_search_filters_elevator"),
                    furnished = previousEntry?.savedStateHandle?.get<Boolean>("current_search_filters_furnished"),
                    energyClass = previousEntry?.savedStateHandle?.get<String>("current_search_filters_energyClass")
                )
            } else {
                NearbyFilters(
                    insertionType = previousEntry?.savedStateHandle?.get<String>("current_filters_insertion"),
                    minPrice = previousEntry?.savedStateHandle?.get<Int>("current_filters_minPrice"),
                    maxPrice = previousEntry?.savedStateHandle?.get<Int>("current_filters_maxPrice"),
                    minSurfaceArea = previousEntry?.savedStateHandle?.get<Int>("current_filters_minSurface"),
                    maxSurfaceArea = previousEntry?.savedStateHandle?.get<Int>("current_filters_maxSurface"),
                    minRooms = previousEntry?.savedStateHandle?.get<Int>("current_filters_minRooms"),
                    concierge = previousEntry?.savedStateHandle?.get<Boolean>("current_filters_concierge"),
                    airConditioning = previousEntry?.savedStateHandle?.get<Boolean>("current_filters_airConditioning"),
                    elevator = previousEntry?.savedStateHandle?.get<Boolean>("current_filters_elevator"),
                    energyClass = previousEntry?.savedStateHandle?.get<String>("current_filters_energyClass")
                )
            }

            SearchFiltersScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                initialFilters = initialFilters,
                showInsertionType = true,
                onApply = { filters ->
                    val handle = navController.previousBackStackEntry?.savedStateHandle
                    if (isFromSearchScreen) {
                        handle?.set("filter_insertionType", filters.insertionType)
                        handle?.set("filter_minPrice", filters.minPrice)
                        handle?.set("filter_maxPrice", filters.maxPrice)
                        handle?.set("filter_minSurface", filters.minSurfaceArea)
                        handle?.set("filter_maxSurface", filters.maxSurfaceArea)
                        handle?.set("filter_minRooms", filters.minRooms)
                        handle?.set("filter_maxRooms", filters.maxRooms)
                        handle?.set("filter_type", filters.type)
                        handle?.set("filter_condition", filters.propertyCondition)
                        handle?.set("filter_concierge", filters.concierge)
                        handle?.set("filter_airConditioning", filters.airConditioning)
                        handle?.set("filter_elevator", filters.elevator)
                        handle?.set("filter_furnished", filters.furnished)
                        handle?.set("filter_energyClass", filters.energyClass)
                        handle?.set("search_filters_applied", true)
                    } else {
                        handle?.set("filter_insertionType", filters.insertionType)
                        handle?.set("filter_minPrice", filters.minPrice)
                        handle?.set("filter_maxPrice", filters.maxPrice)
                        handle?.set("filter_minSurface", filters.minSurfaceArea)
                        handle?.set("filter_maxSurface", filters.maxSurfaceArea)
                        handle?.set("filter_minRooms", filters.minRooms)
                        handle?.set("filter_concierge", filters.concierge)
                        handle?.set("filter_airConditioning", filters.airConditioning)
                        handle?.set("filter_elevator", filters.elevator)
                        handle?.set("filter_energyClass", filters.energyClass)
                        handle?.set("filters_applied", true)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable<Route.PropertyDetails> {
            PropertyDetailsScreen(
                onBackNavigation = {
                    if(navController.previousBackStackEntry == null){
                        navController.navigate(Route.Home){
                            popUpTo<Route.PropertyDetails>{
                                inclusive = true
                            }
                        }
                    }
                    else{
                        navController.navigateUp()
                    }
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
                },
                onPropertyDetailsNavigate = { propertyId ->
                    navController.navigate(Route.PropertyDetails(propertyId))
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