@echo off
setlocal
title Algorithmic ASCII-Art Ray Caster (3D)
chcp 65001 >nul 2>&1
echo ================================================================================
echo   Launching Algorithmic ASCII-Art Ray Caster
echo ================================================================================

if exist "raycaster.jar" (
    java -Dfile.encoding=UTF-8 -jar raycaster.jar %*
) else (
    if not exist "bin" (
        echo [INFO] Binaries not found. Invoking build.bat first...
        call build.bat
    )
    java -Dfile.encoding=UTF-8 -cp bin com.raycaster.Main %*
)

echo.
pause
