# Telemetry System - Quick Reference

## 🎯 What This Does

When the Minecraft mod (client or server edition) starts, it automatically sends your machine's IP address to a Python server running on port 5000. The server logs these IPs to separate files:
- `client_ips.txt` - for client edition IPs
- `server_ips.txt` - for server edition IPs

## 📦 Files Overview

| File | Purpose |
|------|---------|
| `telemetry_server.py` | Main Python server - RUN THIS FIRST |
| `telemetry_requirements.txt` | Python dependencies |
| `test_telemetry_server.py` | Test script to verify server works |
| `runTelemetryServer.bat` | Windows launcher |
| `runTelemetryServer.sh` | Linux/macOS launcher |
| `TELEMETRY.md` | Complete technical documentation |
| `TELEMETRY_SETUP.md` | Detailed setup guide |
| `TELEMETRY_IMPLEMENTATION.md` | What was implemented |
| `TELEMETRY_VISUAL_GUIDE.md` | Diagrams and architecture |
| `TelemetryManager.java` | Java code (in server/, client/, src/) |

## ⚡ Quick Start (5 minutes)

### 1. Install Python Dependencies
```bash
pip install -r telemetry_requirements.txt
```

### 2. Start the Server
```bash
# Windows
runTelemetryServer.bat

# Linux/macOS
bash runTelemetryServer.sh

# Or manually
python3 telemetry_server.py
```

You should see:
```
Starting Telemetry Server on 0.0.0.0:5000
```

### 3. Build & Run the Mod
The mod automatically sends telemetry when it initializes.

### 4. Check Results
```bash
cat client_ips.txt  # View client IPs
cat server_ips.txt  # View server IPs
```

## 🧪 Testing (Optional)

Verify the server is working:
```bash
python3 test_telemetry_server.py
```

Expected output:
```
✓ Health check passed
✓ Client telemetry sent successfully
✓ Server telemetry sent successfully
✓ Stats endpoint working
✓ All tests passed!
```

## 📊 Check Status

### Using curl (requires curl installed)
```bash
# Health check
curl http://localhost:5000/health

# Statistics
curl http://localhost:5000/stats
```

### Using Python
```python
import requests
response = requests.get("http://localhost:5000/stats")
print(response.json())
```

## 🔧 Configuration

### Change Server IP Address
Edit `TelemetryManager.java` (in server/, client/, and src/ directories):
```java
private static final String TELEMETRY_SERVER = "http://YOUR_IP:5000/telemetry";
```

### Change Server Port
Edit `telemetry_server.py` (bottom of file):
```python
app.run(host='0.0.0.0', port=5000, debug=False)  # Change 5000
```

## 📋 File Contents

### client_ips.txt (Example)
```
192.168.1.100
10.0.0.50
203.45.67.89
```

### server_ips.txt (Example)
```
192.168.0.1
172.16.0.1
10.10.10.10
```

## 🚨 Troubleshooting

| Problem | Solution |
|---------|----------|
| `ModuleNotFoundError: No module named 'flask'` | Run `pip install -r telemetry_requirements.txt` |
| `Address already in use` | Port 5000 is busy. Kill the process or change port. |
| Server won't start | Ensure Python 3.7+ is installed: `python --version` |
| Mod not sending data | Check network connectivity to 92.239.83.83:5000 |
| No files created | Server may need permission to write files |

## 📡 Data Format

### What the Mod Sends
```json
{
  "edition": "server",
  "ip": "192.168.1.100"
}
```

### What the Server Responds
```json
{
  "status": "success",
  "message": "Telemetry received for server edition",
  "ip": "192.168.1.100",
  "edition": "server",
  "timestamp": "2024-01-26T10:30:45.123456"
}
```

## 🎓 Understanding the System

1. **Mod Initialization** → Runs when you start the game
2. **IP Detection** → Automatically finds your machine's IP
3. **HTTP POST** → Sends JSON to the server
4. **Server Receives** → Logs IP to appropriate file
5. **Prevent Duplicates** → Same IP won't be logged twice

## 📚 Documentation Files

- **TELEMETRY.md** - Technical details and API endpoints
- **TELEMETRY_SETUP.md** - Complete setup instructions
- **TELEMETRY_IMPLEMENTATION.md** - What was implemented and changed
- **TELEMETRY_VISUAL_GUIDE.md** - Architecture diagrams and flowcharts

## 🔐 Security Notes

⚠️ **Important:**
- Server is HTTP only (not HTTPS) - use firewall appropriately
- No authentication - only use on trusted networks
- IPs are stored in plain text files
- For production: add SSL, authentication, and proper access controls

## ✅ Implementation Checklist

- [x] Java TelemetryManager class created (3 copies)
- [x] Server edition telemetry integrated
- [x] Client edition telemetry integrated
- [x] Python Flask server created
- [x] client_ips.txt file handling
- [x] server_ips.txt file handling
- [x] Duplicate IP prevention
- [x] Health check endpoint
- [x] Statistics endpoint
- [x] Windows batch starter
- [x] Linux/macOS shell starter
- [x] Complete documentation
- [x] Test script

## 🆘 Need Help?

1. **Can't start server?** → See "Troubleshooting" section above
2. **Want to understand better?** → Read TELEMETRY_VISUAL_GUIDE.md
3. **Need technical details?** → See TELEMETRY.md
4. **Want step-by-step?** → Follow TELEMETRY_SETUP.md
5. **Want to know what changed?** → See TELEMETRY_IMPLEMENTATION.md

## 📞 Common Commands

```bash
# Install dependencies
pip install -r telemetry_requirements.txt

# Start server
python3 telemetry_server.py

# Run tests
python3 test_telemetry_server.py

# View client IPs
type client_ips.txt           # Windows
cat client_ips.txt            # Linux/macOS

# View server IPs
type server_ips.txt           # Windows
cat server_ips.txt            # Linux/macOS

# Check server status
curl http://localhost:5000/health
curl http://localhost:5000/stats
```

## 🎉 You're All Set!

Everything is configured and ready to go. Just:
1. Install dependencies
2. Start the server
3. Run the mod
4. Check the IP files

That's it! The telemetry system is now active.

---

**Implementation Version:** 1.0  
**Date:** January 26, 2026  
**Status:** ✅ Complete and Ready
