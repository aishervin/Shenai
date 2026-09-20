package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ShenBackground
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ShenBackground
                ) {
                    val uiState by chatViewModel.uiState.collectAsState()

                    Crossfade(
                        targetState = when {
                            !uiState.isSplashFinished -> ScreenState.SPLASH
                            uiState.showOnboarding -> ScreenState.ONBOARDING
                            else -> ScreenState.CHAT
                        },
                        animationSpec = tween(350),
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            ScreenState.SPLASH -> {
                                SplashScreen(
                                    onTimeout = { chatViewModel.finishSplash() }
                                )
                            }
                            ScreenState.ONBOARDING -> {
                                OnboardingScreen(
                                    onFinish = { chatViewModel.finishOnboarding() }
                                )
                            }
                            ScreenState.CHAT -> {
                                ChatScreen(
                                    viewModel = chatViewModel,
                                    uiState = uiState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class ScreenState {
    SPLASH, ONBOARDING, CHAT
}

// Retained for screenshot & UI tests compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
