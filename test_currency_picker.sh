#!/bin/bash

echo "Testing currency from symbol picker with space..."

# Clear and start fresh
adb shell am force-stop ai.jagoan.keyboard.titan2.debug
sleep 1

# Open test activity
adb shell am start -n ai.jagoan.keyboard.titan2.debug/ai.jagoan.keyboard.titan2.ui.test.AutocorrectTestActivity
sleep 2

# Clear any existing text
adb shell input tap 540 600
sleep 1
adb shell input keyevent KEYCODE_CLEAR
sleep 0.5

# Type a simple test: Type '$', then 'test'
echo "Typing dollar sign and then 'test'..."
adb shell input text '\$'
sleep 0.5
adb shell input keyevent KEYCODE_T
sleep 0.2
adb shell input keyevent KEYCODE_E
sleep 0.2
adb shell input keyevent KEYCODE_S
sleep 0.2
adb shell input keyevent KEYCODE_T
sleep 1.5

# Take screenshot
adb shell screencap /sdcard/dollar_test.png
adb pull /sdcard/dollar_test.png test_reports/dollar_test.png

echo "✓ Screenshot saved"
echo "Check if there's a space between $ and test"
