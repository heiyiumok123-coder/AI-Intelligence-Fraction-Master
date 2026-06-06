# 🍎 AI Intelligence Fraction Master (iOS 26 Liquid Glass Edition)

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![UI Style](https://img.shields.io/badge/UI-Liquid%20Glass-pink.svg)

一款結合了 **Google ML Kit OCR 辨識**、**自動化分數詳解** 與 **iOS 26 Liquid Glass 極致視覺** 的高強度安全加固分數練習工具。

---

## ✨ 核心特色 (Core Features)

### 💎 1. iOS 26 "Liquid Glass" 視覺語言
- **液態玻璃 UI**: 全面採用 Apple Intelligence 視覺規範，卡片具備厚實的毛玻璃質感、流體折射率以及極致絲滑的連續曲率圓角 (Squircle)。
- **動態光譜背景**: 背景由粉、青、紫三色構成動態漸變，並透過高斯模糊營造出深度的空氣感。
- **霓虹呼吸燈效果**: 按鈕與容器邊緣具備微光折射高光，模擬系統運算的生命感。

### 📸 2. AI 拍照辨識 (OCR)
- **即時掃描**: 整合 `CameraX` 與 `Google ML Kit Text Recognition`，精準辨識手寫或印刷的分數算式。
- **一鍵運算**: 自動解析辨識出的分子、分母與運算子，直接給出答案。

### 🧮 3. 分數運算與「直接最簡」
- **完整詳解**: 提供加、減、乘、除、比較大小的詳細步驟，包含通分、約分過程。
- **快速化簡**: 專屬「直接最簡」按鈕，輸入大分數後秒出最簡化結果與 GCD 約分過程。
- **刷題模式**: 支援自定義題目範圍（最大分子/分母）與題數，具備即時 ✅/❌ 反饋。

### 🛡️ 4. 管理員雲端控制系統 (Admin Cloud)
- **雲端同步**: App 硬編碼連結至專屬 Pastebin JSON 後端，確保即便清除數據，更新指令依然有效。
- **強制更新**: 管理員可透過修改 JSON 遠端鎖死 App，強制使用者前往下載新版本。
- **環境加固**: 內建防偵錯 (Anti-Debug)、防模擬器檢測，並通過 R8 激進混淆保護核心 AI 邏輯。

---

## 🛠️ 技術棧 (Tech Stack)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Modern Material 3)
- **AI Engine**: Google ML Kit (Text Recognition)
- **Camera**: Android CameraX
- **Persistence**: SharedPreferences + Remote JSON
- **Build System**: Gradle 9.2.1 + R8 Security Pro

---

## 📦 如何安裝 (Installation)
1. 前往 GitHub 的 `Releases` 頁面。
2. 下載最新的 `app-secured.apk`。
3. 在 Android 設備（推薦 SDK 34+，如 POCO F7）上安裝並授予相機權限。

---

## 🔑 管理員權限
- **後端登入**: 點擊主畫面右下角「微透明齒輪」圖示。
- **預設密碼**: `398666`
- **雲端源**: `https://pastebin.com/raw/BqMQbvcK`

---

## 📜 授權協議 (License)
本專案採用 **MIT License**。您可以自由使用、修改並分發此程式碼，但請保留原作者標識。

---

> *"The future of math learning, rendered in liquid glass."*
