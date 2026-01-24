# Contributions & Feature Attribution

This document differentiates the features and contributions from the original author (Divefire) versus the enhancements and new features added by Aryo Karbhawono.

---

## 🏛️ Original Author: Divefire (titan2keyboard)

**Repository**: https://github.com/Divefire/titan2keyboard  
**Version**: 0.2.0  
**Copyright**: 2024-2025 Divefire  
**License**: Apache License 2.0

### Core Foundation (What Divefire Built)

#### 1. **Core Architecture & Structure**

- Clean Architecture pattern with layer separation
- MVVM (Model-View-ViewModel) implementation
- Repository pattern for data abstraction
- Dependency Injection with Hilt
- Project structure and package organization

#### 2. **Input Method Service (IME) Core**

- `JagoanInputMethodService` (originally `Titan2InputMethodService`)
- Physical keyboard event handling framework
- Input connection management
- Editor info detection
- Modifier key state tracking (Shift, Alt, Sym)

#### 3. **Key Event Handler**

- `KeyEventHandler` class foundation
- Basic physical key event processing
- Modifier state management
- Alt layer symbol mapping (original 21 symbols)
- Sticky modifier logic (Shift, Alt)

#### 4. **Symbol Picker System**

- `SymbolRepository` with 6 categories structure
- `SymbolPickerOverlay` UI component
- Two-row keyboard layout (Q-P, A-L)
- Physical key mapping for symbol selection
- SYM key cycling through categories
- Original symbol sets for 6 categories (Punctuation, Currency, Math, Arrows, Emoji, Miscellaneous)

#### 5. **Settings System**

- `SettingsRepository` interface and implementation
- `SettingsViewModel` with StateFlow
- `SettingsActivity` with Jetpack Compose
- `SettingsScreen` foundation (later redesigned)
- DataStore Preferences integration
- Settings persistence layer

#### 6. **Core Settings Features**

- Auto-capitalization toggle
- Sticky Shift toggle
- Sticky Alt toggle
- Long-press Shift for CAPS LOCK
- Key repeat on long-press
- Double-space period insertion
- Preferred currency symbol selection
- Keyboard activation status detection

#### 7. **Text Shortcuts System**

- `ShortcutRepository` interface and implementation
- `ShortcutRepositoryImpl` with in-memory caching
- `ShortcutsDataStore` for persistence
- `ShortcutManagementScreen` UI
- `ShortcutManagementViewModel`
- CRUD operations for shortcuts
- Text expansion during typing

#### 8. **Domain Models**

- `KeyboardSettings` data class
- `ModifierState` sealed class
- `KeyEventResult` sealed class
- `TextShortcut` data class
- `Symbol` data class

#### 9. **UI Components**

- `ModifierIndicatorView` for visual modifier state
- Basic symbol picker layout
- Settings card components

#### 10. **Utilities**

- `LocaleUtils` for locale-aware operations
- Basic logging setup

#### 11. **Build Configuration**

- Gradle Kotlin DSL setup
- Version catalogs
- Build variants (debug/release)
- ProGuard rules foundation
- Auto-incrementing build numbers

---

## 🚀 Enhancements & New Features: Aryo Karbhawono (JagoanKeyboardForTitan2)

**Repository**: https://github.com/karbhawono/JagoanKeyboardForTitan2  
**Starting Version**: 0.3.0 (forked from titan2keyboard 0.2.0)  
**Current Version**: 0.4.7  
**Copyright**: 2025 Aryo Karbhawono  
**License**: Apache License 2.0

### Major Enhancements & New Features

#### 1. **Intelligent Autocorrect System** (v0.4.0)

- **NEW**: `DictionaryRepository` interface and implementation
- **NEW**: Multi-language dictionary support (English, Indonesian)
- **NEW**: 53,902+ English words from multiple sources
- **NEW**: Real-time word validation during typing
- **NEW**: Typo detection and correction suggestions
- **NEW**: Word frequency-based suggestions
- **NEW**: Context-aware corrections
- **NEW**: Sentence boundary detection
- **NEW**: Proper noun handling
- **NEW**: Multi-dictionary architecture (base, common, extended)
- **NEW**: Python tools for dictionary processing
- **NEW**: Word deduplication and normalization tools

#### 2. **Dictionary Management & Backup System** (v0.4.7)

- **NEW**: `DictionaryManagementViewModel` - Complete dictionary management
- **NEW**: `DictionaryManagementScreen` - Full-featured UI for dictionary operations
- **NEW**: Add-to-Dictionary feature (Vi-mode commands and long-press)
- **NEW**: Export custom words to ZIP backup
- **NEW**: Import custom words from ZIP backup
- **NEW**: Import modes: Merge and Replace
- **NEW**: Custom word validation (format, duplicates, length)
- **NEW**: Per-language custom word management
- **NEW**: View all custom words by language
- **NEW**: Delete individual custom words
- **NEW**: Clear all custom words (all languages or per-language)
- **NEW**: Backup manifest with version and metadata
- **NEW**: FileProvider integration for secure file sharing
- **NEW**: Result types: `ExportResult`, `ImportResult`, `AddWordResult`
- **NEW**: Data models: `BackupManifest`, `LanguageBackup`, `ImportMode`
- **NEW**: Dedicated custom word files (`en_custom.txt`, `id_custom.txt`)

#### 3. **Vi-mode Cursor Navigation & Editing** (v0.4.3, v0.4.7)

- **NEW**: Complete Vi-mode implementation for cursor navigation
- **NEW**: Vi commands: `h`, `j`, `k`, `l` for cursor movement
- **NEW**: Vi commands: `w`, `b`, `e` for word navigation
- **NEW**: Vi commands: `0`, `$` for line start/end
- **NEW**: Vi commands: `x`, `dd` for text deletion
- **NEW**: Vi commands: `:atd`, `:atdi`, `:atde` for dictionary management
- **NEW**: Command mode activation with colon (`:`)
- **NEW**: Command parsing and execution engine
- **NEW**: Colon detection support (ALT+p on Titan Pocket 2)
- **NEW**: Robust unicode character detection
- **NEW**: Multiple Enter key detection (KEYCODE_ENTER, NUMPAD_ENTER, DPAD_CENTER)
- **NEW**: Command feedback via suggestion bar

#### 4. **Suggestion Bar (IME Controls)** (v0.4.2, v0.4.3)

- **NEW**: `SuggestionBarView` - Custom IME suggestion bar
- **NEW**: Non-Compose view for IME compatibility
- **NEW**: Three autocorrect suggestions display
- **NEW**: One-tap suggestion selection
- **NEW**: Visual feedback for suggestion acceptance
- **NEW**: Temporary messages for user feedback
- **NEW**: Vi-mode command feedback display
- **NEW**: Add-to-Dictionary feedback display
- **NEW**: Debounced updates to prevent flicker
- **NEW**: View reuse for performance optimization

#### 5. **Multi-Language Accent Support** (v0.4.0)

- **NEW**: Long-press accent picker overlay
- **NEW**: 150+ accented characters support
- **NEW**: Language-specific accent sets:
    - French: é, è, ê, ë, à, â, ù, û, ü, ô, ï, ç, œ, æ
    - Spanish: á, é, í, ó, ú, ñ, ü, ¿, ¡
    - German: ä, ö, ü, ß
    - Portuguese: á, â, ã, à, é, ê, í, ó, ô, õ, ú, ü, ç
    - Italian: à, è, é, ì, ò, ù
    - Vietnamese: ă, â, đ, ê, ô, ơ, ư, and tone marks
    - Indonesian: é, è
    - Turkish: ç, ğ, ı, İ, ö, ş, ü
    - Polish: ą, ć, ę, ł, ń, ó, ś, ź, ż
    - Czech/Slovak: á, č, ď, é, ě, í, ň, ó, ř, š, ť, ú, ů, ý, ž
- **NEW**: Quick numeric row for accent selection (1-9, 0)
- **NEW**: Visual accent preview
- **NEW**: Toggle setting for accent feature

#### 6. **Symbol Picker Enhancements** (v0.4.4)

- **ENHANCED**: Category shortcuts: SYM+P/C/M/A/E/O
- **ENHANCED**: Direct access to specific symbol categories
- **ENHANCED**: Currency shortcut: SYM+C+C + country code
- **ENHANCED**: 50+ country code currency mappings
- **ENHANCED**: Auto-space after currency insertion
- **ENHANCED**: Category state tracking
- **ENHANCED**: Improved SYM key behavior
- **ENHANCED**: Visual category indicators

#### 7. **Performance Optimization Utilities** (v0.3.0+)

- **NEW**: `LazyLog` - Lazy logging to avoid string construction overhead
- **NEW**: `PerformanceMonitor` - Real-time performance tracking
- **NEW**: `Debouncer` - Battery optimization for frequent operations
- **NEW**: `ObjectPool` - Object pooling for memory efficiency
- **NEW**: Method timing and profiling
- **NEW**: Memory allocation tracking
- **NEW**: FPS monitoring
- **NEW**: Performance warnings for slow operations

#### 8. **Smart Number Formatting** (v0.4.1)

- **NEW**: `NumberFormatter` utility class
- **NEW**: Indonesian thousand separator (123.456.789)
- **NEW**: Indonesian decimal separator (3,14)
- **NEW**: Automatic number formatting during typing
- **NEW**: Context-aware formatting
- **NEW**: Currency amount formatting
- **NEW**: Configurable formatting rules
- **NEW**: Comprehensive unit tests

#### 9. **UI/UX Complete Redesign** (v0.4.0+)

- **REDESIGNED**: `SettingsScreen` - Complete Material Design 3 overhaul
- **NEW**: `SettingsDesignTokens` - Consistent design system
- **NEW**: QuickAccessToggles - 2x2 grid for frequently used settings
- **NEW**: Emoji icons for all settings
- **NEW**: Grouped settings cards (single card per section)
- **NEW**: SectionHeader with icons and separators
- **NEW**: QuickToggleItem with active/inactive states
- **NEW**: 3D button effects with elevation
- **NEW**: Jagoan Green (#CDFF85) brand color
- **NEW**: Enhanced ActivationBanner
- **NEW**: Modern card layouts with consistent spacing
- **NEW**: Improved visual hierarchy
- **REDESIGNED**: `AboutScreen` - Compact, modern layout
- **NEW**: `LicensesScreen` - Open source license viewer
- **NEW**: `DictionaryManagementScreen` - Full-featured dictionary UI

#### 10. **Advanced Key Handling** (v0.4.1+)

- **ENHANCED**: ALT+Space fixes and improvements
- **ENHANCED**: Auto-unlock ALT after special keys
- **ENHANCED**: Backspace behavior improvements
- **NEW**: ALT+Backspace line deletion
- **ENHANCED**: Space bar context awareness
- **ENHANCED**: Enter key multi-variant detection
- **ENHANCED**: Unicode character detection improvements
- **ENHANCED**: Modifier state synchronization

#### 11. **Testing Infrastructure** (v0.4.0)

- **NEW**: JUnit 5 integration
- **NEW**: MockK for mocking
- **NEW**: Turbine for Flow testing
- **NEW**: Truth assertions
- **NEW**: Jacoco code coverage
- **NEW**: `SettingsRepositoryImplTest` - 16 test cases
- **NEW**: `NumberFormatterTest` - Comprehensive formatting tests
- **NEW**: Test utilities and helpers
- **NEW**: Coverage reporting setup

#### 12. **Documentation** (All versions)

- **NEW**: `changes.md` - Comprehensive changelog with 1500+ lines
- **NEW**: `USAGE.md` - Detailed usage guide
- **NEW**: `BACKUP_EXPORT_IMPLEMENTATION.md` - Implementation details
- **NEW**: `README.md` - Enhanced with all features
- **NEW**: `CONTRIBUTIONS.md` - This file
- **ENHANCED**: Inline code documentation with detailed KDoc
- **ENHANCED**: Architecture decision records (ADRs) in comments
- **ENHANCED**: File headers with modification tracking

#### 13. **Build System Enhancements** (v0.3.0+)

- **ENHANCED**: Gradle configuration optimization
- **NEW**: Jacoco integration for coverage
- **NEW**: Custom APK naming convention
- **NEW**: Enhanced ProGuard rules
- **ENHANCED**: Dependency version management
- **NEW**: Build number auto-increment system
- **NEW**: Debug/Release variant optimization

#### 14. **Data & License Management** (v0.4.0)

- **NEW**: `LicenseDataProvider` - Open source license tracking
- **NEW**: Library attribution system
- **NEW**: Organized license categories (Android, Compose, Kotlin, Testing)
- **NEW**: License viewer UI
- **NEW**: Library version tracking

---

## 📊 Statistics Summary

### Original Contribution (Divefire - v0.2.0)

- **Core Architecture**: 100% foundation
- **IME Framework**: Complete base implementation
- **Symbol Picker**: Full 6-category system
- **Settings System**: Complete foundation
- **Text Shortcuts**: Full implementation
- **Estimated Lines of Code**: ~3,000-4,000 lines
- **Key Files**: ~20-25 Kotlin files

### Enhancements & New Features (Aryo - v0.3.0 to v0.4.7)

- **Autocorrect System**: 100% new
- **Dictionary Management**: 100% new
- **Vi-mode**: 100% new
- **Suggestion Bar**: 100% new
- **Accent Support**: 100% new
- **Performance Utilities**: 100% new
- **Number Formatting**: 100% new
- **UI Redesign**: ~80% redesigned
- **Testing**: 100% new
- **Documentation**: ~90% new/enhanced
- **Estimated Additional Lines of Code**: ~8,000-10,000 lines
- **New/Enhanced Files**: ~40+ Kotlin files

### Version Progression

- **v0.2.0** (Divefire): Base keyboard functionality
- **v0.3.0** (Aryo): Initial public release with foundation
- **v0.3.1**: Bug fixes and improvements
- **v0.4.0**: Major autocorrect, accents, UI redesign
- **v0.4.1**: Number formatting, key handling improvements
- **v0.4.2**: Suggestion bar initial implementation
- **v0.4.3**: Vi-mode navigation, suggestion bar enhancements
- **v0.4.4**: Symbol picker shortcuts
- **v0.4.5**: Dictionary updates
- **v0.4.6**: Dictionary overhaul (53,902 words)
- **v0.4.7**: Dictionary management, backup/export/import, Vi-mode dictionary commands

---

## 🤝 Collaboration Model

This project follows an **enhancement fork** model:

1. **Foundation**: Divefire provided the solid architectural foundation and core IME functionality
2. **Evolution**: Aryo built upon this foundation with advanced features, performance optimizations, and modern UI/UX
3. **Attribution**: All original files maintain Divefire's copyright with clear modification notes
4. **License Compliance**: Both contributions follow Apache License 2.0
5. **Open Source**: Both authors contribute to the open-source Android keyboard ecosystem

---

## 📜 License Attribution

### Original Work

```
Copyright (c) 2024-2025 Divefire
Licensed under the Apache License, Version 2.0
```

### Modifications

```
Modifications Copyright (c) 2025 Aryo Karbhawono
Licensed under the Apache License, Version 2.0
```

### New Files

```
Copyright (c) 2025 Aryo Karbhawono
Licensed under the Apache License, Version 2.0
```

---

## 🎯 Summary

**Divefire's titan2keyboard** provided the essential foundation:

- Robust IME architecture
- Physical keyboard integration
- Symbol picker system
- Settings framework
- Text shortcuts

**Aryo's JagoanKeyboardForTitan2** built upon this with:

- Intelligent autocorrect with 53,902+ word dictionary
- Complete dictionary management & backup system
- Vi-mode cursor navigation
- Suggestion bar for IME controls
- Multi-language accent support
- Advanced symbol picker shortcuts
- Performance optimization utilities
- Smart number formatting
- Complete UI/UX redesign
- Comprehensive testing infrastructure
- Extensive documentation

**Result**: A feature-rich, high-performance physical keyboard IME optimized for Unihertz Titan 2, combining solid architectural foundations with modern features and user experience enhancements.

---

**Last Updated**: 2025-01-24  
**Document Version**: 1.0
