@echo off
setlocal
echo ================================================================================
echo   Building Algorithmic ASCII-Art Ray Caster
echo ================================================================================

if not exist "bin" mkdir bin

echo Compiling Java source files...
javac -d bin -encoding UTF-8 src\main\java\com\raycaster\*.java src\main\java\com\raycaster\config\*.java src\main\java\com\raycaster\core\*.java src\main\java\com\raycaster\engine\*.java src\main\java\com\raycaster\ui\*.java src\test\java\com\raycaster\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Packaging executable JAR...
jar cfe raycaster.jar com.raycaster.Main -C bin . >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] jar command not in PATH, compiling directly to bin/ folder (fully runnable with run.bat)
) else (
    echo [SUCCESS] Successfully generated raycaster.jar!
)

echo [SUCCESS] Compilation completed successfully into bin/ directory!
echo ================================================================================
pause

