package it.unina.dietiestates.features.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.unina.dietiestates.BuildConfig
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.profile.presentation._components.SessionItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileScreenViewModel = koinViewModel(),
    onBackNavigation: (userUpdated: User) -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    var showLogoutAlert by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.changeProfilePic(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackNavigation(state.user!!)
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Arrow back icon")
                    }
                },
                title = {
                    Text("Edit Profile")
                },
                actions = {
                    IconButton(
                        onClick = {
                            showLogoutAlert = true
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = "Logout icon")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            state.user?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(percent = 100))
                                .clickable {
                                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                        ) {

                            if(user.profilePic != null){
                                AsyncImage(
                                    modifier = Modifier.fillMaxSize(),
                                    model = BuildConfig.BASE_URL + state.user?.profilePic,
                                    contentDescription = "User profile picture",
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else{
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ){}
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = "Add a photo icon",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "${user.firstName} ${user.lastName}",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(32.dp)
                    )

                    Text(
                        text = "Info",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = "Email icon")

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = user.email
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Phone, contentDescription = "Phone icon")

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = user.phone ?: "Phone number not entered."
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Notifications",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .clip(ShapeDefaults.Medium)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.House, contentDescription = "House icon")

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "New property notifications",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Enable notifications about properties that may interest you.",
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = state.notificationPreferences.find { it.category == "NEW_PROPERTY_MATCH" }?.enabled ?: false,
                            onCheckedChange = { }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(ShapeDefaults.Medium)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Campaign, contentDescription = "Campaign Icon")

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Promotional notifications",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Enable notifications to receive promotional messages.",
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = state.notificationPreferences.find { it.category == "PROMOTIONAL" }?.enabled ?: false,
                            onCheckedChange = {

                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Sessions",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    repeat(state.sessions.size) { i ->

                        val session = state.sessions[i]
                        val isCurrentSession = session.sessionId == state.currentSessionId

                        SessionItem(
                            session = session,
                            onSessionDelete = {
                                if(isCurrentSession){
                                    viewModel.logout()
                                }
                                else{
                                    viewModel.deleteSession(session.sessionId)
                                }
                            },
                            isCurrentSession = isCurrentSession
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }


        if(showLogoutAlert){
            AlertDialog(
                text = {
                    Text(
                        text = "Are you sure you want to log out?",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton (
                        onClick = viewModel::logout,
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton (
                        onClick = {
                            showLogoutAlert = false
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                onDismissRequest = {
                    showLogoutAlert = false
                }
            )
        }
    }
}