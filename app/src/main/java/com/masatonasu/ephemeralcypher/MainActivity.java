package com.masatonasu.ephemeralcypher;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.SafeBrowsingResponse;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String APP_ORIGIN = "https://app.local/";

    // SANA紙面調パレット(assets/index.htmlの:rootと同期)
    private static final int PAPER = Color.rgb(231, 227, 216);        // #E7E3D8
    private static final int PAPER_FIELD = Color.rgb(243, 241, 232);  // #F3F1E8
    private static final int INK = Color.rgb(33, 31, 26);             // #211F1A
    private static final int INK_MUTED = Color.rgb(123, 118, 102);    // #7B7666
    private static final int LINE = Color.argb(87, 33, 31, 26);       // ink 34%
    private static final int VERMILION = Color.rgb(181, 55, 42);      // #B5372A
    private static final Pattern URL_PATTERN = Pattern.compile("https://[^\\s]+", Pattern.CASE_INSENSITIVE);
    private static final int REQUEST_PICK_TXT = 4101;
    private static final int REQUEST_SAVE_TXT = 4102;

    private FrameLayout root;
    private WebView appWebView;
    private WebView browserWebView;
    private LinearLayout browserContainer;
    private TextView browserUrlLabel;
    private TextView browserStatus;
    private Button captureButton;
    private String pendingTextToSave;
    private String pendingSaveFileName;
    // TXT読込結果の配送バッファ。WebView側ハンドラ未定義時(再生成直後など)に備え、
    // onPageFinished/リトライで確実に届いたことを確認してから破棄する。
    private String pendingLoadedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(PAPER);
        applySystemInsets(root);
        appWebView = createAppWebView();
        browserContainer = createBrowserContainer();

        root.addView(appWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(browserContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        browserContainer.setVisibility(View.GONE);

        setContentView(root);
        loadApp(null);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (browserContainer.getVisibility() == View.VISIBLE) {
            if (browserWebView.canGoBack()) browserWebView.goBack();
            else closeBrowser();
            return;
        }
        if (appWebView.canGoBack()) appWebView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (appWebView != null) appWebView.destroy();
        if (browserWebView != null) browserWebView.destroy();
        super.onDestroy();
    }

    /**
     * targetSdk 35 (Android 15) ではエッジトゥエッジが強制され、themes.xmlの
     * statusBarColorは無視される。システムバー/カットアウト/IMEのインセットを
     * ルートのパディングとして適用し、ツールバーとステータスバーの重なりを解消する。
     * Android 14以前(通常デコア)ではインセットは0で降ってくるため無害。
     */
    private void applySystemInsets(View target) {
        target.setOnApplyWindowInsetsListener((v, insets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
                left = bars.left; top = bars.top; right = bars.right;
                bottom = Math.max(bars.bottom, ime.bottom);
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(left, top, right, bottom);
            return Build.VERSION.SDK_INT >= 30
                    ? WindowInsets.CONSUMED
                    : insets.consumeSystemWindowInsets();
        });
    }

    private WebView createAppWebView() {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("app.local".equals(uri.getHost())) return assetResponse(uri.getPath());
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("app.local".equals(uri.getHost())) return false;

                // Private app-to-native command channel. No JavaScript bridge is exposed to web pages.
                if ("ephemeral".equalsIgnoreCase(uri.getScheme()) && "capture".equalsIgnoreCase(uri.getHost())) {
                    String rawUrl = uri.getQueryParameter("url");
                    openBrowser(rawUrl);
                    return true;
                }
                if ("ephemeral".equalsIgnoreCase(uri.getScheme()) && "picktxt".equalsIgnoreCase(uri.getHost())) {
                    pickTextFile();
                    return true;
                }
                if ("ephemeral".equalsIgnoreCase(uri.getScheme()) && "savetxt".equalsIgnoreCase(uri.getHost())) {
                    saveTextFile(uri.getQueryParameter("name"));
                    return true;
                }

                Intent browser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browser);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Activity再生成などでonActivityResultがページ読込完了より先に来た場合、
                // ここで保留中のTXTを再配送する。
                if (url != null && url.startsWith(APP_ORIGIN)) {
                    flushPendingLoadedText(0);
                }
            }
        });
        return webView;
    }

    private LinearLayout createBrowserContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(PAPER);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(10), dp(8), dp(10), dp(8));
        toolbar.setBackgroundColor(PAPER);

        Button close = new Button(this);
        close.setText("戻る");
        close.setAllCaps(false);
        close.setTextSize(13);
        close.setLetterSpacing(0.08f);
        close.setTextColor(INK);
        close.setStateListAnimator(null);
        GradientDrawable closeBg = new GradientDrawable();
        closeBg.setColor(PAPER_FIELD);
        closeBg.setStroke(Math.max(1, dp(1)), LINE);
        closeBg.setCornerRadius(dp(3));
        close.setBackground(closeBg);
        close.setOnClickListener(v -> closeBrowser());
        toolbar.addView(close, new LinearLayout.LayoutParams(dp(72), dp(44)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, dp(10), 0);
        browserUrlLabel = new TextView(this);
        browserUrlLabel.setTextColor(INK);
        browserUrlLabel.setTextSize(11);
        browserUrlLabel.setTypeface(Typeface.MONOSPACE);
        browserUrlLabel.setSingleLine(true);
        browserStatus = new TextView(this);
        browserStatus.setTextColor(INK_MUTED);
        browserStatus.setTextSize(10);
        browserStatus.setLetterSpacing(0.05f);
        labels.addView(browserUrlLabel);
        labels.addView(browserStatus);
        toolbar.addView(labels, new LinearLayout.LayoutParams(0, dp(48), 1f));

        captureButton = new Button(this);
        captureButton.setText("このページを\n鍵にする");
        captureButton.setAllCaps(false);
        captureButton.setTextSize(12);
        captureButton.setLetterSpacing(0.06f);
        captureButton.setLineSpacing(0f, 1.05f);
        captureButton.setStateListAnimator(null);
        captureButton.setTextColor(new ColorStateList(
                new int[][]{ new int[]{-android.R.attr.state_enabled}, new int[]{} },
                new int[]{ Color.argb(102, 181, 55, 42), VERMILION }));
        GradientDrawable captureBg = new GradientDrawable();
        captureBg.setColor(PAPER_FIELD);
        captureBg.setStroke(Math.max(2, Math.round(1.5f * getResources().getDisplayMetrics().density)), VERMILION);
        captureBg.setCornerRadius(dp(3));
        captureButton.setBackground(captureBg);
        captureButton.setOnClickListener(v -> captureCurrentPage());
        toolbar.addView(captureButton, new LinearLayout.LayoutParams(dp(132), dp(56)));

        container.addView(toolbar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View rule = new View(this);
        rule.setBackgroundColor(LINE);
        container.addView(rule, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(1))));

        browserWebView = createSecureBrowserWebView();
        container.addView(browserWebView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        return container;
    }

    private WebView createSecureBrowserWebView() {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSafeBrowsingEnabled(true);

        String ua = settings.getUserAgentString();
        if (ua != null) {
            ua = ua.replace("; wv", "").replace("Version/4.0 ", "");
            settings.setUserAgentString(ua);
        }

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!"https".equalsIgnoreCase(uri.getScheme())) {
                    Toast.makeText(MainActivity.this,
                            "安全のためHTTPSページだけを開けます。", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                browserUrlLabel.setText(url == null ? "" : url);
                browserStatus.setText("読み込み中…");
                captureButton.setEnabled(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                browserUrlLabel.setText(url == null ? "" : url);
                browserStatus.setText("表示中のページを確認してから鍵にしてください");
                captureButton.setEnabled(true);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) {
                    browserStatus.setText("ページを読み込めませんでした");
                    captureButton.setEnabled(false);
                }
            }

            @Override
            public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType,
                                          SafeBrowsingResponse callback) {
                callback.backToSafety(true);
                Toast.makeText(MainActivity.this,
                        "安全でない可能性のあるページをブロックしました。", Toast.LENGTH_LONG).show();
            }
        });
        return webView;
    }

    private void openBrowser(String rawUrl) {
        if (rawUrl == null) return;
        String value = rawUrl.trim();
        if (value.isEmpty()) return;
        if (!value.matches("(?i)^https://.*")) value = "https://" + value.replaceFirst("(?i)^http://", "");

        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                Toast.makeText(this, "HTTPSのURLを入力してください。", Toast.LENGTH_LONG).show();
                return;
            }
            browserContainer.setVisibility(View.VISIBLE);
            browserStatus.setText("読み込み中…");
            captureButton.setEnabled(false);
            browserWebView.loadUrl(uri.toString());
        } catch (Exception e) {
            Toast.makeText(this, "URLの形式を確認してください。", Toast.LENGTH_LONG).show();
        }
    }

    private void closeBrowser() {
        browserWebView.stopLoading();
        browserContainer.setVisibility(View.GONE);
    }

    private void captureCurrentPage() {
        String current = browserWebView.getUrl();
        if (current == null || !current.startsWith("https://")) {
            Toast.makeText(this, "HTTPSページを表示してから鍵にしてください。", Toast.LENGTH_LONG).show();
            return;
        }

        captureButton.setEnabled(false);
        browserStatus.setText("ページを端末内で鍵化しています…");

        final String script = "(function(){" +
                "try{" +
                "const clone=document.documentElement.cloneNode(true);" +
                "clone.querySelectorAll('script,style,noscript').forEach(n=>n.remove());" +
                "const title=(document.querySelector('title')?.textContent||'').trim();" +
                "const bodyText=clone.textContent||'';" +
                "return (title+'\\n'+bodyText).replace(/\\s+/g,' ').trim().slice(0,200000);" +
                "}catch(e){return '';}})();";

        browserWebView.evaluateJavascript(script, value -> {
            try {
                String normalized = new JSONArray("[" + value + "]").getString(0);
                if (normalized == null || normalized.trim().isEmpty()) {
                    browserStatus.setText("読み取れる本文がありません");
                    captureButton.setEnabled(true);
                    Toast.makeText(this,
                            "ページ本文を読み取れませんでした。表示が完了しているか確認してください。",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
                String fingerprint = Base64.encodeToString(
                        hash, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);

                browserContainer.setVisibility(View.GONE);
                String js = "window.setAndroidPageFingerprint && window.setAndroidPageFingerprint("
                        + org.json.JSONObject.quote(fingerprint) + ")";
                appWebView.evaluateJavascript(js, result -> {
                    Toast.makeText(this, "WEB PAGE CAPTURED", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                browserStatus.setText("ページ鍵の生成に失敗しました");
                captureButton.setEnabled(true);
                Toast.makeText(this, "ページ鍵の生成に失敗しました。", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pickTextFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "application/octet-stream"});
        try {
            startActivityForResult(intent, REQUEST_PICK_TXT);
        } catch (Exception e) {
            Toast.makeText(this, "TXTファイル選択画面を開けませんでした。", Toast.LENGTH_LONG).show();
        }
    }

    private void saveTextFile(String requestedName) {
        String safeName = requestedName == null ? "ephemeral-cypher.txt"
                : requestedName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!safeName.toLowerCase(Locale.ROOT).endsWith(".txt")) safeName += ".txt";
        pendingSaveFileName = safeName;

        String script = "(function(){var e=document.getElementById('resultOutput');return e?e.value:'';})()";
        appWebView.evaluateJavascript(script, value -> {
            try {
                JSONArray arr = new JSONArray("[" + value + "]");
                String text = arr.isNull(0) ? "" : arr.getString(0);
                if (text.isEmpty()) {
                    Toast.makeText(this, "保存するTXT内容がありません。", Toast.LENGTH_SHORT).show();
                    return;
                }
                pendingTextToSave = text;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, pendingSaveFileName);
                startActivityForResult(intent, REQUEST_SAVE_TXT);
            } catch (Exception e) {
                pendingTextToSave = null;
                pendingSaveFileName = null;
                Toast.makeText(this, "TXT保存を開始できませんでした。", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_TXT) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
            Uri uri = data.getData();
            try (InputStream in = getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                if (in == null) throw new IOException("Could not open selected file");
                byte[] buffer = new byte[8192];
                int read;
                int total = 0;
                final int maxBytes = 4 * 1024 * 1024;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > maxBytes) throw new IOException("TXT file is too large");
                    out.write(buffer, 0, read);
                }
                String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
                pendingLoadedText = text;
                flushPendingLoadedText(0);
            } catch (Exception e) {
                Toast.makeText(this, "TXTを読み込めませんでした。UTF-8のテキストファイルを選択してください。", Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (requestCode == REQUEST_SAVE_TXT) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) {
                pendingTextToSave = null;
                pendingSaveFileName = null;
                return;
            }
            Uri uri = data.getData();
            String text = pendingTextToSave;
            pendingTextToSave = null;
            pendingSaveFileName = null;
            if (text == null) return;
            try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException("Could not open destination");
                out.write((text + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
                appWebView.evaluateJavascript(
                        "window.androidTextSaved && window.androidTextSaved()", null);
                Toast.makeText(this, "TXTを保存しました。", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "TXTを保存できませんでした。", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 保留中のTXT内容をWebViewへ配送する。
     * setAndroidTextContent が定義済みで実行できた場合のみ "ok" が返り、成功トーストを出す。
     * 未定義(ページ読込前)なら短い間隔でリトライし、onPageFinishedからも再試行される。
     */
    private void flushPendingLoadedText(int attempt) {
        final String text = pendingLoadedText;
        if (text == null || appWebView == null) return;
        String js = "(function(){"
                + "if (window.setAndroidTextContent) {"
                + "  window.setAndroidTextContent(" + org.json.JSONObject.quote(text) + ");"
                + "  return 'ok';"
                + "}"
                + "return 'retry';"
                + "})()";
        appWebView.evaluateJavascript(js, value -> {
            if ("\"ok\"".equals(value)) {
                if (pendingLoadedText != null) {
                    pendingLoadedText = null;
                    Toast.makeText(this, "TXTを読み込みました。", Toast.LENGTH_SHORT).show();
                }
            } else if (attempt < 20) {
                appWebView.postDelayed(() -> flushPendingLoadedText(attempt + 1), 250);
            } else {
                pendingLoadedText = null;
                Toast.makeText(this,
                        "TXTを画面へ反映できませんでした。もう一度お試しください。",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleIntent(Intent intent) {
        String sharedUrl = extractSharedHttpsUrl(intent);
        if (sharedUrl != null) openBrowser(sharedUrl);
    }

    private String extractSharedHttpsUrl(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return null;
        if (intent.getType() == null || !intent.getType().startsWith("text/")) return null;
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null) return null;
        Matcher matcher = URL_PATTERN.matcher(text);
        if (!matcher.find()) return null;
        String raw = matcher.group();
        try {
            URI uri = URI.create(raw);
            if (!"https".equalsIgnoreCase(uri.getScheme())) return null;
            return uri.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void loadApp(String fingerprint) {
        String url = APP_ORIGIN + "index.html";
        if (fingerprint != null && !fingerprint.isEmpty()) url += "#ecpage=" + fingerprint;
        appWebView.loadUrl(url);
    }

    private WebResourceResponse assetResponse(String path) {
        String clean = path == null || "/".equals(path) ? "index.html" : path.replaceFirst("^/", "");
        if (clean.contains("..")) return null;
        try {
            InputStream stream = getAssets().open(clean);
            String ext = MimeTypeMap.getFileExtensionFromUrl(clean).toLowerCase(Locale.ROOT);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (mime == null) {
                if ("webmanifest".equals(ext) || "json".equals(ext)) mime = "application/json";
                else if ("svg".equals(ext)) mime = "image/svg+xml";
                else mime = "application/octet-stream";
            }
            return new WebResourceResponse(mime, "UTF-8", stream);
        } catch (IOException e) {
            return null;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
