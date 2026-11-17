@echo off
setlocal
echo ================================================================================
echo   Building ALGORITHMIC 3D MAZE EXPLORER
echo ================================================================================

if not exist "bin" mkdir bin

echo Compiling Java 17+ 3D source files...
javac -cp "lib/*" -d bin -encoding UTF-8 src\main\java\com\mazeexplorer\*.java src\main\java\com\mazeexplorer\config\*.java src\main\java\com\mazeexplorer\state\*.java src\main\java\com\mazeexplorer\maze\*.java src\main\java\com\mazeexplorer\builder\*.java src\main\java\com\mazeexplorer\camera\*.java src\main\java\com\mazeexplorer\collision\*.java src\main\java\com\mazeexplorer\player\*.java src\main\java\com\mazeexplorer\raycast\*.java src\main\java\com\mazeexplorer\pathfinding\*.java src\main\java\com\mazeexplorer\ai\*.java src\main\java\com\mazeexplorer\entity\*.java src\main\java\com\mazeexplorer\stats\*.java src\main\java\com\mazeexplorer\ui\hud\*.java src\main\java\com\mazeexplorer\ui\menu\*.java src\test\java\com\mazeexplorer\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Re-running compilation with full file-tree traversal...
    powershell -Command "javac -cp 'lib/*' -d bin -encoding UTF-8 (Get-ChildItem -Path src/main/java, src/test/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)"
)

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] 3D Engine compilation completed successfully into bin/ directory!
echo ================================================================================

