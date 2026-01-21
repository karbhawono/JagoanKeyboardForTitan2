#!/bin/bash

echo "Testing suggestion bar..."

# Open Google Keep
adb shell am start -n com.google.android.keep/.activities.ShareReceiverActivity 2>/dev/null
sleep 2

# Type "teh" (should show suggestion for "the")
echo "Typing 'teh'..."
adb shell input text "teh"
sleep 1

# Take screenshot
adb shell screencap -p /sdcard/test_teh.png
adb pull /sdcard/test_teh.png test_reports/
adb shell rm /sdcard/test_teh.png

echo "Screenshot saved to test_reports/test_teh.png"
echo "Check if suggestion bar is showing above the text field"

# Clear
for i in {1..10}; do adb shell input keyevent KEYCODE_DEL; done
sleep 0.5

# Type "apa" (should NOT be autocorrected since it's valid Indonesian)
echo "Typing 'apa'..."
adb shell input text "apa"
sleep 1

# Take screenshot
adb shell screencap -p /sdcard/test_apa.png
adb pull /sdcard/test_apa.png test_reports/
adb shell rm /sdcard/test_apa.png

echo "Screenshot saved to test_reports/test_apa.png"
echo "Check that 'apa' is NOT showing suggestions (it's a valid word)"

