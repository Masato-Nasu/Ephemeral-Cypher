# Changelog

## v2.1.0

- URL直接取得方式を廃止し、CORSによる `UNREACHABLE` を解消
- Chrome / Chromium向け `Ephemeral Cypher Companion` を追加
- Companionは `activeTab` + `scripting` のみを使用し、`<all_urls>` を要求しない
- 現在タブ内でページ本文を正規化し、SHA-256ページ指紋だけをPWAへ渡す方式へ変更
- ページURLと本文を第三者中継やPWAサーバーへ送らない設計
- Companion指紋と保存HTMLのSHA-256を同じページ素材スロットとして扱い、EC2および旧EC1導出経路に対応
- URL入力欄と `UNREACHABLE` 状態をUIから削除
- TXT保存 / TXT読込機能を継続
- PWAキャッシュを v2.1.0 に更新

## v2.0.1

- 題名を端正なサンセリフ体へ変更
- 一節鍵を削除
- 暗号文のTXT保存とTXT読込を追加

## v2.0.0

- 第三者中継を削除
- EC2（HKDF-SHA-256 + AES-256-GCM）を追加
- EC1 / 無印の旧暗号文との復号互換を維持
