#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export ANDROID_SDK_ROOT="$ROOT_DIR/.android-sdk"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

AVD_NAME="SmartBook_API_35"
PACKAGE="com.cecar.smartbookmobile"
ACTIVITY=".MainActivity"

adb kill-server || true
adb start-server

if ! adb devices | grep -q "emulator-"; then
  nohup emulator -avd "$AVD_NAME" >/tmp/smartbook-emulator.log 2>&1 &
fi

adb wait-for-device
BOOTED=""
until [ "$BOOTED" = "1" ]; do
  BOOTED="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  sleep 2
done

./gradlew :app:installDebug >/tmp/smartbook-install.log 2>&1 || true
adb shell am start -n "$PACKAGE/$ACTIVITY"
