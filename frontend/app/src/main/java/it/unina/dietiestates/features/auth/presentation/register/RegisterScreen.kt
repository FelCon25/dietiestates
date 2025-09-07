package it.unina.dietiestates.features.auth.presentation.register

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.R
import it.unina.dietiestates.core.data.googleAuth.GoogleAuthUtil
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.core.presentation.util.ObserveAsEvents
import it.unina.dietiestates.features.auth.presentation._compontents.CustomTextField
import it.unina.dietiestates.features.auth.presentation._compontents.GoogleSignInButton
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModel = koinViewModel(),
    onNavigateToSignInScreen: () -> Unit,
    onAuthSucceeded: (User) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleAuthUtil = remember {GoogleAuthUtil(context = context)}

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.eventsChannelFlow) { event ->
        when(event){
            is RegisterScreenEvent.OnRegisterSucceeded -> {
                onAuthSucceeded(event.user)
            }
            is RegisterScreenEvent.OnRegisterFailed -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
            is RegisterScreenEvent.OnWrongValueTextField -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
            is RegisterScreenEvent.OnGoogleAuthFailed -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("There was a problem with Google authentication.")
                }
            }
        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .background(Green80)
                .statusBarsPadding()
        ) {

            Text(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                text = "Register",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        modifier = Modifier.height(125.dp),
                        painter = painterResource(R.drawable.dietiestates_logo),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CustomTextField(
                        value = state.email,
                        onValueChange = viewModel::onInputEmailValueChange,
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
                        onValueChange = viewModel::onInputFirstNameValueChange,
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

                    CustomTextField(
                        value = state.lastName,
                        onValueChange = viewModel::onInputLastNameValueChange,
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
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CustomTextField(
                        value = state.password,
                        onValueChange = viewModel::onInputPasswordValueChange,
                        placeholder = {
                            Text("Password", fontSize = 14.sp)
                        },
                        icon = {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Lock icon", tint = MaterialTheme.colorScheme.primary)
                        },
                        isPasswordTextField = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::submitRegister,
                        shape = ShapeDefaults.Medium,
                        enabled = !state.isLoading
                    ) {
                        Text("Register")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider()

                        Text(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 8.dp),
                            text = "OR CONTINUE WITH",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GoogleSignInButton {
                        coroutineScope.launch {
                            googleAuthUtil.sendSignInRequest(
                                onSuccess = { token ->
                                    viewModel.sendGoogleAuth(token)
                                },
                                onFailure = {
                                    viewModel.onEvent(RegisterScreenEvent.OnGoogleAuthFailed)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                }
            }

            Row(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 12.sp
                )

                TextButton(
                    onClick = onNavigateToSignInScreen
                ) {
                    Text(
                        text = "Sign in",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }

    }
}