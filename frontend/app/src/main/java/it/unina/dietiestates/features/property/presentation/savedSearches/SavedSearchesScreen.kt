package it.unina.dietiestates.features.property.presentation.savedSearches

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.property.domain.SavedSearch
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchesScreen(
    viewModel: SavedSearchesScreenViewModel = koinViewModel(),
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit,
    onSearchNavigate: (SavedSearch) -> Unit,
    refreshTrigger: Boolean = false,
    onRefreshConsumed: () -> Unit = {}
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // State for delete confirmation dialog
    var searchToDelete by remember { mutableStateOf<SavedSearch?>(null) }
    
    // Refresh when trigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.onEvent(SavedSearchesScreenEvent.OnRefresh)
            onRefreshConsumed()
        }
    }

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event) {
            is SavedSearchesScreenEvent.OnError -> {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = event.message)
                }
                coroutineScope.launch {
                    refreshState.animateToHidden()
                }
            }
            
            is SavedSearchesScreenEvent.OnSearchDeleted -> {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = "Saved search deleted successfully")
                }
            }

            else -> {}
        }
    }
    
    // Delete confirmation dialog
    searchToDelete?.let { search ->
        AlertDialog(
            onDismissRequest = { searchToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete saved search?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${search.name}\"? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(SavedSearchesScreenEvent.OnDeleteSearch(search.searchId))
                        searchToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { searchToDelete = null }
                ) {
                    Text(text = "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = topBar,
        bottomBar = {
            bottomBar(Route.SavedSearches) {

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
                viewModel.onEvent(SavedSearchesScreenEvent.OnRefresh)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saved Searches",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.savedSearches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved searches",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.savedSearches,
                            key = { it.searchId }
                        ) { savedSearch ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(
                                    animationSpec = tween(durationMillis = 300)
                                ) + scaleIn(
                                    initialScale = 0.9f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                                exit = fadeOut(
                                    animationSpec = tween(durationMillis = 200)
                                ) + scaleOut(
                                    targetScale = 0.9f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                            ) {
                                SavedSearchItem(
                                    savedSearch = savedSearch,
                                    onSearchClick = { onSearchNavigate(savedSearch) },
                                    onDeleteClick = {
                                        searchToDelete = savedSearch
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedSearchItem(
    savedSearch: SavedSearch,
    onSearchClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = savedSearch.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete search",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location info
            savedSearch.filters.let { filters ->
                if (filters.city != null || filters.address != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buildLocationText(filters),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Filter chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Price chip
                    if (filters.minPrice != null || filters.maxPrice != null) {
                        FilterChip(
                            text = buildPriceText(filters),
                            icon = "€"
                        )
                    }

                    // Property type
                    filters.type?.let { type ->
                        FilterChip(
                            text = type.lowercase().replaceFirstChar { it.uppercase() },
                            icon = null,
                            iconVector = Icons.Outlined.Home
                        )
                    }

                    // Rooms
                    if (filters.minRooms != null || filters.maxRooms != null) {
                        FilterChip(
                            text = buildRoomsText(filters),
                            icon = null,
                            iconVector = Icons.Outlined.Home
                        )
                    }

                    // Surface
                    if (filters.minSurfaceArea != null || filters.maxSurfaceArea != null) {
                        FilterChip(
                            text = buildSurfaceText(filters),
                            icon = "m²"
                        )
                    }

                    // Amenities
                    if (filters.elevator == true) {
                        FilterChip(
                            text = "Elevator",
                            icon = null,
                            iconVector = Icons.Outlined.Elevator
                        )
                    }
                    if (filters.airConditioning == true) {
                        FilterChip(
                            text = "AC",
                            icon = null,
                            iconVector = Icons.Outlined.AcUnit
                        )
                    }
                    if (filters.furnished == true) {
                        FilterChip(
                            text = "Furnished",
                            icon = "🛋"
                        )
                    }

                    // Condition
                    filters.propertyCondition?.let { condition ->
                        FilterChip(
                            text = condition.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            icon = "✨"
                        )
                    }

                    // Energy class
                    filters.energyClass?.let { energyClass ->
                        FilterChip(
                            text = "Class $energyClass",
                            icon = "⚡"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    icon: String? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            iconVector != null -> {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
            icon != null -> {
                Text(
                    text = icon,
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun buildLocationText(filters: it.unina.dietiestates.features.property.domain.SearchFilters): String {
    val parts = mutableListOf<String>()
    filters.address?.let { parts.add(it) }
    filters.city?.let { parts.add(it) }
    filters.province?.let { parts.add(it) }
    return parts.joinToString(", ")
}

@Composable
private fun buildPriceText(filters: it.unina.dietiestates.features.property.domain.SearchFilters): String {
    return when {
        filters.minPrice != null && filters.maxPrice != null ->
            "${filters.minPrice.toInt()} - ${filters.maxPrice.toInt()}"
        filters.minPrice != null -> "From ${filters.minPrice.toInt()}"
        filters.maxPrice != null -> "Up to ${filters.maxPrice.toInt()}"
        else -> ""
    }
}

@Composable
private fun buildRoomsText(filters: it.unina.dietiestates.features.property.domain.SearchFilters): String {
    return when {
        filters.minRooms != null && filters.maxRooms != null ->
            "${filters.minRooms}-${filters.maxRooms} rooms"
        filters.minRooms != null -> "${filters.minRooms}+ rooms"
        filters.maxRooms != null -> "Up to ${filters.maxRooms} rooms"
        else -> ""
    }
}

@Composable
private fun buildSurfaceText(filters: it.unina.dietiestates.features.property.domain.SearchFilters): String {
    return when {
        filters.minSurfaceArea != null && filters.maxSurfaceArea != null ->
            "${filters.minSurfaceArea.toInt()}-${filters.maxSurfaceArea.toInt()}"
        filters.minSurfaceArea != null -> "From ${filters.minSurfaceArea.toInt()}"
        filters.maxSurfaceArea != null -> "Up to ${filters.maxSurfaceArea.toInt()}"
        else -> ""
    }
}