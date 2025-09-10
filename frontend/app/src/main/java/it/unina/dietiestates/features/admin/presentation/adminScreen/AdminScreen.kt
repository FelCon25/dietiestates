package it.unina.dietiestates.features.admin.presentation.adminScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.features.admin.presentation._components.AgentItem
import it.unina.dietiestates.features.admin.presentation._components.AssistantItem
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminScreenViewModel,
    topBar: @Composable () -> Unit,
    onAddNewAssistant: () -> Unit,
    onAddNewAgent: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    var isDropdownMenuOpen by remember {  mutableStateOf(false) }

    val pagerState = rememberPagerState(0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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
                    isDropdownMenuOpen = !isDropdownMenuOpen
                }
            ) {

                DropdownMenu(
                    expanded = isDropdownMenuOpen,
                    onDismissRequest = {
                        isDropdownMenuOpen = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(text = "Add Assistant")
                        },
                        onClick = {
                            isDropdownMenuOpen = false
                            onAddNewAssistant()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(text = "Add Agent")
                        },
                        onClick = {
                            isDropdownMenuOpen = false
                            onAddNewAgent()
                        }
                    )
                }

                Icon(imageVector = if(isDropdownMenuOpen) Icons.Outlined.Close else Icons.Outlined.Add, contentDescription = "Add or Close icon")
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
                    state.agency?.let { agency ->
                        TopAppBar(
                            expandedHeight = 100.dp,
                            title = {
                                Column {
                                    Text(
                                        text = agency.businessName,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = agency.vatNumber,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        text = agency.email,
                                        fontSize = 16.sp
                                    )
                                }

                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            ) { innerPadding ->

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
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
                        modifier = Modifier.weight(1f)
                    ) { index ->

                        when(index){
                            0 -> {
                                if(state.assistants.isEmpty()){
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ){
                                        Text("No assistants have been added")
                                    }
                                }
                                else{
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        items(state.assistants.size){ i ->
                                            val assistant = state.assistants[i]

                                            AssistantItem(assistant)
                                        }
                                    }
                                }
                            }

                            1 -> {
                                if(state.agents.isEmpty()){
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ){
                                        Text("No agents have been added")
                                    }
                                }
                                else{
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        items(state.agents.size){ i ->
                                            val agent = state.agents[i]

                                            AgentItem(agent = agent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}