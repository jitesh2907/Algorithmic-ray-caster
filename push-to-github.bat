@echo off
setlocal
title Algorithmic 3D Maze Explorer - GitHub Push Helper
echo ===============================================================================
echo     ALGORITHMIC 3D MAZE EXPLORER - GITHUB REPOSITORY SYNCHRONIZATION
echo ===============================================================================
echo Target Repository : https://github.com/jitesh2907/Algorithmic-ray-caster
echo Active Branch     : main
echo.

set "SCRIPT_DIR=%~dp0"
if exist "%SCRIPT_DIR%.git-bin\cmd\git.exe" (
    set "PATH=%SCRIPT_DIR%.git-bin\cmd;%PATH%"
)

echo Checking Git status...
git status -s
echo.

echo Initiating push to remote origin...
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===============================================================================
    echo SUCCESS: Project successfully pushed to GitHub!
    echo ===============================================================================
) else (
    echo.
    echo [NOTE] If prompted for authentication:
    echo 1. Browser Login: Authorize Git Credential Manager in your web browser.
    echo 2. Personal Access Token (PAT): Enter your GitHub username and PAT as password.
    echo.
)

pause
