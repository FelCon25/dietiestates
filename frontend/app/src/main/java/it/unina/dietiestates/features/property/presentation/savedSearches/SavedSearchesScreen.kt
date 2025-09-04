package it.unina.dietiestates.features.property.presentation.savedSearches

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.unina.dietiestates.app.Route
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchesScreen(
    viewModel: SavedSearchesScreenViewModel = koinViewModel(),
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit,
) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = topBar,
        bottomBar = {
            bottomBar(Route.SavedSearches){

            }
        },
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Saved searches")
        }
    }

}