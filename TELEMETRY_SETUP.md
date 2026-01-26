# Telemetry System Setup Guide

## Quick Start

### Prerequisites
- Python 3.7 or higher
- pip (Python package manager)

### Step 1: Install Dependencies
Run this command in the project root directory:

```bash
pip install -r telemetry_requirements.txt
```

### Step 2: Start the Telemetry Server

**On Windows:**
```bash
runTelemetryServer.bat
```

**On Linux/macOS:**
```bash
bash runTelemetryServer.sh
```

**Or manually:**
```bash
python3 telemetry_server.py
```

### Step 3: Verify Server is Running
The server should output:
```
Starting Telemetry Server on 0.0.0.0:5000
```

You can test it with:
```bash
curl http://localhost:5000/health
```

Should return:
```json
{"status":"healthy"}
```

## What Happens Next

1. When the Minecraft client or server edition starts with the mod enabled, it will:
   - Detect the machine's IP address
   - Send a POST request to `92.239.83.83:5000/telemetry`
   - Include the edition ("client" or "server") and IP address

2. The Python server receives the request and:
   - Validates the data
   - Logs the IP to the appropriate file (client_ips.txt or server_ips.txt)
   - Prevents duplicate entries

3. You can check statistics at any time:
   ```bash
   curl http://localhost:5000/stats
   ```

## File Output

After running, you'll see:
- `client_ips.txt` - List of client edition IPs (one per line)
- `server_ips.txt` - List of server edition IPs (one per line)

Example content:
```
192.168.1.100
10.0.0.50
203.45.67.89
```

## Important Notes

⚠️ **Security Note:** The telemetry server is a simple HTTP server. For production use, consider:
- Running behind an HTTPS reverse proxy
- Implementing authentication
- Setting up proper firewall rules
- Securing the IP data files
- Running on a restricted network

## Troubleshooting

**Port Already in Use:**
If port 5000 is already in use, you'll see:
```
OSError: [Errno 48] Address already in use
```
Solution: Kill the process using port 5000 or change the port in `telemetry_server.py`

**Module Not Found:**
If you get `ModuleNotFoundError: No module named 'flask'`:
Solution: Run `pip install -r telemetry_requirements.txt`

**Connection Refused:**
If the mod can't connect to the server:
- Verify the server is running
- Check firewall settings
- Verify 92.239.83.83 points to your server or update the IP in TelemetryManager.java

## Monitoring

Monitor the server in real-time by checking the logs:

**Windows:**
```cmd
type client_ips.txt
type server_ips.txt
```

**Linux/macOS:**
```bash
tail -f client_ips.txt
tail -f server_ips.txt
```

Or use the stats endpoint:
```bash
curl http://localhost:5000/stats
```

## More Information

See [TELEMETRY.md](TELEMETRY.md) for complete technical documentation.
