#!/usr/bin/env bash
set -euo pipefail

echo "This script does not install Android SDK/JDK automatically."
echo "It documents the expected command-line setup for your local machine."
echo
echo "Required tools:"
echo "- JDK 17"
echo "- Android SDK Platform 34"
echo "- Android Build Tools 34.x"
echo "- Android NDK 26"
echo "- platform-tools (adb)"
echo "- Gradle"
echo
echo "After installing them locally, re-run:"
echo "  ./scripts/check-env.sh"
echo "  ./scripts/build-apk.sh"
