#!/usr/bin/env python3
"""
Telemetry Server for Minecraft Command Maker Mod
Listens on port 5000 and logs telemetry data from client and server editions
"""

from flask import Flask, request, jsonify
import json
import os
from datetime import datetime
import logging

app = Flask(__name__)

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# File paths for storing IPs
CLIENT_IPS_FILE = 'client_ips.txt'
SERVER_IPS_FILE = 'server_ips.txt'

# Initialize files if they don't exist
def init_files():
    """Initialize IP files if they don't exist"""
    for file in [CLIENT_IPS_FILE, SERVER_IPS_FILE]:
        if not os.path.exists(file):
            open(file, 'a').close()
            logger.info(f"Created {file}")

init_files()

def log_ip(ip_address, edition):
    """Log IP address to appropriate file"""
    if edition.lower() == 'client':
        file_path = CLIENT_IPS_FILE
    elif edition.lower() == 'server':
        file_path = SERVER_IPS_FILE
    else:
        logger.warning(f"Unknown edition: {edition}")
        return False
    
    try:
        # Check if IP already exists in file
        if os.path.exists(file_path):
            with open(file_path, 'r') as f:
                existing_ips = f.read().splitlines()
        else:
            existing_ips = []
        
        # Add IP if it doesn't already exist
        if ip_address not in existing_ips:
            with open(file_path, 'a') as f:
                f.write(f"{ip_address}\n")
            logger.info(f"Logged {edition} IP: {ip_address} to {file_path}")
            return True
        else:
            logger.info(f"IP {ip_address} already logged in {file_path}")
            return True
    except Exception as e:
        logger.error(f"Error logging IP: {e}")
        return False

@app.route('/telemetry', methods=['POST'])
def telemetry():
    """Receive and log telemetry data"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No JSON data provided'}), 400
        
        edition = data.get('edition')
        ip_address = data.get('ip')
        
        if not edition or not ip_address:
            return jsonify({'error': 'Missing edition or ip field'}), 400
        
        # Log the IP
        success = log_ip(ip_address, edition)
        
        if success:
            response = {
                'status': 'success',
                'message': f'Telemetry received for {edition} edition',
                'ip': ip_address,
                'edition': edition,
                'timestamp': datetime.now().isoformat()
            }
            logger.info(f"Telemetry received - Edition: {edition}, IP: {ip_address}")
            return jsonify(response), 200
        else:
            return jsonify({'error': 'Failed to log telemetry'}), 500
    
    except Exception as e:
        logger.error(f"Error processing telemetry: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({'status': 'healthy'}), 200

@app.route('/stats', methods=['GET'])
def stats():
    """Get statistics about logged IPs"""
    try:
        client_count = 0
        server_count = 0
        
        if os.path.exists(CLIENT_IPS_FILE):
            with open(CLIENT_IPS_FILE, 'r') as f:
                client_count = len([line.strip() for line in f if line.strip()])
        
        if os.path.exists(SERVER_IPS_FILE):
            with open(SERVER_IPS_FILE, 'r') as f:
                server_count = len([line.strip() for line in f if line.strip()])
        
        return jsonify({
            'client_ips_count': client_count,
            'server_ips_count': server_count,
            'total_ips': client_count + server_count
        }), 200
    except Exception as e:
        logger.error(f"Error getting stats: {e}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    logger.info("Starting Telemetry Server on 0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=False)
