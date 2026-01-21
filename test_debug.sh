#!/bin/bash

echo "Testing with logs..."

# Open Google Keep
adb shell am start -n com.google.android.keep/.activities.ShareReceiverActivity 2>/dev/null
sleep 2

# Type "teh"
echo "Typing 'teh'..."
adb shell input text "teh"
sleep 1

# Press space
echo "Pressing space..."
adb shell input keyevent KEYCODE_SPACE
sleep 1

# Get logs
echo "=== LOGS ==="
adb logcat -d | grep -E "(AutocorrectEngine|AutocorrectManager|DictionaryRepository|Titan2IME)" | tail -50

