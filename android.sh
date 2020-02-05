#! /bin/bash
# Copyright 2018-2020 Twitter, Inc.
# Licensed under the MoPub SDK License Agreement
# http://www.mopub.com/legal/sdk-license-agreement/


# Run with: [mopub-android]$ chmod +x android.sh && ./android.sh
sdk_manager_dir="$HOME/Library/Android/sdk/tools/bin"
avd_manager_dir="$HOME/Library/Android/sdk/tools/bin"
device_emulator_dir="$HOME/Library/Android/sdk/emulator"
adb_dir="$HOME/Library/Android/sdk/platform-tools"
mopub_sample_dir="$(pwd)/mopub-sample"
gradle_dir="$HOME/.gradle/wrapper/dists/gradle-5.4.1-bin/*/gradle-5.4.1/bin"

# Generate Gradle wrapper files
cd $mopub_sample_dir
$gradle_dir/gradle wrapper

# Kill any running emulator
cd $adb_dir
./adb emu kill
./adb devices | grep "emulator-" | while read -r emulator device; do ./adb -s $emulator emu kill; done

echo "list of all Running emulators/Devices"
$adb_dir/adb devices

# Create new AVD (Android Virtual Device):
cd $avd_manager_dir
(echo "no") | ./avdmanager create avd -n testavd -k "system-images;android-29;google_apis;x86" --device "pixel_xl" --sdcard 128M --force

# Start an emulator/device from an AVD: [in the background]
cd $device_emulator_dir
./emulator -avd testavd -no-boot-anim &

# Wait for emulator/device to start
while [ "`$adb_dir/adb shell getprop sys.boot_completed | tr -d '\r' `" != "1" ] ; do echo "Waiting for emulator to boot"; sleep 15; done

echo "list of all Running emulators/Devices"
$adb_dir/adb devices

# ************ Set up Environment, Install APKs, Run tests, Uninstall APKs, Kill emulator ***************

# Give Gradle execution permission:
cd $mopub_sample_dir
chmod +x gradlew

# Create APK for mopub-sample app:
./gradlew assembleDebug --stacktrace

# Create test APK for mopub-sample app:
./gradlew assembleAndroidTest --stacktrace

# Install mopub-sample app APK: [Grant Permissions]
cd $adb_dir
./adb push $mopub_sample_dir/build/outputs/apk/external/debug/mopub-sample-external-debug.apk /data/local/tmp/com.mopub.simpleadsdemo
./adb shell pm install -g -t -r "/data/local/tmp/com.mopub.simpleadsdemo"

# Install test APK: [Grant Permissions]
./adb push $mopub_sample_dir/build/outputs/apk/androidTest/external/debug/mopub-sample-external-debug-androidTest.apk /data/local/tmp/com.mopub.simpleadsdemo.test
./adb shell pm install -g -t -r "/data/local/tmp/com.mopub.simpleadsdemo.test"

# Enable fullscreen mode:
./adb shell settings put global policy_control immersive.full=com.mopub.simpleadsdemo

# Run All Tests
echo "Test run starts"
test_report=$(./adb shell am instrument -w -r   -e package com.mopub.tests -e debug false com.mopub.simpleadsdemo.test/androidx.test.runner.AndroidJUnitRunner)

# Show test report
echo "$test_report"

# Clean-up: Uninstall mopub-sample app, test app
./adb uninstall com.mopub.simpleadsdemo
./adb uninstall com.mopub.simpleadsdemo.test

# Clean-up: Kill any running emulator
cd $adb_dir
./adb emu kill
./adb devices | grep "emulator-" | while read -r emulator device; do ./adb -s $emulator emu kill; done

echo "list of all Running emulators/Devices"
$adb_dir/adb devices

# Terminate shell with appropriate exit code
exitcmd=$(if echo "$test_report" | grep -q 'FAILURES!!!'; then echo "exit 1"; else echo "exit 0"; fi)
echo "Exiting terminal with command: $exitcmd"
$exitcmd
