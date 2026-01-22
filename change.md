# Changelog

All notable changes to Jagoan Keyboard for Titan 2 will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.4.0] - 2025-01-22

### 🎉 Major Features Added

#### Intelligent Autocorrect System

**AutocorrectEngine** - Core autocorrect engine with multiple algorithms:

- Levenshtein edit distance calculation for typo detection (max 2 edits)
- Keyboard proximity analysis for QWERTY layout awareness
- Smart contractions support (e.g., "dont" → "don't", "cant" → "can't")
- Context-aware language detection
- Confidence scoring system (high >0.8, medium >0.5, low <0.5)
- Returns top 5 suggestions sorted by confidence
- File: `ai.jagoan.keyboard.titan2.engine.AutocorrectEngine`

**AutocorrectManager** - Lifecycle and state management:

- Asynchronous dictionary loading on initialization
- Real-time suggestion generation during typing
- Thread-safe operations with Kotlin Coroutines
- Configurable enable/disable functionality via settings
- Efficient background processing with SupervisorJob
- File: `ai.jagoan.keyboard.titan2.engine.AutocorrectManager`

**DictionaryRepository** - Multi-language dictionary support:

- English dictionary (en.txt) - comprehensive word list
- Indonesian dictionary (id.txt) - bahasa Indonesia support
- English contractions database (en_contractions.txt)
- Personal dictionary for user-added words
- Efficient in-memory caching with ConcurrentHashMap
- Language detection for unknown words
- Thread-safe concurrent access
- Asset-based dictionary loading
- File: `ai.jagoan.keyboard.titan2.data.repository.DictionaryRepositoryImpl`

**Settings Integration**:

- `autocorrectEnabled` - Master toggle for autocorrect (default: true)
- `autocorrectLanguages` - List of active languages (default: ["en", "id"])
- `showSuggestions` - Display suggestion bar UI (default: true, currently disabled)

#### Multi-Language Accent Support

**AccentRepository** - Comprehensive accent variants system:

- Support for 7 languages:
    - French (fr): é è ê ë à â ù û ü ï î ô ç œ æ
    - German (de): ä ö ü ß
    - Spanish (es): á é í ó ú ñ ü ¿ ¡
    - Portuguese (pt): á à â ã é ê í ó ô õ ú ç
    - Italian (it): à è é ì ò ù
    - Danish (da): æ ø å
    - Norwegian (no): æ ø å
- Long-press key to cycle through accent variants
- 150+ accent character mappings
- Configurable toggle in settings: "Long-press for Accents" (default: OFF)
- Visual cycling with debounced input
- File: `ai.jagoan.keyboard.titan2.data.AccentRepository`

**Settings Integration**:

- `selectedLanguage` - Primary language for accent support (default: "en")
- `longPressAccents` - Enable accent cycling on long-press (default: false)

#### Enhanced UI/UX

**Settings Screen Complete Redesign** - Material Design 3 overhaul:

- **QuickAccessToggles** - 2×2 grid for frequently used settings:
    - Sticky Shift (📌) - Keep Shift active for next key
    - Sticky Alt (⌥) - Keep Alt active for next key
    - Auto-capitalize (🔠) - Capitalize first letter of sentences
    - Long-press Accents (é) - Cycle accent variants on long-press
    - Visual active states with green border and filled background
- **SettingsDesignTokens** - Consistent design system:
    - Jagoan Green: #CDFF85 (primary accent)
    - Background Dark: #121212 (pure black for OLED)
    - Surface Dark: #1A1A1A (elevated surfaces)
    - On Surface: #FFFFFF (primary text)
    - On Surface Variant: #888888 (secondary text)
    - Danger Red: #FF6464 (destructive actions)
    - Spacing hierarchy: 8dp, 16dp, 24dp, 28dp
    - Corner radii: 12dp (medium), 16dp (large)
- **Visual Improvements**:
    - Emoji icons for all settings (🔠 ⌨️ 💬 💰 🌐 ⟲ 📌 ⌥ ⌫)
    - Grouped settings cards with proper dividers
    - Enhanced activation banner with streamlined flow
    - Improved button hierarchy (primary/secondary/danger states)
    - Section headers with icons and underline separators
    - Consistent 16dp padding and rounded corners
- File: `ai.jagoan.keyboard.titan2.ui.settings.SettingsScreen`

**SuggestionBar Component** - Real-time autocorrect suggestions:

- Horizontal scrollable bar for multiple suggestions
- Displays current word and top 5 suggestions
- Click to accept and replace suggestions
- Confidence indicators (visual priority)
- Auto-hide when symbol picker is active
- WindowManager overlay positioning above input field
- Jetpack Compose implementation for smooth rendering
- Files: `ai.jagoan.keyboard.titan2.ui.ime.SuggestionBar`, `SuggestionBarView`

**Licenses Screen** - Comprehensive open source attribution:

- Categorized library information:
    - **Build Tools**: Android Gradle Plugin, Kotlin, KSP
    - **Core Libraries**: Kotlin stdlib, Serialization
    - **AndroidX**: Core KTX, Lifecycle, DataStore
    - **Jetpack Compose**: UI, Material3, Runtime
    - **Dependency Injection**: Hilt Android, Hilt Compiler
    - **Coroutines**: Core, Android extensions, Test
    - **Testing**: JUnit 5, MockK, Turbine, Truth
- 30+ library entries with versions, purposes, licenses, and URLs
- Expandable cards with detailed information
- Proper Apache 2.0 and EPL 2.0 license attribution
- File: `ai.jagoan.keyboard.titan2.ui.settings.LicensesScreen`
- Data: `ai.jagoan.keyboard.titan2.data.LicenseDataProvider`

#### Performance Optimizations

**New Utility Classes**:

- **PerformanceMonitor**: Method execution time tracking
    - `measure()` function for inline performance logging
    - Millisecond precision timing
    - Automatic cleanup and result return
- **LazyLog**: Deferred string evaluation for efficient logging
    - Prevents expensive string operations when logging disabled
    - Lambda-based message construction
    - Zero overhead when log level filtered
- **Debouncer**: Event debouncing for UI operations
    - Configurable delay (default: 300ms)
    - Coroutine-based implementation
    - Prevents excessive API calls
- **ObjectPool**: Generic object pooling pattern
    - Reduces garbage collection pressure
    - Configurable pool size
    - Thread-safe acquire/release operations
- **LocaleUtils**: Currency and locale utilities
    - `getDefaultCurrencySymbol()` - Auto-detect currency from locale
    - Currency mapping for 20+ countries
    - Fallback to system defaults

- Files: `ai.jagoan.keyboard.titan2.util.*`

**IME Service Improvements**:

- Suggestion bar overlay with WindowManager positioning
- Capacitive keyboard blocking (1000ms cooldown after physical key press)
- Improved lifecycle management with ServiceLifecycleOwner
- Better state synchronization between symbol picker and suggestion bar
- Optimized view attachment/detachment logic
- Reduced memory allocations during typing
- File: `ai.jagoan.keyboard.titan2.ime.JagoanInputMethodService`

#### Testing Infrastructure

**Comprehensive Test Coverage**:

**SettingsRepositoryImplTest** (19 tests):

- DataStore initialization and default values
- Flow emission and state updates
- Individual preference updates (12 different settings)
- Autocorrect language list serialization/deserialization
- Thread-safe concurrent access
- Error handling and edge cases
- File: `ai.jagoan.keyboard.titan2.data.repository.SettingsRepositoryImplTest`

**SymbolPickerOverlayTest** (469 tests):

- **Physical Key Mapping Tests** (20 tests):
    - Row 1 (Q-P) maps to indices 0-9
    - Row 2 (A-L) maps to indices 0-8
    - Invalid keys return -1
    - Edge cases (shift, numbers, special keys)
- **Symbol Category Tests** (72 tests):
    - All 6 categories have exactly 19 symbols
    - Proper symbol distribution across categories
    - Punctuation, Currency, Math, Arrows, Emoji, Miscellaneous
- **Category Cycling Logic** (36 tests):
    - Linear progression through categories
    - Wrap-around from last to first
    - State persistence between cycles
- **Currency Quick Access** (24 tests):
    - Double-tap SYM key for quick currency insert
    - Timing validation (<500ms threshold)
    - State reset after timeout
- **Integration Tests** (317 tests):
    - End-to-end symbol selection workflows
    - Physical keyboard to symbol mapping
    - Real symbol data validation
    - Category consistency checks

- Technologies: JUnit 5, MockK, Truth assertions, Turbine for Flow testing
- File: `ai.jagoan.keyboard.titan2.ui.ime.SymbolPickerOverlayTest`

**Test Infrastructure**:

- JUnit 5 (Jupiter) test platform
- MockK for Kotlin-friendly mocking
- Turbine for Flow and coroutine testing
- Truth for fluent assertions
- Coroutines Test utilities
- Jacoco code coverage reporting
- 488+ total test cases across all test files

#### Architecture Enhancements

**New Domain Models**:

- **AutocorrectSuggestion**: Typed suggestion model
    - `word: String` - The suggested word
    - `confidence: Float` - Confidence score (0.0-1.0)
    - `metadata: SuggestionMetadata` - Additional context
- **SuggestionMetadata**: Suggestion context
    - `source: SuggestionSource` - Origin of suggestion
    - `language: String?` - Detected language
    - `editDistance: Int` - Levenshtein distance from input
- **SuggestionSource**: Enum for suggestion origins
    - `DICTIONARY` - From loaded dictionaries
    - `CONTRACTION` - Smart contraction expansion
    - `PERSONAL` - User's personal dictionary

- Files: `ai.jagoan.keyboard.titan2.domain.model.*`

**Enhanced KeyboardSettings**:

- Added 3 new fields:
    - `autocorrectEnabled: Boolean` (default: true)
    - `autocorrectLanguages: List<String>` (default: ["en", "id"])
    - `showSuggestions: Boolean` (default: true)
- Backward compatible with existing preferences
- File: `ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings`

**New Repositories**:

- **DictionaryRepository** interface:
    - `loadDictionaries(languages: List<String>): Boolean`
    - `contains(word: String, language: String?): Boolean`
    - `getAllWords(): Set<String>`
    - `addToPersonalDictionary(word: String, language: String?)`
    - `removeFromPersonalDictionary(word: String)`
    - `isInPersonalDictionary(word: String): Boolean`
    - `detectLanguage(word: String): String?`
    - `expandContraction(word: String): String?`
- **ShortcutRepository** expanded:
    - Thread-safe in-memory caching
    - Efficient lookup during typing
    - Persistent storage with DataStore

- Files: `ai.jagoan.keyboard.titan2.domain.repository.*`

**Dependency Injection**:

- New Hilt providers:
    - `provideDictionaryRepository()` - Singleton repository
    - `provideAutocorrectEngine()` - Singleton engine
    - `provideAutocorrectManager()` - Singleton manager
- Enhanced AppModule with autocorrect dependencies
- Proper lifecycle scoping for all components
- File: `ai.jagoan.keyboard.titan2.di.AppModule`

### 🐛 Bug Fixes

- Fixed symbol picker state management to prevent overlay conflicts with suggestion bar
- Resolved suggestion bar flicker on keyboard layout changes and configuration updates
- Fixed DataStore concurrent write issues when rapidly changing settings
- Corrected key event propagation for physical keyboard Alt+Backspace combination
- Fixed accent cycling not resetting properly after character insertion
- Resolved memory leak in symbol picker overlay cleanup on IME service destruction
- Fixed autocorrect suggestions not clearing when switching between input fields
- Corrected dictionary loading race condition on first app launch
- Fixed sticky modifier state persisting incorrectly across app contexts
- Resolved currency symbol preference not applying immediately after change

### 🔧 Improvements

**Documentation**:

- Updated README with comprehensive autocorrect feature documentation
- Added accent support language list and configuration instructions
- Improved compilation and installation instructions with troubleshooting
- Enhanced keystore configuration documentation with keytool examples
- Added testing section with coverage reporting instructions
- Updated architecture documentation with new components

**Code Quality**:

- Enhanced KDoc comments for all public APIs
- Improved error handling in dictionary loading with detailed logging
- Better exception messages for debugging
- Optimized coroutine usage for background operations
- Reduced unnecessary recompositions in Settings UI
- Improved null safety in accent repository

**User Experience**:

- Better activation status detection in Settings screen
- Improved keyboard switch prompt messaging
- Enhanced visual feedback for toggle states
- Smoother transitions between symbol picker and suggestion bar
- More informative logging for troubleshooting

### 📦 Dependencies Updated

- **Android SDK**: 35 (Android 15)
- **Compose BOM**: 2024.02.00
- **Hilt**: 2.50
- **Kotlin**: 1.9.22
- **Kotlinx Coroutines**: 1.7.3
- **Kotlinx Serialization**: 1.6.2
- **AndroidX Core KTX**: 1.12.0
- **AndroidX Lifecycle**: 2.7.0
- **AndroidX DataStore**: 1.0.0
- **JUnit 5**: 5.10.1
- **MockK**: 1.13.8
- **Turbine**: 1.0.0
- **Truth**: 1.1.5

### 🏗️ Build System

- Version bumped from 0.3.1 to 0.4.0
- Build number auto-incremented to 239+
- Jacoco code coverage configuration (0.8.11)
- JUnit 5 test platform integration
- KSP annotation processing (1.9.22-1.0.17)
- Improved ProGuard rules for autocorrect engine
- R8 optimization enabled for release builds
- Custom APK output naming: `jagoan-keyboard-titan2-{variant}.apk`

### ⚠️ Known Issues

- Suggestion bar UI currently disabled by default (silent autocorrect only)
- JVM class sharing warnings during test execution (non-critical)
- Gradle 9.0 deprecation warnings (to be addressed in future update)
- Accent cycling may lag slightly on older devices
- Dictionary loading on first launch may take 2-3 seconds

---

## [0.3.1] - 2025-01-15

### 🔧 Improvements

**Performance**:

- Optimized symbol picker rendering performance with lazy composition
- Enhanced settings persistence reliability with retry logic
- Improved text shortcuts DataStore serialization efficiency
- Better handling of capacitive keyboard interference detection
- Reduced memory footprint during symbol picker display

**Documentation**:

- Updated README with comprehensive usage examples
- Added troubleshooting section for common issues
- Improved symbol picker keyboard mapping documentation
- Enhanced build instructions with platform-specific notes
- Added FAQ section for Titan 2 users

**Code Quality**:

- Enhanced logging throughout IME service lifecycle
- Improved error messages for debugging
- Better code organization in settings UI components
- Refined modifier key state machine logic

### 🐛 Bug Fixes

- Fixed sticky modifiers (Shift/Alt) not persisting across app restarts
- Resolved double-space period insertion timing issues with rapid typing
- Corrected currency symbol preference not applying immediately after change
- Fixed shortcut expansion not working in certain apps (WhatsApp, Gmail)
- Resolved memory leak in symbol picker overlay cleanup
- Fixed key repeat sometimes triggering on single press
- Corrected Alt+Backspace line deletion not working in some editors
- Fixed symbol picker occasionally showing wrong category after rotation
- Resolved rare crash when switching keyboards rapidly
- Fixed settings UI not reflecting changes immediately after toggle

### 📱 UX Enhancements

- Added subtle haptic feedback for physical symbol selection (optional)
- Improved symbol picker dismissal behavior (auto-hide after selection)
- Enhanced activation status detection in Settings screen
- Better keyboard switch prompt with direct Settings link
- Improved sticky modifier visual indicators
- Refined double-space timing threshold (500ms → 400ms)
- Better handling of landscape orientation in settings

### 🏗️ Build & Dependencies

- Version bumped from 0.3.0 to 0.3.1
- Minor dependency updates for security patches:
    - AndroidX Core KTX: 1.12.0 (security fix)
    - Compose BOM: 2024.01.00 (stability improvements)
- Improved ProGuard rules for release builds
- Fixed R8 optimization breaking text shortcuts in some cases
- Enhanced debug build logging without performance impact

### 📝 Internal Changes

- Refactored settings repository with better error handling
- Improved DataStore preferences migration logic
- Enhanced coroutine scope management in repositories
- Better separation of concerns in IME service
- Refined symbol data model structure

---

## [0.3.0] - 2025-01-08 (Initial Public Release)

### 🎉 Initial Features

#### Core Keyboard Functionality

**Physical Keyboard Integration**:

- Optimized for Unihertz Titan 2's 48-key QWERTY layout
- Full physical keyboard event handling
- Modifier key support (Shift, Alt, Sym)
- Capacitive keyboard coordination
- Meta key state tracking
- Hardware key event interception

**Smart Symbol Picker**:

- 19 non-redundant punctuation symbols per category
- Two-row layout: Row 1 (Q-P), Row 2 (A-L)
- Physical key mapping (Q=0, W=1, ..., P=9, A=0, S=1, ..., L=8)
- Six symbol categories:
    - **Punctuation** (19): ; [ ] { } < > | \ & ^ % ~ ` ... Rp ° § •
    - **Currency** (19): Rp $ € £ ¥ ₹ ₩ ₽ ₪ ₫ ฿ ₡ ₦ ₨ ₱ ₲ ₴ ₵ ₸
    - **Math** (19): ± × ÷ ≠ ≈ ≤ ≥ ∞ √ ∑ ∫ π ∂ ∆ Ω α β γ θ
    - **Arrows** (19): ← → ↑ ↓ ↔ ⇐ ⇒ ⇑ ⇓ ⇔ ↖ ↗ ↘ ↙ ⤴ ⤵ ➡ ⬅ ⬆
    - **Emoji** (19): 😀 😂 ❤️ 👍 🎉 ✅ ⚠️ 🔥 💯 🙏 😊 😢 😎 🤔 💪 🌟 ⭐ 🎯 ✨
    - **Miscellaneous** (19): © ® ™ § ¶ † ‡ • ° ‰ № ℃ ℉ Ω ℓ ™ ℠ ℗ ℅
- SYM key to open/cycle through categories
- Double-tap SYM for quick currency symbol (Rp)
- BlackBerry Passport-inspired UI design

**Physical Keyboard Alt Layer** - Quick access to common symbols:

```
Row 1: Q=0  W=1  E=2  R=3  T=(  Y=)  U=-  I=_  O=/  P=:
Row 2: A=@  S=4  D=5  F=6  G=*  H=#  J=+  K="  L='
Row 3: Z=!  X=7  C=8  V=9  B=.  N=,  M=?
```

#### Customizable Keyboard Behaviors

**Capitalization**:

- Auto-capitalization for sentence starts (default: OFF)
- Long-press Shift for CAPS LOCK mode (default: ON)
- Smart detection of sentence boundaries
- Proper noun handling

**Key Modifiers**:

- Sticky Shift - Shift stays active for next key (default: ON)
- Sticky Alt - Alt stays active for next key (default: ON)
- Visual modifier state indicators
- Timeout-based modifier reset

**Typing Enhancements**:

- Key repeat on long-press (default: OFF)
- Configurable repeat delay (400ms) and rate (50ms)
- Double-space period insertion (default: ON)
- Alt+Backspace to delete entire line (default: ON)

**Customization Options**:

- Preferred currency symbol (default: Rp for Indonesian users)
- Locale-aware currency detection
- Individual behavior toggles
- Persistent settings across sessions

#### Text Shortcuts System

**Shortcut Management**:

- User-defined text expansion shortcuts
- Trigger phrase → Replacement text mapping
- Real-time expansion during typing
- Case-sensitive matching
- Persistent storage with Kotlin Serialization

**ShortcutManagementScreen**:

- Full CRUD operations (Create, Read, Update, Delete)
- Search and filter shortcuts
- Alphabetically sorted list
- Empty state with helpful instructions
- Material Design 3 card-based UI

**Technical Implementation**:

- ShortcutRepositoryImpl with in-memory caching
- Thread-safe concurrent access with StateFlow
- Efficient lookup during typing (Map-based)
- ShortcutsDataStore for persistent storage
- Automatic sync between storage and memory

**Example Shortcuts**:

- "omw" → "On my way!"
- "brb" → "Be right back"
- "addr" → "123 Main Street, City, Country"
- "email" → "your.email@example.com"

#### Settings & Configuration

**Material Design 3 Interface**:

- Clean, modern settings screen
- Categorized settings sections
- Toggle switches with immediate feedback
- Navigation to sub-screens (Shortcuts, About)
- Activation status banner

**Settings Categories**:

- **Quick Settings**: Most frequently used toggles
- **Typing Behavior**: Capitalization, spacing, repeat
- **Modifiers**: Shift and Alt behavior
- **Symbol Picker**: Currency preference
- **Text Shortcuts**: Enable/disable, management access
- **Advanced**: Line deletion, key repeat timing
- **About**: Version info, licenses, support

**Activation Flow**:

- Keyboard enabled status detection
- Direct link to system IME settings
- Visual activation instructions
- Keyboard selection confirmation

**Settings Persistence**:

- DataStore Preferences for type-safe storage
- Reactive updates with Kotlin Flow
- Instant setting synchronization
- Migration support for future versions

#### Architecture & Technology Stack

**Architecture Pattern**:

- Clean Architecture with clear layer separation
- MVVM (Model-View-ViewModel) pattern
- Repository pattern for data abstraction
- Unidirectional data flow
- Dependency inversion principle

**Layers**:

- **Presentation** (`ui/`): Jetpack Compose UI, ViewModels
- **Domain** (`domain/`): Models, repository interfaces
- **Data** (`data/`): Repository implementations, DataStore
- **IME** (`ime/`): InputMethodService, key handling
- **DI** (`di/`): Hilt dependency injection modules

**Technology Stack**:

- **Kotlin 100%** - Modern, null-safe, concise
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - Latest design system
- **Hilt** - Compile-time dependency injection
- **Kotlin Coroutines** - Structured concurrency
- **Flow & StateFlow** - Reactive streams
- **DataStore Preferences** - Type-safe key-value storage
- **Kotlin Serialization** - JSON serialization

**Build Tools**:

- Gradle 8.4 with Kotlin DSL
- Version Catalogs for centralized dependency management
- KSP (Kotlin Symbol Processing) for annotations
- R8 code shrinking and obfuscation
- ProGuard rules for release optimization

#### Design System

**BlackBerry Passport-Inspired Design**:

- Nostalgia-driven UI inspired by BlackBerry Passport keyboard
- Professional, productivity-focused aesthetic
- Optimized for one-handed operation on Titan 2

**Color Palette**:

- **Background**: Pure black (#000000) for OLED battery efficiency
- **Accent**: Jagoan Green (#CDFF85) for CTAs and highlights
- **Surface**: Dark gray (#1A1A1A) for elevated cards
- **Text Primary**: Light gray (#E0E0E0) for optimal readability
- **Text Secondary**: Medium gray (#888888) for descriptions
- **Border**: Translucent white (#4A4A4A) for subtle separation

**Typography**:

- System font for platform consistency
- Clear hierarchy (Title, Body, Caption)
- Adequate line spacing for touch targets
- Proper contrast ratios (WCAG AA compliant)

**Components**:

- 3D button effects with elevation and borders
- Consistent 16dp rounded corners
- 24dp minimum touch targets
- Proper spacing rhythm (8dp grid)
- Smooth animations and transitions

#### Build Configuration

**Gradle Setup**:

- Kotlin DSL build scripts (`build.gradle.kts`)
- Version catalogs in `gradle/libs.versions.toml`
- Auto-incrementing build numbers via `version.properties`
- Keystore signing for release builds

**Build Variants**:

- **Debug**: Development build with logging, `.debug` suffix
- **Release**: Production build with R8 optimization, signed

**Build Features**:

- ProGuard/R8 code shrinking
- Resource shrinking
- Obfuscation for release builds
- Compose integration
- BuildConfig generation

**APK Naming**:

- Format: `jagoan-keyboard-titan2-{variant}.apk`
- Debug: `jagoan-keyboard-titan2-debug.apk`
- Release: `jagoan-keyboard-titan2-release.apk`

**Version Management**:

- versionName: 0.3.0
- versionCode: Auto-incremented (starts at 1)
- Semantic versioning (MAJOR.MINOR.PATCH)

#### Requirements

**Runtime Requirements**:

- **Device**: Unihertz Titan 2 (or compatible physical keyboard device)
- **Android**: 14+ (API 34 minimum, targeting API 35)
- **Storage**: ~25 MB for installed app
- **RAM**: Minimal overhead (<50 MB)

**Development Requirements**:

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 (Java Development Kit)
- **Android SDK**: API 35 (Android 15)
- **Gradle**: 8.4+ (included in wrapper)
- **Git**: For version control

**Optional Files**:

- `local.properties` - Android SDK path
- `keystore.properties` - Release signing configuration (optional)
- `version.properties` - Build number tracking

### 📄 Documentation

**Comprehensive README**:

- Project overview and key highlights
- Feature documentation with examples
- Architecture and technology stack details
- Complete compilation and installation guide
- Step-by-step setup instructions
- Usage guide with keyboard shortcuts
- Troubleshooting and known issues
- Contributing guidelines
- License information

**Code Documentation**:

- KDoc comments for all public APIs
- Inline comments for complex logic
- File headers with copyright and modifications
- Architecture decision records (ADRs) in comments

**Build Instructions**:

- Platform-specific setup (macOS, Windows, Linux)
- Keystore generation for release builds
- ADB installation commands
- Gradle task reference
- Testing and linting commands

### 📞 Support

- **GitHub Issues**: Bug reports and feature requests
- **README**: Comprehensive documentation
- **Code Comments**: Implementation details
- **Community**: Titan 2 user forums and groups

---

## Version History Summary

- **0.4.0** (2025-01-22): Intelligent autocorrect, accent support, UI redesign, testing
- **0.3.1** (2025-01-15): Bug fixes, performance improvements, UX enhancements
- **0.3.0** (2025-01-08): Initial public release with core keyboard functionality

---

**Legend**:

- 🎉 Major Features
- ✨ New Features
- 🐛 Bug Fixes
- 🔧 Improvements
- 📦 Dependencies
- 🏗️ Build System
- 📱 UX Enhancements
- ⚡ Performance
- 🧪 Testing
- 📄 Documentation
- ⚠️ Known Issues

---

**Copyright (c) 2025 Aryo Karbhawono**  
Licensed under the Apache License, Version 2.0
