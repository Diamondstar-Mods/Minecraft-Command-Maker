#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo "Building CMDMaker Server version..."
mkdir -p CompiledFiles

./gradlew :server:build
cp server/build/libs/* CompiledFiles/ 2>/dev/null || true

./gradlew :quilt-server:build
cp quilt-server/build/libs/* CompiledFiles/ 2>/dev/null || true

echo "Building CMDMaker Client version..."
./gradlew :client:build
cp client/build/libs/* CompiledFiles/ 2>/dev/null || true

./gradlew :quilt-client:build
cp quilt-client/build/libs/* CompiledFiles/ 2>/dev/null || true

echo "Copying resources..." 
mkdir -p docs/cdn/files
cp CompiledFiles/* docs/cdn/files/ 2>/dev/null || true

echo "Build complete!"
