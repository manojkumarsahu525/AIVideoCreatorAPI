#!/usr/bin/env bash
set -e

# Usage: ./build-release-aab.sh
# Place your keystore file and create a keystore.properties in project root (see keystore.properties.example)
# Then run this script from repository root.

cd AndroidClient

if [ ! -f "../keystore.properties" ]; then
  echo "keystore.properties not found in project root. Copy keystore.properties.example and fill values."
  exit 1
fi

./gradlew clean bundleRelease

echo "AAB built: check AndroidClient/app/build/outputs/bundle/release/"

exit 0
