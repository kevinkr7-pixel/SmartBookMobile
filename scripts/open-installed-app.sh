#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export ANDROID_SDK_ROOT="$ROOT_DIR/.android-sdk"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"

adb start-server
adb devices | grep -q "emulator-" || { echo "No hay emulador conectado."; exit 1; }
adb shell am start -n co.edu.cecar.smartbookmobile/.MainActivity
