# Local Maven Repository Test Configuration

# This file demonstrates how to switch sample-app to use 
# the published Maven artifact instead of project dependency

## Step 1: Publish SDK to local Maven
# Run: ./gradlew :grid-sdk:publishToMavenLocal

## Step 2: Modify settings.gradle.kts
# Add mavenLocal() to repositories

## Step 3: Modify sample-app/build.gradle.kts
# Replace: implementation(project(":grid-sdk"))
# With: implementation("com.android.gridsdk:grid-sdk:0.1.0")

## Step 4: Sync and build
# The sample-app will now use the published artifact

## Notes:
# - This setup allows testing the SDK as if it were a real dependency
# - Useful for verifying the published artifact works correctly
# - For development, continue using project(":grid-sdk") dependency
