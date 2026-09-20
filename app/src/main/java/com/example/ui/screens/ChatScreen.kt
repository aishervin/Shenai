package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ShenModel
import com.example.ui.components.MessageBubble
import com.example.ui.components.ModelSelectorDropdown
import com.example.ui.components.ShenLogoGlyph
import com.example.ui.components.TypingIndicator
import com.example.ui.components.WebInspectorDialog
import com.example.ui.theme.ShenBackground
import com.example.ui.theme.ShenGlassBorder
import com.example.ui.theme.ShenGlassSurface
import com.example.ui.theme.ShenNeonCyan
import com.example.ui.theme.ShenNeonGreen
import com.example.ui.theme.ShenNeonPink
import com.example.ui.theme.ShenNeonPurple
import com.example.ui.theme.ShenRgbGradient
import com.example.ui.theme.ShenSurface
import com.example.ui.theme.ShenSurfaceElevated
import com.example.ui.theme.ShenTextPrimary
import com.example.ui.theme.ShenTextSecondary
import com.example.ui.theme.ShenTextTertiary
import com.example.viewmodel.ChatUiState
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size, uiState.isWaitingResponse) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ShenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShenLogoGlyph(size = 32)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SHΞN™ᴢᴇʀᴏ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Connection indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.sessionState.isWsActive) ShenNeonGreen
                                            else if (uiState.sessionState.isConnected) ShenNeonCyan
                                            else ShenNeonPink
                                        )
                                )
                            }
                            Text(
                                text = uiState.sessionState.statusMessage,
                                color = ShenTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Model Selector Dropdown
                    ModelSelectorDropdown(
                        selectedModel = uiState.selectedModel,
                        onModelSelected = { viewModel.selectModel(it) }
                    )

                    // Session / Cloudflare web inspector
                    IconButton(
                        onClick = { viewModel.toggleWebInspector() },
                        modifier = Modifier.testTag("open_web_inspector_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Session Sync & Verification",
                            tint = if (uiState.sessionState.cloudflareDetected) ShenNeonPink else ShenNeonCyan
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = { viewModel.toggleSettings() },
                        modifier = Modifier.testTag("open_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ShenTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShenSurface
                )
            )
        },
        bottomBar = {
            // Chat Input Bar with RGB Glow border and Send button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShenSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF141422))
                        .border(1.2.dp, ShenGlassBorder, RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputChanged(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        textStyle = TextStyle(
                            color = ShenTextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(ShenNeonCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.sendMessage() }
                        ),
                        decorationBox = { innerTextField ->
                            if (uiState.inputText.isEmpty()) {
                                Text(
                                    text = "Ask ${uiState.selectedModel.displayName}...",
                                    color = ShenTextTertiary,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Circular RGB Send Button
                    val isEnabled = uiState.inputText.isNotBlank() && !uiState.isWaitingResponse
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEnabled) ShenRgbGradient else SolidColor(Color(0xFF232336))
                            )
                            .clickable(enabled = isEnabled) {
                                viewModel.sendMessage()
                            }
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = if (isEnabled) Color.White else Color(0xFF555570),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cloudflare / Verification alert if detected
                AnimatedVisibility(
                    visible = uiState.sessionState.cloudflareDetected,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1422))
                            .clickable { viewModel.toggleWebInspector() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ShenNeonPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Browser verification prompt detected. Tap to solve once.",
                                color = ShenNeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Verify",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Chat Messages List
                if (uiState.messages.isEmpty()) {
                    EmptyChatState(
                        model = uiState.selectedModel,
                        onPromptSelected = { prompt ->
                            viewModel.sendMessage(prompt)
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { msg ->
                            MessageBubble(message = msg)
                        }

                        if (uiState.isWaitingResponse) {
                            item {
                                TypingIndicator(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .testTag("typing_indicator")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Web Inspector Dialog
    if (uiState.showWebInspector) {
        WebInspectorDialog(
            webViewManager = viewModel.webViewManager,
            onDismiss = { viewModel.toggleWebInspector() }
        )
    }

    // Settings Screen overlay
    if (uiState.showSettings) {
        SettingsScreen(
            currentModel = uiState.selectedModel,
            onModelSelected = { viewModel.selectModel(it) },
            onClearHistory = { viewModel.clearHistory() },
            onResetCookies = { viewModel.flushCookies() },
            onReloadSession = { viewModel.reloadSession() },
            onBack = { viewModel.toggleSettings() }
        )
    }
}

@Composable
fun EmptyChatState(
    model: ShenModel,
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starterPrompts = remember(model.id) {
        when (model.id) {
            "coder" -> listOf(
                "Optimize a concurrent Kotlin Coroutine channel pipeline",
                "Write a WebSocket listener with automatic exponential backoff",
                "Implement a Room database with dynamic KSP migrations"
            )
            "beta" -> listOf(
                "Analyze the trade-offs of localized AI vs cloud reasoning matrices",
                "Synthesize a step-by-step framework for zero-trust security",
                "What are the emerging trends in distributed neural computing?"
            )
            "darkweb" -> listOf(
                "Explain how sovereign cryptographic tokens verify session state",
                "Deconstruct the architecture of decentralized mesh networks",
                "Provide an uncensored deep-layer structural audit"
            )
            else -> listOf(
                "Explain quantum computing in three concise paragraphs",
                "How does the SHΞN™ session keeper maintain real-time state?",
                "Draft a high-impact product announcement for SHΞN™ᴢᴇʀᴏ"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShenLogoGlyph(size = 64)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = model.displayName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = model.description,
            color = ShenTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SUGGESTED INITIALIZATIONS",
            color = ShenNeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        starterPrompts.forEach { prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShenGlassSurface)
                    .border(1.dp, ShenGlassBorder, RoundedCornerShape(12.dp))
                    .clickable { onPromptSelected(prompt) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("starter_prompt_chip")
            ) {
                Text(
                    text = prompt,
                    color = ShenTextPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
