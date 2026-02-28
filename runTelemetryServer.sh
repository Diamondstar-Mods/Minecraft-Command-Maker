#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo "Starting Minecraft Command Maker Telemetry Server..."
echo

# Choose pip
if command -v pip3 >/dev/null 2>&1; then
  PIP=pip3
elif command -v pip >/dev/null 2>&1; then
  PIP=pip
else
  echo "Error: pip not found. Install Python and pip." >&2
  exit 1
fi

echo "Installing dependencies with: $PIP install -r telemetry_requirements.txt"
$PIP install -r telemetry_requirements.txt
echo

# Choose python
if command -v python3 >/dev/null 2>&1; then
  PY=python3
elif command -v python >/dev/null 2>&1; then
  PY=python
else
  echo "Error: Python not found. Install Python." >&2
  exit 1
fi

echo "Starting telemetry_server.py with $PY"
"$PY" telemetry_server.py
#!/bin/bash
# Telemetry Server Start Script for Linux/macOS
# This script starts the Python telemetry server on port 5000

echo "Starting Minecraft Command Maker Telemetry Server..."
echo ""
echo "Install dependencies with: pip install -r telemetry_requirements.txt"
echo ""

python3 telemetry_server.py

if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Failed to start telemetry server"
    echo "Make sure Python 3 is installed and in your PATH"
    echo ""
fi
