# Telemetry System Documentation

## Overview
This telemetry system allows the Minecraft Command Maker mod to send usage data to a centralized server, tracking which machines have the client and server editions installed.

## Components

### 1. Java Telemetry Manager (`TelemetryManager.java`)
- Located in all three editions (shared src, server, client)
- Sends a POST request to the telemetry server when the mod initializes
- Automatically detects and sends the sender's IP address
- Runs asynchronously to avoid blocking mod initialization

**Features:**
- Automatic IP detection (uses checkip.amazonaws.com as primary method, falls back to local hostname)
- Non-blocking execution (runs in separate thread)
- Error handling and logging

### 2. Python Telemetry Server (`telemetry_server.py`)
- Flask-based HTTP server running on port 5000
- Listens for POST requests from the mod
- Logs IP addresses to separate files for client and server editions

**Features:**
- Stores client edition IPs in `client_ips.txt`
- Stores server edition IPs in `server_ips.txt`
- Prevents duplicate IPs (each IP logged only once)
- Health check endpoint (`/health`)
- Statistics endpoint (`/stats`)

## Setup Instructions

### Java Mod Setup
The telemetry code is already integrated into the mod. When either the client or server edition starts with the mod enabled, it will automatically send a telemetry request.

### Python Server Setup

1. **Install Dependencies:**
   ```bash
   pip install -r telemetry_requirements.txt
   ```

2. **Run the Server:**
   ```bash
   python telemetry_server.py
   ```

   The server will start listening on `http://0.0.0.0:5000`

3. **Expected Output:**
   - Server logs will show: "Starting Telemetry Server on 0.0.0.0:5000"
   - Client and server IPs will be logged to respective .txt files
   - One IP per line in each file

## API Endpoints

### POST `/telemetry`
Receives telemetry data from the mod.

**Request Body:**
```json
{
    "edition": "client" or "server",
    "ip": "192.168.1.100"
}
```

**Response (Success):**
```json
{
    "status": "success",
    "message": "Telemetry received for client edition",
    "ip": "192.168.1.100",
    "edition": "client",
    "timestamp": "2024-01-26T10:30:45.123456"
}
```

### GET `/health`
Health check endpoint.

**Response:**
```json
{
    "status": "healthy"
}
```

### GET `/stats`
Get statistics about logged IPs.

**Response:**
```json
{
    "client_ips_count": 5,
    "server_ips_count": 3,
    "total_ips": 8
}
```

## Data Files

### `client_ips.txt`
Contains list of client edition machine IPs, one per line:
```
192.168.1.100
10.0.0.50
203.45.67.89
```

### `server_ips.txt`
Contains list of server edition machine IPs, one per line:
```
192.168.0.1
172.16.0.1
```

## Configuration

### Server Configuration
- **Host:** 0.0.0.0 (listen on all interfaces)
- **Port:** 5000
- **Debug Mode:** Disabled in production

### Mod Configuration
- **Telemetry Server URL:** http://92.239.83.83:5000/telemetry
- **Edition Labels:** "client" or "server"
- **Timeout:** Default (system-dependent, usually 30 seconds)

## Troubleshooting

### Mod not sending telemetry
1. Check network connectivity
2. Verify the IP address 92.239.83.83:5000 is accessible
3. Check mod logs for "Telemetry sent successfully" or error messages
4. Ensure TelemetryManager.java is compiled and included in the mod JAR

### Server not receiving requests
1. Verify server is running on port 5000
2. Check firewall allows port 5000
3. Verify mod can resolve 92.239.83.83
4. Check server logs for connection attempts

### Duplicate IPs
The server automatically prevents duplicate IPs from being logged multiple times.

## Privacy Considerations
- Only IP addresses are collected
- No personal data is stored
- IPs are stored in plain text files
- Consider securing these files appropriately in production

## Logging
- Java: Uses SLF4J logger (logs to game console and logs/)
- Python: Uses Python's logging module (logs to console)

Both systems include detailed logging for debugging and monitoring purposes.
