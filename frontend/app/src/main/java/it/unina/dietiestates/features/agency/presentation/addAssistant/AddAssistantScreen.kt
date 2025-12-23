package it.unina.dietiestates.features.agency.presentation.addAssistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.core.presentation._compontents.CustomTextField
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.agency.domain.Assistant
import org.koin.androidx.compose.koinViewModel

private val ErrorRed = Color(0xFFD32F2F)
private val ErrorRedLight = Color(0xFFFFEBEE)
private val ErrorRedBorder = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddAssistantScreen(
    viewModel: AddAssistantScreenViewModel = koinViewModel(),
    onBackNavigation: () -> Unit,
    onNewAssistantAdded: (Assistant) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event){
            is AddAssistantScreenEvent.OnAddAssistantSucceeded -> {
                onNewAssistantAdded(event.assistant)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Assistant",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackNavigation) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header description
            Text(
                text = "Create a new assistant account for your agency",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Error message
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRedLight
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
                            tint = ErrorRed,
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

            // Email Card
            InputCard(
                icon = Icons.Rounded.AlternateEmail,
                title = "Email Address",
                placeholder = "Enter assistant's email",
                value = state.email,
                onValueChange = viewModel::onInputEmailChange,
                isError = state.emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First Name Card
            InputCard(
                icon = Icons.Rounded.Person,
                title = "First Name",
                placeholder = "Enter first name",
                value = state.firstName,
                onValueChange = viewModel::onInputFirstNameChange,
                isError = state.firstNameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name Card
            InputCard(
                icon = Icons.Rounded.Person,
                title = "Last Name",
                placeholder = "Enter last name",
                value = state.lastName,
                onValueChange = viewModel::onInputLastNameChange,
                isError = state.lastNameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Card
            InputCard(
                icon = Icons.Rounded.Phone,
                title = "Phone Number",
                placeholder = "Enter phone number (optional)",
                value = state.phone ?: "",
                onValueChange = viewModel::onInputPhoneChange,
                isError = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Card
            InputCard(
                icon = Icons.Rounded.Key,
                title = "Password",
                placeholder = "Enter password (min. 8 characters)",
                value = state.password,
                onValueChange = viewModel::onInputPasswordChange,
                isPassword = true,
                isError = state.passwordError
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Add Assistant Button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onClick = viewModel::addAssistant,
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(6.dp))
                    Text(
                        text = "Add Assistant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InputCard(
    icon: ImageVector,
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    val borderColor by animateColorAsState(
        targetValue = if (isError) ErrorRedBorder else Color.Gray.copy(alpha = 0.15f),
        label = "borderColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        label = "iconTint"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurface,
        label = "titleColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) ErrorRedLight.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isError) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.padding(6.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            CustomTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(placeholder, fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.5f))
                },
                icon = {},
                isPasswordTextField = isPassword
            )
        }
    }
}
