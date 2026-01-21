#!/bin/bash

# Open Google Keep
adb shell am start -n com.google.android.keep/.activities.ShareReceiverActivity 2>/dev/null
sleep 2

# Press SYM key
echo "Pressing SYM key..."
adb shell input keyevent KEYCODE_SYM
sleep 1

# Take screenshot
adb shell screencap -p /sdcard/test_sym.png
adb pull /sdcard/test_sym.png test_reports/
adb shell rm /sdcard/test_sym.png

echo "Screenshot saved. Check if symbol picker appeared."

# Get logs
adb logcat -d | grep -i "sym\|symbol" | tail -20

