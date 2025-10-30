@echo off
REM Chain Messaging - Deployment Script for Windows

echo ========================================
echo Chain Messaging Deployment Script
echo ========================================

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found. Please run from project root.
    exit /b 1
)

REM Set deployment environment
set DEPLOYMENT_ENV=production
set BUILD_TYPE=release

echo.
echo Starting deployment preparation...
echo Environment: %DEPLOYMENT_ENV%
echo Build Type: %BUILD_TYPE%
echo.

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean
if %ERRORLEVEL% neq 0 (
    echo Error: Clean failed
    exit /b 1
)

REM Run tests
echo Running tests...
call gradlew.bat test
if %ERRORLEVEL% neq 0 (
    echo Error: Tests failed
    exit /b 1
)

REM Run lint checks
echo Running lint checks...
call gradlew.bat lint
if %ERRORLEVEL% neq 0 (
    echo Warning: Lint checks failed
)

REM Validate deployment
echo Validating deployment configuration...
call gradlew.bat validateDeployment
if %ERRORLEVEL% neq 0 (
    echo Error: Deployment validation failed
    exit /b 1
)

REM Build release APK
echo Building release APK...
call gradlew.bat assembleRelease
if %ERRORLEVEL% neq 0 (
    echo Error: Release build failed
    exit /b 1
)

REM Build release AAB (Android App Bundle)
echo Building release AAB...
call gradlew.bat bundleRelease
if %ERRORLEVEL% neq 0 (
    echo Error: Bundle build failed
    exit /b 1
)

REM Generate deployment report
echo Generating deployment report...
call gradlew.bat generateDeploymentReport

echo.
echo ========================================
echo Deployment completed successfully!
echo ========================================
echo.
echo Output files:
echo - APK: app\build\outputs\apk\release\app-release.apk
echo - AAB: app\build\outputs\bundle\release\app-release.aab
echo.
echo Next steps:
echo 1. Test the release build on physical devices
echo 2. Upload to Google Play Console
echo 3. Configure release rollout
echo 4. Monitor crash reports and user feedback
echo.

pause