# Ephemeral Cypher Companion

Chrome / Chromium 系ブラウザ用の Manifest V3 拡張です。

## 役割

現在開いているページの DOM をその場で読み取り、`script` / `style` / `noscript` を除外し、タイトルとテキストを正規化して SHA-256 指紋を生成します。

Ephemeral Cypher へ渡すのは **32バイトのページ指紋だけ**です。ページ URL と本文は Ephemeral Cypher のサーバーへ送信しません。指紋は URL のフラグメント（`#ecpage=...`）で渡すため、通常の HTTP リクエストにも含まれません。

権限は次の2つだけです。

- `activeTab`: ユーザーが拡張を開いた現在タブへの一時アクセス
- `scripting`: そのタブ内で指紋生成コードを実行

`<all_urls>` の常時アクセス権は使用しません。

## インストール

1. Chromeで `chrome://extensions/` を開く
2. 右上の「デベロッパー モード」をON
3. 「パッケージ化されていない拡張機能を読み込む」
4. この `companion-extension` フォルダを選択
5. ツールバーに Ephemeral Cypher Companion を固定

## 使い方

1. 鍵にしたいWebページを開く
2. Companionを開く
3. 「このページを鍵にする」を押す
4. Ephemeral Cypherが開き、`WEB PAGE CAPTURED` になれば準備完了
5. 暗号化または復号する

同じページでも、表示内容・ログイン状態・動的コンテンツ・A/Bテストなどが変化すると指紋が変わる場合があります。これはEphemeral Cypherの可死性の一部です。

## ホスト先を変更する場合

`popup.js` の `APP_URL` を変更してください。
