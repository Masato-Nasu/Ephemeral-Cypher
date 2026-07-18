# Ephemeral Cypher Android v0.1.6

Android edition of Ephemeral Cypher with an on-device in-app browser for capturing web pages as cryptographic key material.

## Main flow

1. Open Ephemeral Cypher.
2. Enter an HTTPS URL and tap **ページを鍵にする**.
3. The page opens visibly in the in-app browser.
4. Log in there if the site requires authentication.
5. After confirming the page is fully displayed, tap **このページを鍵にする** in the browser toolbar.
6. The app reads the currently displayed DOM text locally, applies the Ephemeral Cypher normalization rule, computes SHA-256 on-device, and returns to the encryption UI.
7. Encrypt or decrypt with the resulting page fingerprint.

The Android Share menu remains available as a shortcut: sharing an HTTPS page to Ephemeral Cypher opens that page in the same in-app browser, where the user explicitly chooses when to capture it.

## Security model

- HTTPS pages only.
- Cleartext HTTP is disabled in the Android manifest.
- No third-party relay server.
- No `addJavascriptInterface()` bridge is exposed to browsed web pages.
- The packaged app UI communicates with native Android only through a private custom navigation command intercepted by the app.
- WebView local file/content access is disabled.
- File-URL and universal file-URL access are disabled.
- Mixed content is blocked.
- Safe Browsing is enabled.
- JavaScript pop-up windows and multiple windows are disabled.
- Third-party cookies are disabled; first-party cookies are allowed so ordinary site login can work.
- Page text and the generated fingerprint are not sent to the developer or a relay service.
- The target website necessarily receives the normal direct page request from the device.

## PC compatibility

The visible-text normalization and SHA-256 page-fingerprint procedure is kept aligned with Ephemeral Cypher Companion. Stable pages should therefore be cross-compatible between PC and Android.

Dynamic, personalized, A/B-tested, geolocated, or time-varying pages can still produce different fingerprints across devices or sessions.

## Build

Open this folder in Android Studio, sync Gradle, then run on Android 8.0+.

Release APK: **Build > Generate Signed App Bundle or APK > APK**.

Package: `com.masatonasu.ephemeralcypher`
