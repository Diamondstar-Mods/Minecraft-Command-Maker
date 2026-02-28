@echo off
cd ..
REM Telemetry Server Start Script for Windows
REM This script starts the Python telemetry server on port 5000

echo Starting Minecraft Command Maker Telemetry Server...
echo.
echo Installing dependencies with: pip install -r telemetry_requirements.txt
pip install -r telemetry_requirements.txt
echo.

python telemetry_server.py

if %errorlevel% neq 0 (
    echo.
    echo Error: Failed to start telemetry server
    echo Make sure Python is installed and in your PATH
    echo.
    pause
)
