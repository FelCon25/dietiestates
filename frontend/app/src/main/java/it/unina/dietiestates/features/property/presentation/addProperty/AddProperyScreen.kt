package it.unina.dietiestates.features.property.presentation.addProperty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.presentation._compontents.ProgressStepIndicator
import it.unina.dietiestates.features.property.presentation.addProperty.pages.CharacteristicsPage
import it.unina.dietiestates.features.property.presentation.addProperty.pages.GeneralInfoPage
import it.unina.dietiestates.features.property.presentation.addProperty.pages.LocationPage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    viewModel: AddPropertyScreenViewModel = koinViewModel(),
    onBackNavigation: () -> Unit,
    onNewPropertyAdded: (Property) -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val steps = listOf("General info", "Characteristics", "Location")

    val pagerState = rememberPagerState(state.currentPage.ordinal, pageCount = { steps.size })

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event){
            is AddPropertyScreenEvent.OnNavigateToNextPage -> {
                scope.launch {
                    pagerState.animateScrollToPage(state.currentPage.ordinal)
                }
            }

            is AddPropertyScreenEvent.OnNavigateToPrevPage -> {
                scope.launch {
                    pagerState.animateScrollToPage(state.currentPage.ordinal)
                }
            }

            is AddPropertyScreenEvent.OnWrongValueInput -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is AddPropertyScreenEvent.OnPropertyAddedSuccessfully -> {
                onNewPropertyAdded(event.property)
            }

            is AddPropertyScreenEvent.OnPropertyAddingFailed -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Property creation failed, please try again.")
                }
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Property")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackNavigation
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Icon back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            ProgressStepIndicator(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 16.dp),
                steps = steps,
                currentStep = state.currentPage.ordinal
            )

            if(state.property != null){
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    if(state.isAddingPropertyLoading){
                        Column {
                            Text("Property creation in progress...")

                            Spacer(modifier = Modifier.height(16.dp))

                            CircularProgressIndicator()
                        }
                    }
                }
            }
            else{
                HorizontalPager(
                    modifier = Modifier
                        .weight(1f),
                    state = pagerState,
                    userScrollEnabled = false
                ) {

                    when(it){
                        0 -> GeneralInfoPage(viewModel = viewModel, generalInfoState = state.generalInfo)
                        1 -> CharacteristicsPage(viewModel = viewModel, characteristicsState = state.characteristics)
                        2 -> LocationPage(viewModel = viewModel, locationState = state.location)
                    }
                }
            }
        }
    }
}

