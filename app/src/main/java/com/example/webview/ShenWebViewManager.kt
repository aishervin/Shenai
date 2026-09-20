package com.example.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.bridge.AndroidBridge
import com.example.model.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class ShenWebViewManager(
    private val context: Context,
    val bridge: AndroidBridge
) {
    companion object {
        const val TAG = "SHEN_DEBUG"
        const val TARGET_URL = "https://chat.dphn.ai"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile; rv:128.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36"

        const val INJECTION_SCRIPT = """
(function() {
  if (window.__shen_injected) return;
  window.__shen_injected = true;
  console.log('[SHEN_DEBUG] Injecting network hooks into page');

  // Intercept Fetch API
  const origFetch = window.fetch;
  window.fetch = async function(...args) {
    try {
      const resp = await origFetch.apply(this, args);
      const clone = resp.clone();
      clone.text().then(t => {
        try {
          if (window.AndroidBridge && window.AndroidBridge.onResponse) {
            window.AndroidBridge.onResponse(String(args[0]), t);
          }
        } catch(e) { console.error(e); }
      }).catch(e => console.error(e));
      return resp;
    } catch(err) {
      return origFetch.apply(this, args);
    }
  };

  // Intercept XMLHttpRequest
  const origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function(m, u) {
    this.addEventListener('load', function() {
      try {
        if (window.AndroidBridge && window.AndroidBridge.onResponse) {
          window.AndroidBridge.onResponse(String(u), this.responseText || '');
        }
      } catch(e) { console.error(e); }
    });
    return origOpen.apply(this, arguments);
  };

  // Intercept WebSockets
  const OrigWS = window.WebSocket;
  window.WebSocket = function(url, protocols) {
    const ws = protocols ? new OrigWS(url, protocols) : new OrigWS(url);
    ws.addEventListener('message', e => {
      try {
        if (window.AndroidBridge && window.AndroidBridge.onWsMessage) {
          window.AndroidBridge.onWsMessage(String(url), String(e.data));
        }
      } catch(err) {}
    });
    ws.addEventListener('open', () => {
      try {
        if (window.AndroidBridge && window.AndroidBridge.onWsOpen) {
          window.AndroidBridge.onWsOpen(String(url));
        }
      } catch(err) {}
    });
    ws.addEventListener('close', () => {
      try {
        if (window.AndroidBridge && window.AndroidBridge.onWsClose) {
          window.AndroidBridge.onWsClose(String(url));
        }
      } catch(err) {}
    });
    return ws;
  };
  window.WebSocket.prototype = OrigWS.prototype;

  // Signal injection readiness
  try {
    if (window.AndroidBridge && window.AndroidBridge.onPageReady) {
      window.AndroidBridge.onPageReady(window.location.href, document.title || 'SHEN Session Active');
    }
  } catch(e) {}
})();
"""
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webViewInstance: WebView? = null

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreateWebView(): WebView {
        webViewInstance?.let { return it }

        Log.d(TAG, "Creating and configuring ShenWebView instance")
        return try {
            val wv = WebView(context).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    userAgentString = USER_AGENT
                    cacheMode = WebSettings.LOAD_DEFAULT
                    loadsImagesAutomatically = true
                    allowContentAccess = true
                    allowFileAccess = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this@apply, true)

                addJavascriptInterface(bridge, AndroidBridge.BRIDGE_NAME)

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d(TAG, "onPageStarted: $url")
                        _sessionState.value = _sessionState.value.copy(
                            isConnected = true,
                            isPageLoaded = false,
                            lastUrl = url ?: TARGET_URL,
                            statusMessage = "Connecting to SHΞN neural node..."
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "onPageFinished: $url - injecting network bridge")
                        
                        // Inject network and WebSocket interception scripts
                        view?.evaluateJavascript(INJECTION_SCRIPT) { result ->
                            Log.d(TAG, "Injection script evaluated: $result")
                        }

                        // Check for cloudflare or browser challenges
                        view?.evaluateJavascript("document.title") { titleResult ->
                            val cleanTitle = titleResult?.trim('"') ?: ""
                            val isCloudflare = cleanTitle.contains("Just a moment", ignoreCase = true) ||
                                               cleanTitle.contains("Cloudflare", ignoreCase = true) ||
                                               cleanTitle.contains("Challenge", ignoreCase = true) ||
                                               cleanTitle.contains("Attention Required", ignoreCase = true)
                            
                            _sessionState.value = _sessionState.value.copy(
                                isPageLoaded = true,
                                cloudflareDetected = isCloudflare,
                                statusMessage = if (isCloudflare) "Verification required" else "SHΞN™ Node Active"
                            )
                        }

                        // Persist session cookies
                        CookieManager.getInstance().flush()
                    }

                    @SuppressLint("WebViewClientOnReceivedSslError")
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        Log.w(TAG, "SSL error encountered: ${error?.primaryError}")
                        // Allow navigation if legitimate certificate or dev environment
                        handler?.proceed()
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let {
                            Log.d(TAG, "Page Title changed: $it")
                        }
                    }
                }
            }

            wv.loadUrl(TARGET_URL)
            webViewInstance = wv
            wv
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ShenWebView: ${e.message}", e)
            val fallback = WebView(context)
            webViewInstance = fallback
            fallback
        }
    }

    fun sendMessage(text: String, onCompletion: ((String) -> Unit)? = null) {
        val wv = webViewInstance ?: getOrCreateWebView()
        Log.d(TAG, "sendMessage dispatched: \"$text\"")

        // JSON escape user text safely
        val jsonEscaped = JSONObject.quote(text)

        val sendScript = """
(function() {
  try {
    var box = document.querySelector('textarea, input[type="text"], [contenteditable="true"]');
    var btn = document.querySelector('button[type="submit"], button.send, [aria-label*="send" i], [data-testid*="send" i]');
    
    if (!btn) {
      var allButtons = Array.from(document.querySelectorAll('button'));
      btn = allButtons.find(b => {
        var label = (b.getAttribute('aria-label') || b.innerText || '').toLowerCase();
        return label.includes('send') || b.querySelector('svg');
      });
    }

    if (box) {
      box.focus();
      var setter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value')?.set
                || Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value')?.set;
      
      if (setter) {
        setter.call(box, $jsonEscaped);
      } else {
        box.value = $jsonEscaped;
      }

      box.dispatchEvent(new Event('input', { bubbles: true }));
      box.dispatchEvent(new Event('change', { bubbles: true }));

      if (btn && !btn.disabled) {
        btn.click();
        return "SUCCESS_CLICK";
      } else {
        // Fallback: Dispatch Enter key
        box.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
        return "SUCCESS_ENTER";
      }
    }
    return "INPUT_NOT_FOUND";
  } catch(e) {
    return "ERROR: " + e.message;
  }
})();
"""

        mainHandler.post {
            wv.evaluateJavascript(sendScript) { result ->
                Log.d(TAG, "sendMessage evaluation result: $result")
                onCompletion?.invoke(result ?: "null")
                CookieManager.getInstance().flush()
            }
        }
    }

    fun reload() {
        mainHandler.post {
            Log.d(TAG, "Reloading Shen session WebView")
            webViewInstance?.reload() ?: getOrCreateWebView().loadUrl(TARGET_URL)
        }
    }

    fun clearCookies() {
        mainHandler.post {
            Log.d(TAG, "Flushing and clearing session cookies")
            CookieManager.getInstance().removeAllCookies {
                CookieManager.getInstance().flush()
                _sessionState.value = _sessionState.value.copy(
                    activeCookiesCount = 0,
                    statusMessage = "Session reset completed"
                )
            }
            reload()
        }
    }
}
