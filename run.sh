#!/usr/bin/env bash
set -e

if [ -f "raycaster.jar" ]; then
    java -Dfile.encoding=UTF-8 -jar raycaster.jar "$@"
else
    if [ ! -d "bin" ]; then
        echo "[INFO] Binaries not found. Building project..."
        ./build.sh
    fi
    java -Dfile.encoding=UTF-8 -cp bin com.raycaster.Main "$@"
fi
