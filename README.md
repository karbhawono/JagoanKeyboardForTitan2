# Jagoan Keyboard for Titan 2

A modern, feature-rich Input Method Editor (IME) keyboard application specifically designed for the Unihertz Titan 2 physical QWERTY keyboard. Built with cutting-edge Android technologies and inspired by the iconic BlackBerry Passport design.

## 🎯 Project Overview

Jagoan Keyboard for Titan 2 is a native Android keyboard application that enhances the typing experience on devices with physical QWERTY keyboards. The app features an intelligent symbol picker that eliminates redundant symbols already available on the physical keyboard, customizable typing behaviors, and a sleek dark theme.

**Key Highlights:**

- 🎨 BlackBerry Passport-inspired dark UI with 3D button effects
- ⌨️ Optimized for Unihertz Titan 2's 48-key physical keyboard
- 🚀 Built with modern Android architecture (Clean Architecture + MVVM)
- 🎭 Symbol picker with intelligent layout (19 non-redundant punctuation symbols)
- 🇮🇩 Default settings optimized for Indonesian users
- 🌙 Pure black theme

## ✨ Features

### Smart Symbol Picker

The symbol picker has been redesigned from the ground up to eliminate redundancy:

**Punctuation Category (19 symbols in 2 rows):**

- **Row 1 (Q-P):** `;` `[` `]` `{` `}` `<` `>` `|` `\` `&`
- **Row 2 (A-L):** `^` `%` `~` `` ` `` `...` `Rp` `°` `§` `•`

**Why only 2 rows?** The physical keyboard already provides quick access to common symbols via Alt modifier:

- Numbers (0-9), basic punctuation (`!` `?` `.` `,` `:` `'` `"` `-` `_`)
- Math operators (`+` `*` `/`), grouping (`(` `)`)
- Special characters (`@` `#`)

**Result:** Cleaner interface, faster symbol access, no visual clutter.

### Physical Keyboard Integration

The Titan 2's Alt layer provides instant access to symbols:

```
Row 1: Q=0  W=1  E=2  R=3  T=(
  Y=)  U=-  I=_  O=/  P=:
Row 2: A=@  S=4  D=5  F=6  G=*  H=#  J=+  K="  L='
Row 3: Z=!  X=7  C=8  V=9  B=.  N=,  M=?
```

### Customizable Behavior

- **Auto-Capitalization** - Smart first letter capitalization (default: OFF)
- **Long-Press Capitalize** - Hold Shift for CAPS LOCK (default: ON)
- **Key Repeat** - Auto-repeat for held keys (default: OFF)
- **Sticky Modifiers** - Shift/Alt stay active for next key (default: ON)
- **Double-Space Period** - Quick sentence ending (default: ON)
- **Text Shortcuts** - User-defined word substitutions (default: ON)
- **Alt+Backspace Delete Line** - Fast line deletion (default: ON)
- **Preferred Currency** - Customizable currency symbol (default: Rp)
- **Accent Support** - Long-press for letter accents (default: OFF, English)

### BlackBerry-Inspired Design

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ #000000 Background (Pure Black)       ┃
┃                                       ┃
┃           Punctuation                 ┃
┃   Press physical key to insert...    ┃
┃                                       ┃
┃ ╔══╦══╦══╦══╦══╦══╦══╦══╦══╦══╗     ┃
┃ ║; ║[ ║] ║{ ║} ║< ║> ║| ║\ ║& ║     ┃
┃ ║Q ║W ║E ║R ║T ║Y ║U ║I ║O ║P ║     ┃
┃ ╚══╩══╩══╩══╩══╩══╩══╩══╩══╩══╝     ┃
┃                                       ┃
┃  ╔══╦══╦══╦══╦══╦══╦══╦══╦══╗       ┃
┃  ║^ ║% ║~ ║` ║..║Rp║° ║§ ║• ║       ┃
┃  ║A ║S ║D ║F ║G ║H ║J ║K ║L ║       ┃
┃  ╚══╩══╩══╩══╩══╩══╩══╩══╩══╝       ┃
┃                                       ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

**Design Features:**

- 🖤 Pure black background (#000000) for OLED efficiency
- ⬜ Black buttons with light gray borders (#4A4A4A)
- 📐 3D depth from 1dp border + 4dp shadow elevation
- 💡 Light gray text (#E0E0E0) for optimal readability
- 🎨 Consistent dark theme across all screens

## 🏗️ Architecture & Technology Stack

### Architecture Pattern

- **Clean Architecture** - Clear separation of concerns (UI → Domain → Data)
- **MVVM** - Model-View-ViewModel with unidirectional data flow
- **Repository Pattern** - Abstract data source access

### Technology Stack

**Core:**

- Kotlin 100% - Modern, null-safe language
- Android 15 (API 35) - Latest Android SDK
- Jetpack Compose - Declarative UI framework
- Material Design 3 - Modern design system

**Libraries:**

- **Hilt** - Compile-time dependency injection
- **Kotlin Coroutines** - Structured concurrency
- **Flow & StateFlow** - Reactive streams
- **DataStore** - Type-safe preferences storage
- **Kotlin Serialization** - JSON serialization

**Testing:**

- **JUnit 5** - Modern testing framework
- **MockK** - Kotlin-friendly mocking
- **Turbine** - Flow testing library
- **Truth** - Fluent assertions
- **Coroutines Test** - Testing utilities

**Build Tools:**

- Gradle 8.4 with Kotlin DSL
- Version Catalogs - Centralized dependency management
- KSP - Kotlin Symbol Processing
- R8 - Code shrinking and obfuscation

## 📋 Requirements

### Runtime Requirements

- **Device:** Unihertz Titan 2 (or compatible physical keyboard device)
- **Android:** 14+ (API 34 minimum, targeting API 35)
- **Storage:** ~25 MB for installed app

### Development Requirements

- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** 17 (Java Development Kit)
- **Android SDK:** API 35 (Android 15)
- **Gradle:** 8.4+ (included in wrapper)
- **Git:** For version control

## 🚀 Compilation & Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/karbhawono/JagoanKeyboardforTitan2.git
cd JagoanKeyboardforTitan2
```

### Step 2: Set Up Required Properties Files

#### 2.1 Create `local.properties`

This file contains the Android SDK location. Create it in the project root:

```properties
# local.properties
sdk.dir=/path/to/your/Android/Sdk
```

**On macOS/Linux:**

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

**On Windows:**

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

#### 2.2 Create `version.properties`

This file tracks the auto-incrementing build number:

```properties
# version.properties
#Auto-incremented build number
versionCode=1
```

The build number will auto-increment with each build.

#### 2.3 Create `keystore.properties` (Optional - for Release Builds)

For **debug builds**, you can skip this step. For **release builds** with signing, create this file:

```properties
# keystore.properties
storeFile=/path/to/your/keystore.jks
storePassword=your_keystore_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

**To generate a keystore:**

```bash
keytool -genkey -v -keystore release.keystore -alias my_alias \
  -keyalg RSA -keysize 2048 -validity 365000
```

Then update `keystore.properties`:

```properties
storeFile=release.keystore
storePassword=your_password_here
keyAlias=my_alias
keyPassword=your_password_here
```

**⚠️ IMPORTANT:** Never commit `keystore.properties` or keystore files to version control!

### Step 3: Build the Project

#### Debug Build (No Keystore Required)

```bash
# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/jagoan-keyboard-titan2-debug.apk
```

#### Release Build (Requires Keystore)

```bash
# Build release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/jagoan-keyboard-titan2-release.apk
```

#### Clean Build

```bash
# Clean previous builds
./gradlew clean

# Clean and build
./gradlew clean assembleDebug
```

### Step 4: Install on Device

#### Via Android Studio

1. Open project in Android Studio
2. Connect Titan 2 via USB
3. Click **Run** (Shift+F10) or use the green play button

#### Via ADB Command Line

```bash
# Install debug build
adb install -r app/build/outputs/apk/debug/jagoan-keyboard-titan2-debug.apk

# Install release build
adb install -r app/build/outputs/apk/release/jagoan-keyboard-titan2-release.apk

# The -r flag allows reinstallation while keeping data
```

#### Manual Installation

1. Copy APK to device
2. Open file manager on device
3. Tap APK file
4. Follow installation prompts

### Step 5: Enable the Keyboard

1. Go to **Settings → System → Languages & input → Virtual keyboard**
2. Tap **Manage keyboards**
3. Enable **Jagoan Keyboard for Titan 2**
4. Open any text field
5. Tap the keyboard icon in navigation bar
6. Select **Jagoan Keyboard for Titan 2**

### Step 6: Configure Settings (Optional)

1. Open the **Jagoan Keyboard** app from launcher
2. Review default settings (optimized for Indonesian Titan 2 users)
3. Adjust as needed:
    - Auto-capitalization
    - Key repeat behavior
    - Sticky modifiers
    - Preferred currency symbol
    - Text shortcuts

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "SettingsRepositoryImplTest"
```

### Run with Coverage

```bash
./gradlew testDebugUnitTest
# Reports: app/build/reports/tests/testDebugUnitTest/index.html
```

### Lint Checks

```bash
./gradlew lint
# Reports: app/build/reports/lint-results.html
```

## 🛠️ Development

### Build Variants

- **debug** - Development build with debugging enabled, package suffix `.debug`
- **release** - Production build with R8 optimization and code shrinking

### Gradle Tasks

```bash
# List all tasks
./gradlew tasks

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug on device
./gradlew installDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Check for errors
./gradlew check

# Clean build files
./gradlew clean
```

### Auto-Incrementing Build Numbers

The build number automatically increments with each build. This is managed by:

- `version.properties` - Stores current build number
- `incrementBuildNumber` Gradle task - Runs before each build

Current version format: `versionName = "0.3.0"` + `versionCode = <auto-increment>`

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Prefer immutability (val over var)

## 📱 Usage Guide

### Basic Usage

1. **Symbol Picker** - Press SYM key to open symbol picker
2. **Category Shortcuts** - Press SYM+P/C/M/A/E/O for direct category access
3. **Category Cycling** - Press SYM repeatedly to cycle through categories
4. **Insert Symbol** - Press Q-P or A-L to insert corresponding symbol
5. **Quick Currency** - Double-tap SYM for preferred currency symbol (e.g., Rp)
6. **Auto-Dismiss** - Symbol picker closes after inserting a symbol

### Symbol Categories

Access directly with shortcuts or cycle through with SYM key:

- **Punctuation (SYM+P)** - `;` `[` `]` `{` `}` `<` `>` `|` `\` `&` `^` `%` `~` etc.
- **Currency (SYM+C)** - Rp $ € £ ¥ ₹ ₩ ₽ ₪ ₫ ฿ etc.
- **Math (SYM+M)** - ± × ÷ ≠ ≈ ≤ ≥ ∞ √ ∑ ∫ π etc.
- **Arrows (SYM+A)** - ← → ↑ ↓ ↔ ⇐ ⇒ ⇑ ⇓ ⇔ etc.
- **Emoji (SYM+E)** - 😀 😂 ❤️ 👍 🎉 ✅ ⚠️ 🔥 etc.
- **Other (SYM+O)** - © ® ™ § ¶ † ‡ • ° etc.

### Physical Keyboard Shortcuts

- **Shift + Letter** - Capitalize single letter
- **Long-press Shift** - CAPS LOCK mode (if enabled)
- **Alt + Key** - Access symbol layer (see keyboard mapping)
- **Alt + Backspace** - Delete entire line (if enabled)
- **Double Space** - Auto-insert period (if enabled)
- **SYM+C+C+Country Code** - Insert currency by country code (e.g., SYM+C+C+ID → Rp)

## 🐛 Known Issues

- JVM warning about class sharing (non-critical, related to test execution)
- Gradle 9.0 deprecation warnings (will be addressed in future Gradle updates)

No functional issues reported in the current build.

## 📄 License

```
/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 */
```

This project incorporates multiple frameworks/libraries with different licensing:

### Current Status

**Copyright (c) 2025 Aryo Karbhawono**

### Third-Party Libraries

This project uses open-source libraries, each with their own licenses:

- Android Jetpack libraries (Apache 2.0)
- Kotlin Standard Library (Apache 2.0)
- Hilt (Apache 2.0)
- Material Design Components (Apache 2.0)
- JUnit 5 (Eclipse Public License 2.0)
- MockK (Apache 2.0)

See individual library documentation for complete license information.

## 🙏 Acknowledgments

- **Unihertz Titan 2** - For creating an excellent physical keyboard phone
- **BlackBerry Passport** - Design inspiration for symbol picker
- **Divefire** - Original titan2keyboard project foundation
- **Android Open Source Project** - For the robust Android platform
- **Kotlin Team** - For the amazing Kotlin language
- **Jetpack Compose Team** - For the modern UI framework
- **All Contributors** - For bug reports, feature requests, and testing

## 📞 Support & Contact

### Issues & Bug Reports

- Open an issue on GitHub with detailed reproduction steps
- Include device info, Android version, and app version
- Attach screenshots or logs if possible

### Feature Requests

- Check existing issues first to avoid duplicates
- Describe the feature and its use case
- Explain how it benefits Titan 2 users

### Questions

- Check documentation first (README, CHANGELOG)
- Search existing issues for similar questions
- Open a new issue with the "question" label

## 🤝 Contributing

Contributions are welcome! Please ensure:

1. **Code Quality**
    - Follow Kotlin coding conventions
    - Write meaningful commit messages
    - Add tests for new features
    - Update documentation

2. **Testing**
    - Test on actual Titan 2 device if possible
    - Ensure all tests pass (`./gradlew test`)
    - Check for lint errors (`./gradlew lint`)

3. **Documentation**
    - Update README if adding features
    - Add KDoc comments for public APIs
    - Update CHANGELOG with changes

4. **Commit Format**
    - Use conventional commits: `feat:`, `fix:`, `docs:`, `refactor:`, etc.
    - Keep commits focused and atomic
    - Reference issues when applicable

## 🔐 Security

### Reporting Security Issues

Opening a private security advisory on GitHub.

### Best Practices

- Never commit sensitive data (keystores, passwords, API keys)
- Keep dependencies up to date
- Use ProGuard/R8 for release builds
- Validate all user inputs

---

**Built with ❤️ for the Unihertz Titan 2 Community**

_Jagoan Keyboard - Making physical keyboard typing great again!_
