package com.example.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "SHΞN™ Alpha",
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class ShenModel(
    val id: String,
    val displayName: String,
    val description: String,
    val speed: String,
    val badge: String
) {
    companion object {
        val ALPHA = ShenModel(
            id = "alpha",
            displayName = "SHΞN™ Alpha",
            description = "Fast, lightweight neural architecture for instant conversational responses.",
            speed = "Lightning",
            badge = "Default"
        )
        val BETA = ShenModel(
            id = "beta",
            displayName = "SHΞN™ Beta",
            description = "Balanced deep-reasoning matrix for analytical logic and nuanced synthesis.",
            speed = "Adaptive",
            badge = "Balanced"
        )
        val CODER = ShenModel(
            id = "coder",
            displayName = "SHΞN™ Coder",
            description = "Specialized algorithmic parser for code architecture, debugging, and scripts.",
            speed = "Precise",
            badge = "Coding"
        )
        val DARKWEB = ShenModel(
            id = "darkweb",
            displayName = "SHΞN™ Darkweb",
            description = "Advanced synthetic intelligence trained for complex multi-layer intelligence.",
            speed = "Deep",
            badge = "Advanced"
        )

        val ALL_MODELS = listOf(ALPHA, BETA, CODER, DARKWEB)

        fun fromId(id: String): ShenModel {
            return ALL_MODELS.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALPHA
        }
    }
}

data class SessionState(
    val isConnected: Boolean = false,
    val isPageLoaded: Boolean = false,
    val isWsActive: Boolean = false,
    val lastUrl: String = "https://chat.dphn.ai",
    val statusMessage: String = "Initializing neural bridge...",
    val cloudflareDetected: Boolean = false,
    val activeCookiesCount: Int = 0
)
