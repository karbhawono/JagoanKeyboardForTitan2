#!/bin/bash

echo "Testing currency insertion with space..."

# Open test activity
adb shell am start -n ai.jagoan.keyboard.titan2.debug/ai.jagoan.keyboard.titan2.ui.test.AutocorrectTestActivity
sleep 2

# Tap on text field
adb shell input tap 540 600
sleep 1

# Test 1: SYM+C+I+D (Indonesia Rupiah)
echo "Test 1: SYM+C+ID → Rp "
adb shell input keyevent KEYCODE_SYM      # Press SYM
adb shell input keyevent KEYCODE_C        # Press C
sleep 0.2
adb shell input keyevent KEYCODE_I        # Press I
sleep 0.2
adb shell input keyevent KEYCODE_D        # Press D
sleep 1

# Now type a word after the currency
echo "Typing 'test' after currency..."
adb shell input keyevent KEYCODE_T
sleep 0.2
adb shell input keyevent KEYCODE_E
sleep 0.2
adb shell input keyevent KEYCODE_S
sleep 0.2
adb shell input keyevent KEYCODE_T
sleep 2

# Take screenshot
adb shell screencap /sdcard/currency_test.png
adb pull /sdcard/currency_test.png test_reports/currency_test.png

echo "✓ Screenshot saved to test_reports/currency_test.png"
echo "Expected: 'Rp test' (with space between Rp and test)"
