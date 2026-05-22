#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

check_bin() {
  local name="$1"
  if command -v "$name" >/dev/null 2>&1; then
    printf "[ok] %s -> %s\n" "$name" "$(command -v "$name")"
  else
    printf "[missing] %s\n" "$name"
  fi
}

echo "Checking local toolchain for Overlay MR"
check_bin node
check_bin npm
check_bin java
check_bin javac
check_bin gradle
check_bin adb
check_bin gcloud

echo
echo "Checking project files"

required_files=(
  "$ROOT_DIR/mobile/package.json"
  "$ROOT_DIR/mobile/android/app/build.gradle"
  "$ROOT_DIR/backend/package.json"
)

for file in "${required_files[@]}"; do
  if [[ -f "$file" ]]; then
    printf "[ok] %s\n" "$file"
  else
    printf "[missing] %s\n" "$file"
  fi
done

echo
echo "Checking model asset folders"
asset_dirs=(
  "$ROOT_DIR/mobile/android/app/src/main/assets/models"
  "$ROOT_DIR/mobile/android/app/src/main/assets/mediapipe"
)

for dir in "${asset_dirs[@]}"; do
  if [[ -d "$dir" ]]; then
    printf "[ok] %s\n" "$dir"
  else
    printf "[missing] %s\n" "$dir"
  fi
done
