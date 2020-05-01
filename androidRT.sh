#! /bin/bash
# Copyright 2018-2020 Twitter, Inc.
# Licensed under the MoPub SDK License Agreement
# http://www.mopub.com/legal/sdk-license-agreement/


# Run with: [mopub-android]$ chmod +x androidRT.sh && ./androidRT.sh
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

# Emulator Boot limit retry logic
i=3
# Wait for emulator/device to start. While validating for getprops == 1. Wait for 20 seconds, and retry again. If retrys > 3. Echo failure and exit 1
while [ "`$adb_dir/adb shell getprop sys.boot_completed | tr -d '\r' `" != "1" ] ; do echo "Waiting for emulator to boot";if [ $i -eq 0 ]; then echo "Boot failure exit 1" && exit 1; fi ; let "i-=1" ; sleep 20; done

echo "list of all Running emulators/Devices"
$adb_dir/adb devices

# # ************ Set up Environment, Install APKs, Run tests, Uninstall APKs, Kill emulator ***************

# Give Gradle execution permission:
cd $mopub_sample_dir
chmod +x gradlew

echo "clean gradle"
./gradlew clean

echo "assemble Debug"
# Create APK for mopub-sample app:
./gradlew assembleDebug --stacktrace

echo "assemble android test"
# Create test APK for mopub-sample app:
./gradlew assembleAndroidTest --stacktrace

echo "push internal apk app"
# Install mopub-sample app APK: [Grant Permissions]
cd $adb_dir
./adb install -r -t -g $mopub_sample_dir/build/outputs/apk/internal/debug/mopub-sample-internal-debug.apk 

echo "push internal android test"
# Install test APK: [Grant Permissions]
./adb install -r -t -g $mopub_sample_dir/build/outputs/apk/androidTest/internal/debug/mopub-sample-internal-debug-androidTest.apk 

# Enable fullscreen mode:
./adb shell settings put global policy_control immersive.full=com.mopub.simpleadsdemo

# Run All Tests
echo "Test run starts"
test_report=$(./adb shell am instrument -w -r -e package com.mopub.tests.ReleaseTesting -e debug false com.mopub.simpleadsdemo.test/androidx.test.runner.AndroidJUnitRunner)

# Show test report
echo "$test_report"

# Clean-up: Uninstall mopub-sample app, test app
./adb uninstall com.mopub.simpleadsdemo
./adb uninstall com.mopub.simpleadsdemo.test

# Clean-up: Kill any running emulator
cd $adb_dir
./adb emu kill
./adb devices | grep "emulator-" | while read -r emulator device; do ./adb -s $emulator emu kill; done

# Kill server after the emulators
./adb kill-server

# Executing ./adb devices makes the daemon start again.
echo "list Process/devices on port 5037"
lsof -i tcp:5037

# Terminate shell with appropriate exit code
exitcmd=$(if echo "$test_report" | grep -q 'FAILURES!!!'; then echo "exit 1"; else echo "exit 0"; fi)
echo "Exiting terminal with command: $exitcmd"
$exitcmd
