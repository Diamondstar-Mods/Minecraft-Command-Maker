#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo "Running: ./gradlew :quilt-server:runClient"
./gradlew :quilt-server:runClient
