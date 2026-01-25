# Jagoan Keyboard for Titan 2 - Features

A comprehensive IME (Input Method Editor) keyboard designed specifically for Unihertz Titan 2 physical keyboard users.

---

## 🎯 Core Features

### Dictionary Management System

**Complete backup, export, import, and management solution for custom words**

- **View Custom Words**:
    - See all custom words grouped by language
    - Total word count at a glance
    - Organized list with language section headers
    - Empty state with helpful instructions

- **Export & Backup**:
    - Export to ZIP backup file (timestamped: `custom_words_backup_<timestamp>.zip`)
    - Contains JSON manifest with metadata
    - Includes per-language text files (`en_custom.txt`, `id_custom.txt`)
    - Share via any app (Google Drive, Email, Files, etc.)
    - Secure FileProvider integration (Android 7+)

- **Import & Restore**:
    - **Merge Mode**: Add new words, keep existing (skip duplicates)
    - **Replace Mode**: Clear all existing, import from backup
    - Import validation with version compatibility checks
    - Detailed summary showing added/skipped/error counts

- **Word Management**:
    - Delete individual custom words with confirmation
    - Clear all custom words (per-language or all languages)
    - Warning dialogs for destructive actions

- **Access**: Settings → "📖 Manage Dictionary" button

### Add-to-Dictionary Feature

**Multiple ways to add custom words to your personal dictionary**

1. **Vi-mode Commands** - Fast command-line style word addition
    - `:atd <word>` - Add to Dictionary (auto-detect language)
    - `:atdi <word>` - Add to Dictionary (Indonesian)
    - `:atde <word>` - Add to Dictionary (English)
    - Visual feedback in suggestion bar

2. **Long-press Suggestions** - Context menu from suggestion bar
    - Long-press any suggestion chip
    - Select language (Indonesian / English)
    - Instant feedback on success/duplicate/error

**Word Validation:**

- Minimum 2 characters
- Alphabetic only (plus apostrophes and hyphens)
- Automatic lowercase conversion
- Duplicate detection across all dictionaries

### Vi Mode Cursor Navigation & Editing

**Professional text editing with Vi commands**

- **Toggle**: TAB+V+I keyboard sequence
    - ⚠️ **Requires Titan 2 Fn Key Setup**: Map Fn → TAB in device keyboard settings
    - Physical shortcut: Press **Fn+V+I** (sends TAB+V+I to keyboard)
- **Visual Indicator**: Green bullet (●) on suggestion bar when active
- **Navigation Commands**:
    - `h, j, k, l` - Move cursor left, down, up, right
    - `w` - Forward by word
    - `b` - Backward by word
    - `0` - Beginning of line
    - `$` - End of line
    - `gg` - Beginning of document
    - `G` - End of document
- **Editing Commands**:
    - `dd` - Delete line
    - `yy` - Yank (copy) line
    - `p` - Paste
    - `u` - Undo
    - `r` - Replace character
    - `x` - Delete character under cursor
- **Dictionary Commands** (v0.4.7):
    - `:atd <word>` - Add to Dictionary (auto-detect)
    - `:atdi <word>` - Add to Dictionary (Indonesian)
    - `:atde <word>` - Add to Dictionary (English)

**Enhanced Key Detection** (v0.4.7):

- Unicode character detection with modifier states
- ALT+key combinations (e.g., ALT+p for colon)
- Multiple Enter key codes (ENTER, NUMPAD_ENTER, DPAD_CENTER)
- Improved command mode state management

**Setup Required:**

- Navigate to **Titan 2 Keyboard Settings** (device settings, not app)
- Go to **Fn Key Mapping**
- Map **Fn → TAB**
- This enables Vi mode toggle via **Fn+V+I**

### Smart Suggestion Bar

**Intelligent autocorrect suggestions**

- **Compact Design**: 25dp height (50% smaller than standard)
- **Display Modes**:
    - ALWAYS_SHOW: Shows when typing with text/suggestions
    - AUTO: Shows with 2+ characters or suggestions
    - OFF: Never shows
- **Features**:
    - Centered suggestion chips with black background
    - High-confidence suggestions in bold
    - One-tap suggestion selection
    - Automatically hides to preserve keyboard gestures
    - Vi mode indicator integration

### Intelligent Autocorrect System

**Advanced typo correction and word suggestions with extensive dictionaries**

- **Multi-Algorithm Engine**:
    - Levenshtein edit distance (max 2 edits)
    - Keyboard proximity analysis for QWERTY layout
    - Smart contractions support (don't, can't, won't)
    - Context-aware language detection
- **Confidence Scoring**:
    - High confidence (>0.8) - Bold suggestions
    - Medium confidence (>0.5) - Regular suggestions
    - Top 5 suggestions sorted by confidence
- **Multi-Language Support** (v0.5.0):
    - **English dictionary**: 5,000 high-frequency words
    - **Indonesian dictionary**: 5,000 common words
    - **English contractions**: 46 entries (can't, don't, won't, etc.)
    - **Total**: 10,045 built-in words (8.5x expansion from v0.4.6)
- **Dictionary Quality**:
    - All lowercase, no duplicates
    - Common words verified (the, a, is, and / ada, adalah, dan, yang)
    - Fast loading (<100ms), minimal memory (~0.5MB)
    - Comprehensive unit tests (12 tests validating integrity)
- **Custom Words Storage** (v0.4.7):
    - Separate storage for custom words (`en_custom.txt`, `id_custom.txt`)
    - Never mixed with built-in dictionaries
    - Export/import ready backup format

### Smart Number Formatting

**Automatic thousand separators**

- **Auto-Format**: `50000` → `50,000` on SPACE
- **Preserves Decimals**: `50000.00` → `50,000.00`
- **Intelligent Detection**:
    - Skips phone numbers (country-aware)
    - Skips credit cards (Luhn algorithm validation)
    - Skips tracking numbers
    - Disabled in password/email/URI fields
- **Supported Formats**: Indonesia, Malaysia, Singapore, Thailand, Philippines, Vietnam, US, UK, Australia

---

## ⌨️ Physical Keyboard Features

### Sticky Modifier Keys

**Keep modifiers active for multiple keys**

- **Sticky Shift** 📌:
    - Single tap: Active for next key only
    - Double tap: Locked until manually released
    - Long press: Locked until manually released
- **Sticky Alt** ⌥:
    - Single tap: Active for next key only
    - Double tap: Locked until manually released
    - Long press: Locked until manually released
- **Visual Feedback**: Status bar notification shows active modifiers

### Symbol Picker & Category Shortcuts

**Direct category access with keyboard shortcuts**

#### SYM Key Behavior

| Action         | Result                                  |
| -------------- | --------------------------------------- |
| **Single tap** | Show/cycle symbol picker                |
| **Double tap** | Insert preferred currency (e.g., "Rp ") |
| **Long press** | No action (reserved for future)         |

#### Category Shortcuts

Jump directly to specific symbol categories without cycling:

| Shortcut  | Category    | Example Symbols                  |
| --------- | ----------- | -------------------------------- |
| **SYM+P** | Punctuation | `;` `[` `]` `{` `}` `\|` `^` `~` |
| **SYM+C** | Currency    | $ € £ ¥ ₹ Rp ₩ ₽                 |
| **SYM+M** | Math        | ± × ÷ ≠ ≈ ≤ ≥ ∞ √ π              |
| **SYM+A** | Arrows      | ← → ↑ ↓ ↔ ⇐ ⇒ ⇔                  |
| **SYM+E** | Emoji       | 😀 😂 ❤️ 👍 🎉 ✅ 🔥             |
| **SYM+O** | Other       | © ® ™ § ¶ † • ° ★                |

**Benefits:**

- Instant access to needed symbols
- No need to cycle through 6 categories
- Mnemonic shortcuts (**P**unctuation, **C**urrency, **M**ath, etc.)

#### Currency Country Code Shortcut

Insert currency symbols by country code:

**Usage:** **SYM+C+C+[CountryCode]**

**Examples:**

- **SYM+C+C+ID** → "Rp " (Indonesia)
- **SYM+C+C+US** → "$ " (USA)
- **SYM+C+C+GB** → "£ " (Great Britain)
- **SYM+C+C+EU** → "€ " (European Union)
- **SYM+C+C+JP** → "¥ " (Japan)
- **SYM+C+C+IN** → "₹ " (India)

**How it works:**

1. Press **SYM+C** → Currency overlay appears
2. Press **C** again → Country code mode activated
3. Type 2-letter country code → Currency inserted

**Supported:** 50+ country codes for major world currencies

### Multi-Language Accent Support

**Long-press for accent variants**

- **Supported Languages** (7 total):
    - French: é è ê ë à â ù û ü ï î ô ç œ æ
    - German: ä ö ü ß
    - Spanish: á é í ó ú ñ ü ¿ ¡
    - Portuguese: á à â ã é ê í ó ô õ ú ç
    - Italian: à è é ì ò ù
    - Danish: æ ø å
    - Norwegian: æ ø å
- **Usage**: Long-press letter key to cycle through variants
- **150+ Character Mappings**

### Symbol Picker

**Easy access to special characters with physical keyboard mapping**

- **Activation**:
    - Single tap SYM → Show/cycle through categories
    - **SYM+P/C/M/A/E/O** → Jump directly to category (see Category Shortcuts above)
- **Categories** (6 total):
    - Punctuation: `;` `[` `]` `{` `}` `|` `\` `&` `^` `%` `~` `` ` `` `...` `°` `§` `•`
    - Currency: $ € £ ¥ ₹ Rp ₩ ₽ ₿ ¢ ₪ ₫ ₱ ฿ and more
    - Math: ± × ÷ ≠ ≈ ≤ ≥ ∞ √ ∑ ∏ ∫ π ∂ ∆ ∇ µ and more
    - Arrows: ← → ↑ ↓ ↔ ↕ ⇐ ⇒ ⇑ ⇓ ⇔ ↵ ↩ ↪ ⟲ ⟳ and more
    - Emoji: 😀 😃 😊 😍 😢 😭 😤 👍 👎 ❤️ 🔥 ✨ 💯 ✅ ❌ ⚠️ 🎉 and more
    - Other: © ® ™ § ¶ † ‡ • · … — – ‹ › « » № ℃ ℉ ♠ ♥ ♦ ♣ ♪ and more
- **Selection**:
    - Physical keyboard layout (Q-P = row 1, A-L = row 2, Z-M = row 3)
    - Press corresponding letter key to insert symbol shown above it
- **Quick Dismiss**: Press BACK, SYM, or ESC

---

## 🔧 Customization & Settings

### Text Behavior

- **Auto-Capitalize** 🔠: First letter of sentences
- **Auto-Format Numbers**: Thousand separators
- **Key Repeat**: Enable/disable key repetition
- **Long-press Capitalize**: Hold letter for uppercase
- **Long-press Accents**: Cycle accent variants

### Autocorrect Settings

- **Enable/Disable**: Master autocorrect toggle
- **Language Selection**: Choose active dictionaries
- **Suggestion Bar Mode**: ALWAYS_SHOW / AUTO / OFF

### Modifier Keys

- **Sticky Shift**: Keep Shift active
- **Sticky Alt**: Keep Alt active

### Currency & Symbols

- **Preferred Currency**: Default currency symbol
- **Symbol Categories**: Organize special characters

### Advanced

- **Keyboard Gesture Support**: Auto-hide IME for Titan 2 gestures
- **Personal Dictionary**: Add custom words
- **Settings Backup**: DataStore persistence

---

## 🎨 User Interface

### Material Design 3

- **Design Tokens**: Jagoan Green (#CDFF85) accent color
- **Dark Theme**: Pure black (#121212) for OLED
- **Consistent Spacing**: 8dp, 16dp, 24dp hierarchy
- **Rounded Corners**: 12dp medium, 16dp large

### Quick Access Toggles

**2×2 grid for frequently used settings**

- Sticky Shift, Sticky Alt, Auto-capitalize, Long-press Accents
- Visual active states with green border

### Settings Organization

- **General**: Core typing behavior
- **Autocorrect**: Dictionary and suggestions
- **Dictionary Management**: View, export, import, delete custom words (v0.4.7)
- **Keyboard**: Modifiers and special keys
- **Currency & Symbols**: Quick access characters
- **Advanced**: Power user features

### License Attribution

**Comprehensive open source credits**

- 30+ library entries with versions
- Categorized by Build Tools, Core, UI, DI, Testing
- Expandable cards with license details

---

## 🚀 Performance & Optimization

### Efficient Architecture

- **Dependency Injection**: Hilt for modular design
- **Coroutines**: Async operations without blocking
- **DataStore**: Fast, type-safe preferences
- **Object Pooling**: Reduced garbage collection

### Smart Resource Management

- **LazyLog**: Deferred string evaluation for logging
- **PerformanceMonitor**: Method execution tracking
- **Debouncer**: Event throttling for UI operations
- **Capacitive Blocking**: 1000ms cooldown after physical keys

### Background Processing

- **Async Dictionary Loading**: Non-blocking initialization
- **Thread-Safe Operations**: Concurrent access support
- **Efficient Caching**: In-memory word lookups

---

## 🔒 Privacy & Compatibility

### Privacy First

- **No Internet Permission**: All processing on-device
- **No Data Collection**: Your typing stays private
- **Local Dictionaries**: No cloud sync

### Titan 2 Optimized

- **Physical Keyboard Focus**: Designed for hardware typing
- **Gesture Preservation**: IME hides for keyboard gestures
- **Compact UI**: Minimal screen displacement (25dp)
- **Status Bar Integration**: Modifier state notifications

### Android Compatibility

- **Target SDK**: 34 (Android 14)
- **Min SDK**: 29 (Android 10)
- **Architecture**: Kotlin, Jetpack Compose, Material 3

---

## 📝 Version History

- **v0.5.0** (2025-01-25): Dictionary expansion to 5k words per language (10,045 total), comprehensive testing suite
- **v0.4.7** (2025-01-24): Dictionary Management, Add-to-Dictionary, Vi-mode enhancements
- **v0.4.6** (2025-01-24): Dictionary overhaul (53,902 English words)
- **v0.4.5** (2025-01-24): Dictionary updates
- **v0.4.4** (2025-01-24): Symbol picker shortcuts, currency by country code
- **v0.4.3** (2025-01-23): Vi Mode, Keyboard gesture fix
- **v0.4.2** (2025-01-23): Smart Suggestion Bar
- **v0.4.1** (2025-01-22): Smart Number Formatting
- **v0.4.0** (2025-01-22): Intelligent Autocorrect System
- **v0.3.1** (2025-01-15): Performance improvements
- **v0.3.0** (2025-01-08): Initial public release

---

## 🛠️ Technical Stack

- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose with Material 3
- **Dependency Injection**: Hilt
- **Async**: Kotlin Coroutines & Flow
- **Storage**: AndroidX DataStore
- **Testing**: JUnit 5, MockK, Turbine
- **Build System**: Gradle 8.7 with KSP

---

## 📄 License

Copyright © 2025 Aryo Karbhawono

Licensed under the Apache License, Version 2.0

---

## 🔗 Links

- **GitHub**: [JagoanKeyboardForTitan2](https://github.com/karbhawono/JagoanKeyboardForTitan2)
- **Issues**: Report bugs and feature requests
- **Discussions**: Community support and ideas

---

_Made with ❤️ for Unihertz Titan 2 users_
