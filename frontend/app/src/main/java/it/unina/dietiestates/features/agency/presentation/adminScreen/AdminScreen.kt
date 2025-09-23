package it.unina.dietiestates.features.agency.presentation.adminScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.features.agency.domain.Agent
import it.unina.dietiestates.features.agency.domain.Assistant
import it.unina.dietiestates.features.agency.presentation._components.AgencyItem
import it.unina.dietiestates.features.agency.presentation._components.AgentItem
import it.unina.dietiestates.features.agency.presentation._components.AssistantItem
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminScreenViewModel,
    topBar: @Composable () -> Unit,
    onAddNewAssistantNavigation: () -> Unit,
    onAddNewAgentNavigation: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    var isModalOpen by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
    )

    val tabs = listOf("Assistants", "Agents")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = topBar,
        floatingActionButton = {
            FloatingActionButton(
                containerColor = Green80,
                contentColor = Color.White,
                onClick = {
                    isModalOpen = !isModalOpen
                }
            ) {
                Icon(imageVector = if(isModalOpen) Icons.Outlined.Close else Icons.Outlined.Add, contentDescription = "Add or Close icon")
            }
        }
    ) { paddingValues ->

        if(state.isLoading){
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }
        else{
            Scaffold(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    CenterAlignedTopAppBar(
                        expandedHeight = 200.dp,
                        title = {
                            state.agency?.let { agency ->
                                AgencyItem(agency = agency)
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .fillMaxHeight(1f)
                ) {
                    TabRow(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.White
                    ){
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title
                                    )
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .weight(1f)
                    ) { index ->

                        when(index){
                            0 -> AssistantsPage(state.assistants)
                            1 -> AgentsPage(state.agents)
                        }
                    }
                }
            }
        }
    }

    if(isModalOpen){
        ModalBottomSheet(
            onDismissRequest = {
                isModalOpen = false
            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isModalOpen = false
                    onAddNewAssistantNavigation()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Add Assistant"
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isModalOpen = false
                    onAddNewAgentNavigation()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Add Agent"
                )
            }
        }
    }
}

@Composable
private fun AssistantsPage(
    assistants: List<Assistant>
){
    if(assistants.isEmpty()){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text("No assistants have been added.")
        }
    }
    else{
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(assistants){ assistant ->
                AssistantItem(assistant)
            }
        }
    }
}

@Composable
private fun AgentsPage(
    agents: List<Agent>
){
    if(agents.isEmpty()){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text("No agents have been added.")
        }
    }
    else{
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(agents){ agent ->
                AgentItem(agent)
            }
        }
    }
}