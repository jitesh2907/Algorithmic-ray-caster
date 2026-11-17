@echo off
setlocal
echo ================================================================================
echo   Running ALGORITHMIC 3D MAZE EXPLORER Automated Test Suite
echo ================================================================================

if not exist "bin\com\mazeexplorer\TestRunner3D.class" (
    call build-3d.bat
)

java -cp "bin;lib/*" com.mazeexplorer.TestRunner3D
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test suite reported failures!
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] All verification tests passed!


