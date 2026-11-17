@echo off
setlocal
title Algorithmic 3D Maze Explorer (Hardware 3D Engine)
echo ================================================================================
echo   Launching ALGORITHMIC 3D MAZE EXPLORER
echo   Powered by jMonkeyEngine 3.9.0-stable (LWJGL3 OpenGL Desktop Renderer)
echo ================================================================================

if not exist "bin\com\mazeexplorer\MazeExplorerApp.class" (
    echo [INFO] Binaries not found. Invoking build-3d.bat...
    call build-3d.bat
)

echo Starting 3D Graphics Engine...
java -Dfile.encoding=UTF-8 -cp "bin;lib/*" com.mazeexplorer.MazeExplorerApp %*

echo.
echo Application terminated.
pause

