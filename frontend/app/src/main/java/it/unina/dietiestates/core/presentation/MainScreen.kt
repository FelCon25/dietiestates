package it.unina.dietiestates.core.presentation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val view = LocalView.current

    LaunchedEffect(currentDestination) {
        currentDestination?.let { navDestination ->
            val isInAuthGraph = navDestination.hierarchy.any { it.hasRoute(Route.AuthGraph::class) }

            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isInAuthGraph
        }
    }

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event){
            is MainScreenEvent.OnLogout -> {
                navController.navigate(Route.AuthGraph){
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MainNavGraph(navController = navController, viewModel)
    }
}