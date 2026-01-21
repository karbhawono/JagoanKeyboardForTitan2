#!/bin/bash
# Quick test with live logging

echo "Starting quick test..."

# Open Google Keep
adb shell am start -n com.google.android.keep/.activities.ShareReceiverActivity 2>/dev/null || \
    adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.google.android.keep/.activities.MainActivity 2>/dev/null
sleep 2

# Type "teh " (should autocorrect to "the ")
echo "Typing 'teh' + space..."
adb shell input text "teh"
sleep 0.5
adb shell input keyevent KEYCODE_SPACE
sleep 1

echo "Done. Checking logs..."
adb logcat -d | grep -E "(AutocorrectManager|AutocorrectEngine|DictionaryRepository|Titan2IME)" | tail -50
