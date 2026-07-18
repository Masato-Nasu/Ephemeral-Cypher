# Changelog

## v0.1.7
- Fixed toolbar overlapping the Android status bar: targetSdk 35 enforces edge-to-edge, so system-bar / display-cutout / IME insets are now applied as root padding (`applySystemInsets`). No overlap on Android 15; inert on Android 14 and below.
- Restyled the whole UI to the paper/ink/vermilion language (紙 #E7E3D8 · 墨 #211F1A · 朱 #B5372A): flat paper ground with faint ruled lines, sharp corners, hairline borders, tracked-out red labels, filled vermilion primary action. CSS values only — selectors, DOM, and all crypto/bridge logic untouched.
- The in-app capture browser toolbar now matches: paper toolbar with hairline rule, ink monospace URL, vermilion-bordered capture button.
- Added `android:windowSoftInputMode="adjustResize"`; theme bar colors synced to #E7E3D8 for pre-15 devices.
- versionCode 8 → 9, versionName 0.1.6 → 0.1.7.

## v0.1.6 (revision 2)
- Fixed silent loss of imported TXT content: the native picker result is now delivered to the WebView with an explicit handshake (`ok`/`retry`), retried until `setAndroidTextContent` is available, and re-delivered from `onPageFinished` if the Activity/WebView was recreated while the file picker was open.
- The "TXTを読み込みました。" toast is now shown only after the WebView confirms receipt; a failure toast is shown if delivery ultimately fails.
- Hardened ciphertext parsing: zero-width characters (U+200B/200C/200D/FEFF) and embedded whitespace are stripped from the payload before Base64 decoding. Accepts strictly more inputs; no change to key derivation (HKDF), AES-GCM parameters, or the EC2/EC1/legacy formats.
- versionCode 7 → 8 (versionName stays 0.1.6).

- Fixed Android **TXTで保存**: Android WebView no longer relies on `Blob` + `a.download`; it now opens the native document creator and writes UTF-8 text through `ContentResolver`.
- Web builds continue to use browser `Blob` downloads.
## v0.1.6
- Fixed the **TXTを読み込む** button in the packaged Android UI.
- Replaced the overlapping transparent `input[type=file]` control with a normal button.
- Android now routes the button explicitly to `ephemeral://picktxt`, invoking the existing native TXT picker.
- Web builds keep TXT import support by explicitly calling `cipherFile.click()`.

## v0.1.5
- Fixed Android page fingerprint handoff after in-app browser capture.
- Captured page keys are now applied directly to the existing app screen without reloading it.
- KEY READY / selected page state and the Encrypt button update immediately after capture.
- Preserves any message already typed before opening the in-app browser.


## v0.1.5

- Replaced hidden automatic URL capture with a visible in-app browser flow.
- URL entry now opens the requested HTTPS page inside the app first.
- Added an explicit **このページを鍵にする** button; page content is captured only after user action.
- Login-required pages can be opened and authenticated inside the same WebView session before capture.
- Android share-menu URLs now open in the in-app browser instead of being captured invisibly.
- Removed `addJavascriptInterface()` from the app/native boundary.
- Added HTTPS-only navigation enforcement for the capture browser.
- Disabled local file/content access, mixed content, JavaScript pop-up windows, and multiple windows.
- Enabled Safe Browsing and disabled third-party cookies.

## v0.1.2

- Added direct URL input from the main Ephemeral Cypher UI.
- Retained Android share-menu capture as an optional shortcut.

## v0.1.1

- Improved delayed client-rendered page capture.


## v0.1.5
- Android native TXT file picker for reliable ciphertext import.
- Reads selected UTF-8 TXT locally and injects it directly into the decrypt input.
- Keeps WebView content/file access restrictions unchanged.
