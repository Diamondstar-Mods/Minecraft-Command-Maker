# Telemetry System - Visual Guide

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│           MINECRAFT MOD (Client & Server Editions)          │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ExampleMod.onInitialize() / CMDMakerClient.onInit() │  │
│  │           (Runs when mod loads)                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │     TelemetryManager.sendTelemetry("server/client")  │  │
│  │     - Detects local IP address                       │  │
│  │     - Creates JSON: {"edition":"...", "ip":"..."}   │  │
│  │     - Runs in background thread                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                 │
│      POST HTTP Request to 92.239.83.83:5000/telemetry      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│         PYTHON TELEMETRY SERVER (Port 5000)                 │
│                  (telemetry_server.py)                      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │    Flask HTTP Server @ 0.0.0.0:5000                  │  │
│  │                                                      │  │
│  │  POST /telemetry       → Receive & Log IPs          │  │
│  │  GET  /health          → Health Check               │  │
│  │  GET  /stats           → Show IP Statistics         │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  client_ips.txt    │   server_ips.txt               │  │
│  │  ───────────────   │   ────────────────             │  │
│  │  192.168.1.100     │   10.0.0.1                     │  │
│  │  10.0.0.50         │   172.16.0.1                   │  │
│  │  203.45.67.89      │   (one IP per line)            │  │
│  │  (one IP per line) │                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

```
START MOD
   │
   ├─→ ExampleMod.onInitialize() [SERVER]
   │   └─→ TelemetryManager.sendTelemetry("server")
   │       └─→ HTTP POST {"edition":"server", "ip":"x.x.x.x"}
   │
   ├─→ CMDMakerClient.onInitializeClient() [CLIENT]
   │   └─→ TelemetryManager.sendTelemetry("client")
   │       └─→ HTTP POST {"edition":"client", "ip":"x.x.x.x"}
   │
   └─→ Telemetry Server receives request
       ├─→ Validate JSON data
       ├─→ Determine edition type
       ├─→ Log IP to appropriate file
       └─→ Return 200 OK response
```

## IP Detection Flow

```
TelemetryManager.getLocalIP()
  │
  ├─→ Try: Connect to checkip.amazonaws.com
  │   ├─→ Success: Return public IP
  │   └─→ Fail: Try fallback
  │
  ├─→ Try: InetAddress.getLocalHost().getHostAddress()
  │   ├─→ Success: Return local IP
  │   └─→ Fail: Return "unknown"
  │
  └─→ Return IP address to sendTelemetry()
```

## File Organization

```
Minecraft-Command-Maker/
│
├── telemetry_server.py          ← Main Python server (run this!)
├── telemetry_requirements.txt    ← Python dependencies
├── test_telemetry_server.py      ← Test script
├── runTelemetryServer.bat        ← Windows start script
├── runTelemetryServer.sh         ← Linux/macOS start script
│
├── TELEMETRY.md                  ← Technical documentation
├── TELEMETRY_SETUP.md            ← Setup guide
├── TELEMETRY_IMPLEMENTATION.md   ← Implementation summary
│
├── client_ips.txt                ← Output: Client IPs (created by server)
├── server_ips.txt                ← Output: Server IPs (created by server)
│
├── server/
│   └── src/main/java/com/example/
│       ├── ExampleMod.java       ← Modified (added telemetry call)
│       └── TelemetryManager.java  ← New file
│
└── client/
    └── src/main/java/com/example/
        ├── CMDMakerClient.java    ← Modified (added telemetry call)
        └── TelemetryManager.java  ← New file
```

## Request/Response Format

### REQUEST (Mod → Server)
```
POST /telemetry HTTP/1.1
Host: 92.239.83.83:5000
Content-Type: application/json
Content-Length: 48

{
  "edition": "server",
  "ip": "192.168.1.100"
}
```

### RESPONSE (Server → Mod)
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 156

{
  "status": "success",
  "message": "Telemetry received for server edition",
  "ip": "192.168.1.100",
  "edition": "server",
  "timestamp": "2024-01-26T10:30:45.123456"
}
```

## Setup Timeline

```
Step 1: Install Dependencies
        pip install -r telemetry_requirements.txt
        └─→ Installs Flask, Werkzeug, requests

Step 2: Start Server
        python3 telemetry_server.py
        └─→ Server listening on 0.0.0.0:5000

Step 3: Build & Run Mod
        gradle build && run server/client
        └─→ Mod sends telemetry on startup

Step 4: Monitor Results
        cat client_ips.txt
        cat server_ips.txt
        └─→ View collected IPs
```

## Key Files Checklist

### New Files Created ✓
- [ ] telemetry_server.py
- [ ] telemetry_requirements.txt
- [ ] test_telemetry_server.py
- [ ] runTelemetryServer.bat
- [ ] runTelemetryServer.sh
- [ ] TELEMETRY.md
- [ ] TELEMETRY_SETUP.md
- [ ] TELEMETRY_IMPLEMENTATION.md

### Modified Files ✓
- [ ] server/src/main/java/com/example/ExampleMod.java
- [ ] client/src/main/java/com/example/CMDMakerClient.java

### New Java Files ✓
- [ ] server/src/main/java/com/example/TelemetryManager.java
- [ ] client/src/main/java/com/example/TelemetryManager.java

## Testing Endpoints

### Health Check
```bash
curl http://localhost:5000/health
```
Response: `{"status":"healthy"}`

### Check Stats
```bash
curl http://localhost:5000/stats
```
Response:
```json
{
  "client_ips_count": 2,
  "server_ips_count": 1,
  "total_ips": 3
}
```

### Manual Telemetry Test
```bash
curl -X POST http://localhost:5000/telemetry \
  -H "Content-Type: application/json" \
  -d '{"edition":"client","ip":"192.168.1.1"}'
```

## Output Example

### Terminal Output (Server Running)
```
Starting Telemetry Server on 0.0.0.0:5000
2024-01-26 10:15:30,123 - INFO - Created client_ips.txt
2024-01-26 10:15:30,124 - INFO - Created server_ips.txt
2024-01-26 10:16:45,567 - INFO - Logged server IP: 192.168.1.100 to server_ips.txt
2024-01-26 10:16:50,234 - INFO - Telemetry received - Edition: server, IP: 192.168.1.100
2024-01-26 10:17:15,890 - INFO - Logged client IP: 10.0.0.50 to client_ips.txt
2024-01-26 10:17:16,123 - INFO - Telemetry received - Edition: client, IP: 10.0.0.50
```

### File Output

**client_ips.txt:**
```
10.0.0.50
203.45.67.89
192.168.1.99
```

**server_ips.txt:**
```
192.168.1.100
172.16.0.1
```

---

**For detailed information, see the documentation files included in this project.**
