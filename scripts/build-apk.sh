#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MOBILE_DIR="$ROOT_DIR/mobile"
ANDROID_DIR="$MOBILE_DIR/android"

if ! command -v node >/dev/null 2>&1; then
  echo "node is required"
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm is required"
  exit 1
fi

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle is required"
  exit 1
fi

cd "$MOBILE_DIR"

if [[ ! -d node_modules ]]; then
  echo "Installing mobile dependencies"
  npm install
fi

cd "$ANDROID_DIR"
gradle assembleDebug

echo
echo "APK generated at:"
echo "$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
