package it.unina.dietiestates.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.flow.first

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel
) {

    val navController = rememberNavController()

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