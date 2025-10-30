@echo off
echo ========================================
echo Chain Messaging App - Comprehensive Test Suite
echo ========================================
echo.

echo Starting comprehensive test execution...
echo This will run all test suites including:
echo - End-to-End Message Flow Tests
echo - Blockchain and P2P Integration Tests  
echo - Security and Encryption Tests
echo - Load and Performance Tests
echo.

echo Setting up test environment...
call gradlew clean
if %ERRORLEVEL% neq 0 (
    echo Failed to clean project
    exit /b 1
)

echo.
echo Running comprehensive test suite...
call gradlew test --tests "com.chain.messaging.ComprehensiveTestRunner" --info
if %ERRORLEVEL% neq 0 (
    echo Comprehensive tests failed
    exit /b 1
)

echo.
echo Running individual test suites for detailed reporting...

echo.
echo [1/4] Running End-to-End Tests...
call gradlew test --tests "com.chain.messaging.e2e.*" --info

echo.
echo [2/4] Running Integration Tests...
call gradlew test --tests "com.chain.messaging.integration.BlockchainP2PIntegrationTest" --info

echo.
echo [3/4] Running Security Tests...
call gradlew test --tests "com.chain.messaging.security.*" --info

echo.
echo [4/4] Running Load Tests...
call gradlew test --tests "com.chain.messaging.performance.*" --info

echo.
echo ========================================
echo Comprehensive Test Suite Complete
echo ========================================
echo.
echo Test reports are available in:
echo - app/build/reports/tests/test/index.html
echo.
echo Check the console output above for detailed results
echo and any failures that need to be addressed.
echo.
pause