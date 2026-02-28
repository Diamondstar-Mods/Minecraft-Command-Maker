#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Running: ./gradlew :quilt-server:runClient"
./gradlew :quilt-server:runClient
