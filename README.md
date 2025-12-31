# EphemeralCypher / エフェメラルサイファー

A minimal, client‑side cipher tool that derives a key from **Website/HTML** and/or an **Image**, then encrypts/decrypts your message.  
(Works as a PWA and can be installed.)

👉 Live: https://masato-nasu.github.io/Ephemeral-Cypher/

---

## Screenshot

![EphemeralCypher UI](./screenshot.png)

---

## Screen guide

- **Site URL**: enter a page URL to use as key material (optional)
- **HTML file**: choose a local HTML file to use as key material (optional)
- **Image key**: choose an image file to use as key material (optional)
- **Message**: plaintext (for Encrypt) or ciphertext (for Decrypt)
- **Result**: output area
- **Buttons**: Encrypt / Decrypt / Copy Result / Clear
- **Language**: 日本語 / English toggle (top‑right)

> Provide **at least one** key material: (URL/HTML) and/or Image.

---

## Quick start

1. Provide **at least one** key material:
   - **Website URL** *or* **HTML file**
   - **Image**
2. Type/paste your **Message**
3. Click **Encrypt** or **Decrypt**
4. Use **Copy Result** if needed

Encryption/decryption runs in your browser (client‑side).  
When you use a Website URL as a key material, the app fetches the page content to derive the key.

---

## Key materials

### Website URL / HTML file
Use **either** a URL **or** an HTML file.  
If a website blocks page fetching, the URL option may not work for that site. In that case, use the HTML file option.

### Image
Select an image file as a key material.

---

## Install (PWA)

- **Chrome / Edge (Desktop & Android)**: open the Live URL → install icon in the address bar (or menu → Install).
- **iPhone Safari**: Share → **Add to Home Screen**.

---

## Local run

Static files only (no build step). Serve the folder, e.g.:

```bash
python -m http.server 8000
```

Open: http://localhost:8000/

---

## License

MIT

---

<details>
<summary>日本語</summary>

## 画面の説明

- **サイトURL**：鍵素材として使うページURL（任意）
- **HTMLファイル**：鍵素材として使うローカルHTML（任意）
- **画像鍵**：鍵素材として使う画像（任意）
- **メッセージ**：暗号化したい文章／復号したい暗号文
- **結果**：出力欄
- **ボタン**：暗号化／復号／結果をコピー／クリア
- **言語**：右上で切替（日本語 / English）

※ 鍵素材は **最低1つ** 必要です（URL/HTML または 画像、または両方）。

## 使い方（最短）

1. 鍵素材を **最低1つ** 用意します  
   - **サイトURL** または **HTMLファイル**  
   - **画像**
2. メッセージを入力／貼り付け
3. **暗号化** または **復号** を押す
4. 必要なら **結果をコピー**

暗号化／復号はブラウザ内（クライアント側）で行われます。  
URLを鍵素材に使う場合は、ページ内容を取得して鍵を導出します。

</details>
