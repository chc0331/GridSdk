#!/bin/bash
# GridSdk Build Verification Script

set -e

echo "==================================="
echo "GridSdk Build Verification"
echo "==================================="

echo ""
echo "1. Building grid-sdk debug..."
./gradlew :grid-sdk:assembleDebug --console=plain

echo ""
echo "2. Building grid-sdk release..."
./gradlew :grid-sdk:assembleRelease --console=plain

echo ""
echo "3. Checking AAR outputs..."
if [ -f "grid-sdk/build/outputs/aar/grid-sdk-debug.aar" ]; then
    echo "✓ Debug AAR found"
    ls -lh grid-sdk/build/outputs/aar/grid-sdk-debug.aar
else
    echo "✗ Debug AAR not found"
    exit 1
fi

if [ -f "grid-sdk/build/outputs/aar/grid-sdk-release.aar" ]; then
    echo "✓ Release AAR found"
    ls -lh grid-sdk/build/outputs/aar/grid-sdk-release.aar
else
    echo "✗ Release AAR not found"
    exit 1
fi

echo ""
echo "4. Publishing to local Maven repository..."
./gradlew :grid-sdk:publishToMavenLocal --console=plain

echo ""
echo "5. Checking Maven local repository..."
MAVEN_LOCAL=~/.m2/repository/com/android/gridsdk/grid-sdk/0.1.0
if [ -d "$MAVEN_LOCAL" ]; then
    echo "✓ Published to Maven Local"
    ls -lh "$MAVEN_LOCAL"
else
    echo "✗ Maven Local publication not found"
    exit 1
fi

echo ""
echo "==================================="
echo "✓ All build verifications passed!"
echo "==================================="
