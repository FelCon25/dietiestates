package it.unina.dietiestates

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import it.unina.dietiestates.core.presentation.MainScreen
import it.unina.dietiestates.core.presentation.MainScreenEvent
import it.unina.dietiestates.core.presentation.MainScreenViewModel
import it.unina.dietiestates.ui.theme.DietiestatesAndroidClientTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.value.isReady.not()
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        intent.getStringExtra("propertyId")?.let { propertyId ->
            propertyId.toIntOrNull()?.let {
                viewModel.onEvent(MainScreenEvent.OnReceivedPushNotification(it))
            }
        }

        setContent {
            DietiestatesAndroidClientTheme {
                MainScreen(viewModel)
            }
        }
    }
}