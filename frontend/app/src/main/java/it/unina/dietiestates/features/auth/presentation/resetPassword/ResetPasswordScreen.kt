package it.unina.dietiestates.features.auth.presentation.resetPassword

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unina.dietiestates.R
import it.unina.dietiestates.core.presentation._compontents.CustomTextField
import it.unina.dietiestates.ui.theme.Green80
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = koinViewModel(),
    onBackNavigation: () -> Unit,
    onPasswordReset: () -> Unit
){
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(state.shouldNavigate) {
        if (state.shouldNavigate) {
            onPasswordReset()
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .background(Green80)
                .statusBarsPadding()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackNavigation) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green80
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Image(
                    modifier = Modifier.height(125.dp),
                    painter = painterResource(R.drawable.dietiestates_logo),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Success message
                if (state.successMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error message
                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!state.codeVerified) {
                    // STEP 1: Enter reset code
                    Text(
                        text = "Enter the 6-digit code sent to your email.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Verification Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modern OTP Input
                    OtpInput(
                        code = state.code,
                        onCodeChange = viewModel::onCodeChange
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::verifyCode,
                        shape = ShapeDefaults.Medium,
                        enabled = state.code.length == 6 && !state.isLoading
                    ) {
                        Text(if (state.isLoading) "Verifying..." else "Continue")
                    }
                } else {
                    // STEP 2: Enter new password
                    Text(
                        text = "Create your new password.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "New Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CustomTextField(
                        value = state.newPassword,
                        onValueChange = viewModel::onNewPasswordChange,
                        placeholder = {
                            Text("New password", fontSize = 14.sp)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Lock icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isPasswordTextField = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Confirm Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CustomTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        placeholder = {
                            Text("Confirm password", fontSize = 14.sp)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Lock icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isPasswordTextField = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::resetPassword,
                        shape = ShapeDefaults.Medium,
                        enabled = !state.isLoading
                    ) {
                        Text(if (state.isLoading) "Resetting..." else "Reset Password")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = code,
        onValueChange = { newValue ->
            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                onCodeChange(newValue)
            }
        },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val digit = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = code.length == index
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = when {
                                    digit.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    else -> Color.White
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = when {
                                    digit.isNotEmpty() -> MaterialTheme.colorScheme.primary
                                    isFocused -> MaterialTheme.colorScheme.primary
                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (digit.isNotEmpty()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                Color.Transparent
                        )
                    }
                    
                    if (index < 5) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            // TextField nascosto per il focus
            Box(modifier = Modifier.size(0.dp)) {
                innerTextField()
            }
        }
    )
}
