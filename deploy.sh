#!/bin/bash
# Chain Messaging - Deployment Script for Unix/Linux/macOS

set -e  # Exit on any error

echo "========================================"
echo "Chain Messaging Deployment Script"
echo "========================================"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "Error: gradlew not found. Please run from project root."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Set deployment environment
export DEPLOYMENT_ENV=production
export BUILD_TYPE=release

echo ""
echo "Starting deployment preparation..."
echo "Environment: $DEPLOYMENT_ENV"
echo "Build Type: $BUILD_TYPE"
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Run tests
echo "Running tests..."
./gradlew test

# Run lint checks
echo "Running lint checks..."
./gradlew lint || echo "Warning: Lint checks failed"

# Validate deployment
echo "Validating deployment configuration..."
./gradlew validateDeployment

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

# Build release AAB (Android App Bundle)
echo "Building release AAB..."
./gradlew bundleRelease

# Generate deployment report
echo "Generating deployment report..."
./gradlew generateDeploymentReport

echo ""
echo "========================================"
echo "Deployment completed successfully!"
echo "========================================"
echo ""
echo "Output files:"
echo "- APK: app/build/outputs/apk/release/app-release.apk"
echo "- AAB: app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "Next steps:"
echo "1. Test the release build on physical devices"
echo "2. Upload to Google Play Console"
echo "3. Configure release rollout"
echo "4. Monitor crash reports and user feedback"
echo ""