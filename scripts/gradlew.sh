#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -x "./gradlew" ]; then
  exec ./gradlew "$@"
else
  echo "Unix gradle wrapper './gradlew' not found or not executable. Please run from project root." >&2
  exit 1
fi
