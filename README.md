# SHΞN™ᴢᴇʀᴏ (com.shenzero.ai)

An autonomous, white-label AI chat client engineered with Kotlin, Jetpack Compose, and real-time WebView session persistence.

## Features
- **Branded Ecosystem**: Custom SHΞN™ᴢᴇʀᴏ design language, cybernetic dark theme with RGB gradient lighting (`#FF3D81` → `#7B2FF7` → `#00E5FF`), and glassmorphism.
- **4 Neural Models**:
  - `SHΞN™ Alpha`: Default, instant conversational synthesis.
  - `SHΞN™ Beta`: Balanced deep-reasoning matrix.
  - `SHΞN™ Coder`: Algorithmic & syntax specialized engine.
  - `SHΞN™ Darkweb`: Advanced synthetic intelligence.
- **Persistent Session & Network Interception**:
  - Background `1x1 dp` WebView with cookie persistence via `CookieManager.flush()`.
  - Android Foreground Service (`ShenSessionService`) maintaining real-time cookies and WebSocket connectivity.
  - JavaScript bridge intercepting `fetch`, `XMLHttpRequest`, and `WebSocket` events directly to `AndroidBridge` and ViewModel flows.
  - Dedicated Session & Verification sheet to inspect live cookies and resolve Cloudflare challenges seamlessly.
- **Local Persistence**: `DataStore` storing conversation history and model preferences.

## Build Instructions

### Build Release APK
To assemble the signed release APK with your configured credentials:

```bash
./gradlew assembleRelease
```

The compiled APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

### Build Debug APK
For local testing and emulator inspection:

```bash
./gradlew assembleDebug
```

### Run Unit & Robolectric Tests
```bash
./gradlew testDebugUnitTest
```
