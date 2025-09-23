# EphemeralCypher

自然言語で鍵を共有できるインストール型暗号アプリ（PWA）

![GitHub top language](https://img.shields.io/github/languages/top/Masato-Nasu/Ephemeral-Cypher)
![GitHub last commit](https://img.shields.io/github/last-commit/Masato-Nasu/Ephemeral-Cypher)
![GitHub](https://img.shields.io/github/license/Masato-Nasu/Ephemeral-Cypher)

## 🚀 デモ
👉 [EphemeralCypher を使ってみる](https://masato-nasu.github.io/Ephemeral-Cypher/)

## 📸 スクリーンショット
![EphemeralCypher Screenshot](./screenshot.png)

## 🔑 特徴
- **HTMLだけ／JPEGだけ／両方**で鍵を生成できる
- **AES-GCM**（認証付き対抗モード）で暗号化／復号
- **PWA対応**でインストールしてオフライン利用も可能
- **ワンクリックで全クリア**（URL／HTMLファイル／画像／パスフレーズ／メッセージ）

## 🌫 コンセプト
鍵は残らず、暗号文も一時的に消えていく。  
**「儚さこそが強度になる」** ─ EphemeralCypher が提示する新しい暗号体験。

EphemeralCypher は、「鍵の共有」を自然なコミュニケーションで実現する、インストール可能な暗号アプリ（PWA）です。

🔑 鍵素材の柔軟さ

HTMLだけ を鍵にできる

JPEGだけ を鍵にできる

HTML＋JPEGの組み合わせ も可能

URL や画像ファイルを伝えるだけで、これまで難しかった鍵共有が自然言語で完結します。

⚙️ 技術と仕組み

AES-GCM で暗号化／復号（ブラウザ組込みの Web Crypto API 使用）

salt + IV + passphrase に対応し、強度を確保

画像は「そのままのバイト列」か「pHash（知覚ハッシュ）」のどちらかで利用可能

厳格モード（生バイト）: 一致すれば最も強固

寛容モード（pHash）: 近似画像にも対応可能だが耐性は相対的に低下

📱 アプリとしての特徴

PWA対応
スマホやPCにインストールしてアプリのように利用可能

オフラインでも利用可
ローカルHTMLや画像ファイル入力に対応

ワンクリックで全リセット
「クリア」ボタンでテキスト／URL／HTMLファイル／画像ファイル／パスフレーズをすべて初期化

📲 インストール方法（PWA）

EphemeralCypher は Progressive Web App (PWA) として提供されています。
スマートフォンやPCのブラウザから簡単に アプリのようにインストール できます。

スマートフォン（Android / iPhone）

Chrome（Android）または Safari（iPhone）でアプリのURLを開きます。

画面下または右上のメニューから 「ホーム画面に追加」 を選びます。

Android: ⋮ メニュー → ホーム画面に追加

iPhone: 共有アイコン → ホーム画面に追加

ホーム画面にアイコンが追加され、以降は ネイティブアプリのように起動可能 です。

パソコン（Chrome / Edge）

Chrome または Edge でアプリのURLを開きます。

アドレスバーの右端に表示される インストール（＋）アイコン をクリックします。

もし表示されない場合は、右上メニューから「アプリをインストール」を選択してください。

デスクトップやアプリ一覧から、通常のアプリのように起動できます。

⚠️ 注意点

インストールは端末ごとに必要です。

キャッシュが残る場合があります。動作が不安定なときは ?reset=1 をURL末尾に付けてリロードしてください。

鍵源に指定したHTMLやJPEGファイルは端末ごとに用意する必要があります。

🌫 コンセプト

EphemeralCypher のキーワードは 「儚さ」。
鍵は残らず、暗号文も一時的に消えていく。

儚さこそが強度になる

現代の情報環境にふさわしい、新しい暗号体験を提示します。
