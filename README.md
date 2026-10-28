# 🏗️ Stack Drop

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-red.svg)](https://m3.material.io)

**Stack Drop** is a physics-inspired, fast-paced tower building arcade game for Android built with 100% Kotlin and Jetpack Compose. 

Drop moving blocks with perfect timing, trim off overhangs, and stack your way to the top!

---

## ✨ Features

- 🎯 **Precision Drop Physics**: Tap to drop moving blocks. Overhanging pieces chop off and fall as realistic fragments!
- 🔥 **Perfect Drops & Combo Streaks**: Align blocks perfectly with the base to gain streak bonuses and restore block width.
- 📐 **Dynamic Camera Tracking**: Smoothly follows your tower upwards as you reach new height records.
- ⚡ **3 Difficulty Levels**:
  - **Easy**: Slower block speed, wider blocks, forgiving alignment tolerances.
  - **Medium**: Balanced speed and challenge for everyday play.
  - **Hard**: Fast-moving blocks, slim width, and tight precision requirements.
- 🎨 **Material 3 Themes**: Full support for Light Theme, Dark Theme, and System Default.
- 🔊 **Procedural Sound & Haptics**: Rich audio effects using `SoundPool` and tactile feedback for every drop, perfect hit, and game over.
- 📊 **Statistics & High Scores**: Local persistence tracking total games played, highest stack level, top score, total blocks dropped, and best perfect streak.
- ♿ **Accessibility Ready**: Built-in Reduced Motion support for smoother visuals on all devices.
- 📴 **100% Offline Play**: No internet connection required.

---

## 🏗️ Architecture & Tech Stack

Stack Drop is designed following modern Android architecture guidelines:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **State Management**: `StateFlow` and Coroutines
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Data Persistence**: `SharedPreferences` repository with Kotlin `Flow`
- **Audio & Haptics**: Custom `SoundPlayer` (`SoundPool`) & `HapticManager` (`Vibrator`)
- **Graphics & Animation**: Custom Compose Canvas drawing with frame-rate independent physics loops (`withFrameNanos`)

---

## 📁 Project Structure

```
com.example.stackdrop/
├── data/              # GamePreferencesRepository (SharedPreferences & Flow)
├── model/             # Data models (Difficulty, GameState, TowerBlock, MovingBlock, etc.)
├── ui/
│   ├── components/    # GameCanvas and reusable composables
│   ├── navigation/    # AppNavigation and Screen routes
│   ├── screens/       # HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen, SplashScreen
│   └── theme/         # Color, Type, Theme definitions
├── util/              # GamePhysics engine, HapticManager, SoundPlayer
└── viewmodel/         # StackDropViewModel
```

---

## 🛠️ Requirements & Installation

### Requirements
- **Android Studio**: Ladybug / 2024.2.1 or newer recommended
- **Minimum SDK**: Android 8.0 (API Level 26)
- **Target SDK**: Android 15 / 16 (API Level 37)
- **JDK**: Java 11 or higher

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Stack-Drop.git
   cd Stack-Drop
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and build the project.
4. Run on an Android Emulator or connected physical device:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Testing

Run unit tests for game logic and physics calculations:
```bash
./gradlew test
```

Unit test coverage includes:
- Block alignment & overhang calculations (`GamePhysicsTest`)
- Perfect drop tolerance detection and streak tracking (`GameLogicTest`)
- Score and multiplier scaling

---

## 👨‍💻 Author

Developed by **[Abdullah Ateeq](https://github.com/abdullah-ateeq)**.

---

## 📄 License

This project is licensed under the MIT License.
