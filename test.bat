@echo off
setlocal
chcp 65001 >nul 2>&1
echo ================================================================================
echo   Running Algorithmic ASCII-Art Ray Caster Test Suite
echo ================================================================================

if not exist "bin" (
    echo [INFO] Binaries not found. Building project...
    call build.bat
)

java -Dfile.encoding=UTF-8 -cp bin com.raycaster.StandaloneTestRunner
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Automated test suite reported failures!
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] All verification tests passed!
pause
