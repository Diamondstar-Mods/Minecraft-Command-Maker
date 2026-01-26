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
