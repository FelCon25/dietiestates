package it.unina.dietiestates.features.agency.presentation.addAssistant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.core.presentation._compontents.CustomTextField
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.agency.domain.Assistant
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddAssistantScreen(
    viewModel: AddAssistantScreenViewModel = koinViewModel(),
    onBackNavigation: () -> Unit,
    onNewAssistantAdded: (Assistant) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->

        when(event){
            is AddAssistantScreenEvent.OnAddAssistantSucceeded -> {
                onNewAssistantAdded(event.assistant)
            }
            is AddAssistantScreenEvent.OnAddAssistantFailed -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
            is AddAssistantScreenEvent.OnWrongValueTextField -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Assistant")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackNavigation
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Arrow back icon")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CustomTextField(
                    value = state.email,
                    onValueChange = viewModel::onInputEmailChange,
                    placeholder = {
                        Text("Email", fontSize = 14.sp)
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = "Email icon", tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "First name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CustomTextField(
                    value = state.firstName,
                    onValueChange = viewModel::onInputFirstNameChange,
                    placeholder = {
                        Text("First name", fontSize = 14.sp)
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = "Person icon", tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Last name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CustomTextField(
                    value = state.lastName,
                    onValueChange = viewModel::onInputLastNameChange,
                    placeholder = {
                        Text("Last name", fontSize = 14.sp)
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = "Person icon", tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Phone",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CustomTextField(
                    value = state.phone ?: "",
                    onValueChange = viewModel::onInputPhoneChange,
                    placeholder = {
                        Text("Phone", fontSize = 14.sp)
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Phone, contentDescription = "Phone icon", tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                CustomTextField(
                    value = state.password,
                    onValueChange = viewModel::onInputPasswordChange,
                    placeholder = {
                        Text("Passowrd", fontSize = 14.sp)
                    },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = "Person icon", tint = MaterialTheme.colorScheme.primary)
                    },
                    isPasswordTextField = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::addAssistant,
                    shape = ShapeDefaults.Medium,
                    enabled = !state.isLoading
                ) {
                    Text("Add Assistant")
                }
            }
        }


    }

}