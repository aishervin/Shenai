package com.example.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class BridgeEvent {
    data class HttpResponse(val url: String, val body: String) : BridgeEvent()
    data class WsMessage(val url: String, val data: String) : BridgeEvent()
    data class WsOpen(val url: String) : BridgeEvent()
    data class WsClose(val url: String) : BridgeEvent()
    data class PageStatus(val title: String, val url: String) : BridgeEvent()
}

class AndroidBridge(
    private val onEventReceived: ((BridgeEvent) -> Unit)? = null
) {
    companion object {
        const val TAG = "SHEN_DEBUG"
        const val BRIDGE_NAME = "AndroidBridge"
    }

    private val _events = MutableSharedFlow<BridgeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<BridgeEvent> = _events.asSharedFlow()

    @JavascriptInterface
    fun onResponse(url: String, body: String) {
        val preview = if (body.length > 250) body.take(250) + "..." else body
        Log.d(TAG, "onResponse received from $url: $preview")
        val event = BridgeEvent.HttpResponse(url, body)
        _events.tryEmit(event)
        onEventReceived?.invoke(event)
    }

    @JavascriptInterface
    fun onWsMessage(url: String, data: String) {
        val preview = if (data.length > 250) data.take(250) + "..." else data
        Log.d(TAG, "onWsMessage received from $url: $preview")
        val event = BridgeEvent.WsMessage(url, data)
        _events.tryEmit(event)
        onEventReceived?.invoke(event)
    }

    @JavascriptInterface
    fun onWsOpen(url: String) {
        Log.d(TAG, "onWsOpen: WebSocket connected at $url")
        val event = BridgeEvent.WsOpen(url)
        _events.tryEmit(event)
        onEventReceived?.invoke(event)
    }

    @JavascriptInterface
    fun onWsClose(url: String) {
        Log.d(TAG, "onWsClose: WebSocket closed at $url")
        val event = BridgeEvent.WsClose(url)
        _events.tryEmit(event)
        onEventReceived?.invoke(event)
    }

    @JavascriptInterface
    fun onPageReady(url: String, title: String) {
        Log.d(TAG, "onPageReady: $title at $url")
        val event = BridgeEvent.PageStatus(title, url)
        _events.tryEmit(event)
        onEventReceived?.invoke(event)
    }
}
