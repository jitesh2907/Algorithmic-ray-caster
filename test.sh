#!/usr/bin/env bash
set -e

if [ ! -d "bin" ]; then
    echo "[INFO] Binaries not found. Building project..."
    ./build.sh
fi

java -Dfile.encoding=UTF-8 -cp bin com.raycaster.StandaloneTestRunner
