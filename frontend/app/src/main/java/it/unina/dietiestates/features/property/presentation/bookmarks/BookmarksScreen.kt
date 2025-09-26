package it.unina.dietiestates.features.property.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.property.presentation._compontents.PropertyItem
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksScreenViewModel = koinViewModel(),
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event){
            is BookmarksScreenEvent.OnError -> {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = event.message)
                }
                coroutineScope.launch {
                    refreshState.animateToHidden()
                }
            }

            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = topBar,
        bottomBar = {
            bottomBar(Route.Bookmarks){

            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        PullToRefreshBox(
           modifier = Modifier
               .padding(paddingValues)
               .fillMaxSize(),
            isRefreshing = state.isRefreshing,
            state = refreshState,
            onRefresh = {
                viewModel.onEvent(BookmarksScreenEvent.OnRefresh)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            ) {

                Text(
                    text = "Saved properties",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.properties){ property ->
                        PropertyItem(
                            property = property
                        )
                    }
                }


            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if(state.isLoading) {
                    CircularProgressIndicator()
                }
                else if(state.properties.isEmpty()) {
                    Text(
                        text = "No saved properties"
                    )
                }
            }
        }
    }
}