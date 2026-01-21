#!/bin/bash
# Automated Autocorrect Testing Script
# Tests the autocorrect feature via ADB commands

set -e

REPORT_DIR="test_reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/autocorrect_test_$TIMESTAMP.txt"
SCREENSHOT_DIR="$REPORT_DIR/screenshots_$TIMESTAMP"

mkdir -p "$SCREENSHOT_DIR"

echo "=====================================" | tee -a "$REPORT_FILE"
echo "Autocorrect Feature Test Report" | tee -a "$REPORT_FILE"
echo "Timestamp: $(date)" | tee -a "$REPORT_FILE"
echo "=====================================" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

# Function to take screenshot
take_screenshot() {
    local name=$1
    echo "  📸 Taking screenshot: $name" | tee -a "$REPORT_FILE"
    adb shell screencap -p /sdcard/screenshot.png
    adb pull /sdcard/screenshot.png "$SCREENSHOT_DIR/${name}.png" > /dev/null 2>&1
    adb shell rm /sdcard/screenshot.png
}

# Function to type text via ADB
type_text() {
    local text=$1
    echo "  ⌨️  Typing: '$text'" | tee -a "$REPORT_FILE"
    adb shell input text "$text"
    sleep 0.3
}

# Function to press a key
press_key() {
    local key=$1
    echo "  🔘 Pressing: $key" | tee -a "$REPORT_FILE"
    adb shell input keyevent "$key"
    sleep 0.2
}

# Function to clear text field
clear_field() {
    echo "  🧹 Clearing field" | tee -a "$REPORT_FILE"
    # Select all and delete
    adb shell input keyevent KEYCODE_MOVE_END
    for i in {1..50}; do
        adb shell input keyevent KEYCODE_DEL
    done
    sleep 0.3
}

echo "🚀 Starting Autocorrect Tests..." | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

# Open Google Keep for testing
echo "📱 Opening Google Keep for text input..." | tee -a "$REPORT_FILE"
adb shell am start -n com.google.android.keep/.activities.ShareReceiverActivity > /dev/null 2>&1 || \
    adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.google.android.keep/.activities.MainActivity > /dev/null 2>&1 || \
    adb shell am start -n com.google.android.apps.keep/.ui.activities.BrowseActivity > /dev/null 2>&1
sleep 2

# Try to create new note
adb shell input keyevent KEYCODE_N  # Try shortcut
sleep 1
adb shell input tap 540 1800  # Fallback: tap new note button (center bottom)
sleep 1

take_screenshot "01_initial"

echo "" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 1: Simple Typo Correction" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing: 'teh' should autocorrect to 'the'" | tee -a "$REPORT_FILE"
type_text "teh"
take_screenshot "02_before_space_teh"
press_key "KEYCODE_SPACE"
sleep 0.5
take_screenshot "03_after_space_the"
press_key "KEYCODE_ENTER"
echo "✓ Test 1 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 2: Contraction Correction" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing: 'dont' should autocorrect to 'don't'" | tee -a "$REPORT_FILE"
type_text "dont"
take_screenshot "04_before_space_dont"
press_key "KEYCODE_SPACE"
sleep 0.5
take_screenshot "05_after_space_dont"
type_text "worry"
press_key "KEYCODE_SPACE"
press_key "KEYCODE_ENTER"
echo "✓ Test 2 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 3: Multiple Typos" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing multiple typos in sequence" | tee -a "$REPORT_FILE"
type_text "helo"
press_key "KEYCODE_SPACE"
type_text "wrld"
press_key "KEYCODE_SPACE"
take_screenshot "06_multiple_typos"
press_key "KEYCODE_ENTER"
echo "✓ Test 3 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 4: Undo Autocorrect (Backspace)" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing: Type 'teh', space (autocorrects), backspace (should undo)" | tee -a "$REPORT_FILE"
type_text "teh"
press_key "KEYCODE_SPACE"
sleep 0.3
take_screenshot "07_before_undo"
press_key "KEYCODE_DEL"
sleep 0.3
take_screenshot "08_after_undo"
press_key "KEYCODE_ENTER"
echo "✓ Test 4 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 5: Common Contractions" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing common contractions" | tee -a "$REPORT_FILE"
type_text "cant"
press_key "KEYCODE_SPACE"
type_text "wont"
press_key "KEYCODE_SPACE"
type_text "didnt"
press_key "KEYCODE_SPACE"
take_screenshot "09_contractions"
press_key "KEYCODE_ENTER"
echo "✓ Test 5 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 6: Case Preservation" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing: 'Teh' (capitalized) should become 'The'" | tee -a "$REPORT_FILE"
# Capital T
adb shell input keyevent KEYCODE_SHIFT_LEFT
adb shell input keyevent KEYCODE_T
type_text "eh"
press_key "KEYCODE_SPACE"
sleep 0.3
take_screenshot "10_case_preservation"
press_key "KEYCODE_ENTER"
echo "✓ Test 6 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 7: Indonesian Words (Bilingual)" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing mixed English and Indonesian" | tee -a "$REPORT_FILE"
type_text "helo"
press_key "KEYCODE_SPACE"
type_text "apa"
press_key "KEYCODE_SPACE"
type_text "kabar"
press_key "KEYCODE_SPACE"
take_screenshot "11_bilingual"
press_key "KEYCODE_ENTER"
echo "✓ Test 7 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 8: No False Positives" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing: 'teh' (Indonesian for 'tea') in Indonesian context" | tee -a "$REPORT_FILE"
type_text "minum"
press_key "KEYCODE_SPACE"
type_text "teh"
press_key "KEYCODE_SPACE"
type_text "manis"
press_key "KEYCODE_SPACE"
take_screenshot "12_no_false_positive"
press_key "KEYCODE_ENTER"
echo "✓ Test 8 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 9: Long Words" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing longer word corrections" | tee -a "$REPORT_FILE"
type_text "beautifull"
press_key "KEYCODE_SPACE"
type_text "wonderfull"
press_key "KEYCODE_SPACE"
take_screenshot "13_long_words"
press_key "KEYCODE_ENTER"
echo "✓ Test 9 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

echo "========================================" | tee -a "$REPORT_FILE"
echo "TEST 10: Keyboard Proximity Typos" | tee -a "$REPORT_FILE"
echo "========================================" | tee -a "$REPORT_FILE"
echo "Testing nearby key typos (e.g., 'gello' -> 'hello')" | tee -a "$REPORT_FILE"
type_text "gello"
press_key "KEYCODE_SPACE"
type_text "wprld"
press_key "KEYCODE_SPACE"
take_screenshot "14_proximity_typos"
press_key "KEYCODE_ENTER"
echo "✓ Test 10 complete" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

take_screenshot "15_final"

echo "" | tee -a "$REPORT_FILE"
echo "=====================================" | tee -a "$REPORT_FILE"
echo "✅ All Tests Completed!" | tee -a "$REPORT_FILE"
echo "=====================================" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"
echo "📊 Test Summary:" | tee -a "$REPORT_FILE"
echo "  - 10 test scenarios executed" | tee -a "$REPORT_FILE"
echo "  - Screenshots saved to: $SCREENSHOT_DIR" | tee -a "$REPORT_FILE"
echo "  - Report saved to: $REPORT_FILE" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"
echo "📝 Next Steps:" | tee -a "$REPORT_FILE"
echo "  1. Review screenshots in $SCREENSHOT_DIR" | tee -a "$REPORT_FILE"
echo "  2. Verify autocorrections occurred as expected" | tee -a "$REPORT_FILE"
echo "  3. Check for any false positives or missed corrections" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

# Collect logcat for analysis
echo "📋 Collecting logcat logs..." | tee -a "$REPORT_FILE"
adb logcat -d | grep -E "(AutocorrectEngine|AutocorrectManager|DictionaryRepository)" > "$REPORT_DIR/logcat_$TIMESTAMP.txt" 2>&1 || true
echo "  Logcat saved to: $REPORT_DIR/logcat_$TIMESTAMP.txt" | tee -a "$REPORT_FILE"

echo "" | tee -a "$REPORT_FILE"
echo "🎉 Testing complete! Review the screenshots to verify autocorrect behavior." | tee -a "$REPORT_FILE"

# Display final screenshot locations
echo ""
echo "Screenshot files:"
ls -1 "$SCREENSHOT_DIR"
