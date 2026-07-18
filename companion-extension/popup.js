"use strict";

// Change this only if you host Ephemeral Cypher at a different public URL.
const APP_URL = "https://masato-nasu.github.io/Ephemeral-Cypher/";

const button = document.getElementById("usePage");
const status = document.getElementById("status");

function setStatus(message, kind = "") {
  status.textContent = message;
  status.className = "status" + (kind ? " " + kind : "");
}

function capturePageFingerprint() {
  return (async () => {
    const clone = document.documentElement.cloneNode(true);
    clone.querySelectorAll("script,style,noscript").forEach(node => node.remove());
    const title = (document.querySelector("title")?.textContent || "").trim();
    const bodyText = clone.textContent || "";
    const normalized = (title + "\n" + bodyText).replace(/\s+/g, " ").trim().slice(0, 200000);
    if (!normalized) throw new Error("This page has no readable text.");

    const digest = new Uint8Array(
      await crypto.subtle.digest("SHA-256", new TextEncoder().encode(normalized))
    );
    let binary = "";
    for (let i = 0; i < digest.length; i++) binary += String.fromCharCode(digest[i]);
    const fingerprint = btoa(binary)
      .replace(/\+/g, "-")
      .replace(/\//g, "_")
      .replace(/=+$/g, "");

    return { fingerprint, chars: normalized.length };
  })();
}

function unsupportedUrl(url = "") {
  return /^(chrome|edge|about|brave|opera|vivaldi|devtools|chrome-extension):/i.test(url);
}

button.addEventListener("click", async () => {
  button.disabled = true;
  setStatus("ページ指紋を端末内で生成しています…");

  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!tab?.id) throw new Error("Active tab not found.");
    if (unsupportedUrl(tab.url || "")) throw new Error("この種類のページはブラウザ仕様により読み取れません。");

    const results = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: capturePageFingerprint
    });
    const result = results?.[0]?.result;
    if (!result?.fingerprint) throw new Error("ページ指紋を生成できませんでした。");

    const appUrl = new URL(APP_URL);
    appUrl.hash = new URLSearchParams({ ecpage: result.fingerprint }).toString();

    setStatus(`指紋を生成しました（${result.chars.toLocaleString()}文字）。Ephemeral Cypherを開きます。`, "good");
    await chrome.tabs.create({ url: appUrl.toString() });
    window.close();
  } catch (error) {
    console.error(error);
    setStatus(error?.message || "このページを読み取れませんでした。", "error");
    button.disabled = false;
  }
});
