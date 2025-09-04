package it.unina.dietiestates.features.property.presentation.bookmarks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.unina.dietiestates.app.Route
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookmarksScreen(
    viewModel: BookmarksScreenViewModel = koinViewModel(),
    bottomBar: @Composable (Route, () -> Unit) -> Unit,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            bottomBar(Route.Bookmarks){

            }
        },
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Bookmarks Screen")
        }
    }

}