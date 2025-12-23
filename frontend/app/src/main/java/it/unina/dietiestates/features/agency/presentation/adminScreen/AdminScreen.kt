package it.unina.dietiestates.features.agency.presentation.adminScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.runtime.LaunchedEffect
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
import it.unina.dietiestates.features.agency.domain.Agent
import it.unina.dietiestates.features.agency.domain.Assistant
import it.unina.dietiestates.features.agency.presentation._components.AgencyItem
import it.unina.dietiestates.features.agency.presentation._components.AgentItem
import it.unina.dietiestates.features.agency.presentation._components.AssistantItem
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.delay
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
    
    // Auto-dismiss messages after 3 seconds
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

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
            Box(modifier = Modifier.fillMaxSize()) {
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
                                    AgencyItem(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .heightIn(max = 200.dp),
                                        agency = agency
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { innerPaddingValues ->

                    Column(
                        modifier = Modifier
                            .padding(innerPaddingValues)
                            .fillMaxSize()
                            .fillMaxHeight(1f)
                    ) {
                        // Toast messages at top
                        AnimatedVisibility(
                            visible = state.successMessage != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Text(
                                        text = state.successMessage ?: "",
                                        color = Color(0xFF1B5E20),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        AnimatedVisibility(
                            visible = state.errorMessage != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(8.dp))
                                    Text(
                                        text = state.errorMessage ?: "",
                                        color = Color(0xFFC62828),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
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
                                0 -> AssistantsPage(
                                    assistants = state.assistants,
                                    onDeleteAssistant = viewModel::deleteAssistant
                                )
                                1 -> AgentsPage(
                                    agents = state.agents,
                                    onDeleteAgent = viewModel::deleteAgent
                                )
                            }
                        }
                    }
                }
                
                // Loading overlay when deleting
                if (state.isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
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
    assistants: List<Assistant>,
    onDeleteAssistant: (Int) -> Unit
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
                AssistantItem(
                    assistant = assistant,
                    onDelete = onDeleteAssistant
                )
            }
        }
    }
}

@Composable
private fun AgentsPage(
    agents: List<Agent>,
    onDeleteAgent: (Int) -> Unit
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
                AgentItem(
                    agent = agent,
                    onDelete = onDeleteAgent
                )
            }
        }
    }
}
