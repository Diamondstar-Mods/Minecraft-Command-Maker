#!/usr/bin/env python3
"""
Telemetry Server Test Script
Tests the telemetry server endpoints and functionality
"""

import requests
import json
import time
import sys

# Configuration
SERVER_URL = "http://localhost:5000"
TIMEOUT = 5

def test_health():
    """Test health endpoint"""
    print("Testing /health endpoint...")
    try:
        response = requests.get(f"{SERVER_URL}/health", timeout=TIMEOUT)
        if response.status_code == 200:
            print("✓ Health check passed")
            print(f"  Response: {response.json()}")
            return True
        else:
            print(f"✗ Health check failed with status {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ Health check failed: {e}")
        return False

def test_telemetry_client():
    """Test sending client telemetry"""
    print("\nTesting /telemetry endpoint (client)...")
    try:
        data = {
            "edition": "client",
            "ip": "192.168.1.100"
        }
        response = requests.post(
            f"{SERVER_URL}/telemetry",
            json=data,
            timeout=TIMEOUT
        )
        if response.status_code == 200:
            print("✓ Client telemetry sent successfully")
            print(f"  Response: {response.json()}")
            return True
        else:
            print(f"✗ Client telemetry failed with status {response.status_code}")
            print(f"  Response: {response.text}")
            return False
    except Exception as e:
        print(f"✗ Client telemetry failed: {e}")
        return False

def test_telemetry_server():
    """Test sending server telemetry"""
    print("\nTesting /telemetry endpoint (server)...")
    try:
        data = {
            "edition": "server",
            "ip": "10.0.0.1"
        }
        response = requests.post(
            f"{SERVER_URL}/telemetry",
            json=data,
            timeout=TIMEOUT
        )
        if response.status_code == 200:
            print("✓ Server telemetry sent successfully")
            print(f"  Response: {response.json()}")
            return True
        else:
            print(f"✗ Server telemetry failed with status {response.status_code}")
            print(f"  Response: {response.text}")
            return False
    except Exception as e:
        print(f"✗ Server telemetry failed: {e}")
        return False

def test_stats():
    """Test stats endpoint"""
    print("\nTesting /stats endpoint...")
    try:
        response = requests.get(f"{SERVER_URL}/stats", timeout=TIMEOUT)
        if response.status_code == 200:
            print("✓ Stats endpoint working")
            stats = response.json()
            print(f"  Response: {stats}")
            print(f"  Client IPs: {stats.get('client_ips_count', 0)}")
            print(f"  Server IPs: {stats.get('server_ips_count', 0)}")
            print(f"  Total IPs: {stats.get('total_ips', 0)}")
            return True
        else:
            print(f"✗ Stats endpoint failed with status {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ Stats endpoint failed: {e}")
        return False

def test_invalid_data():
    """Test sending invalid data"""
    print("\nTesting invalid data handling...")
    try:
        # Missing edition
        data = {"ip": "192.168.1.1"}
        response = requests.post(f"{SERVER_URL}/telemetry", json=data, timeout=TIMEOUT)
        if response.status_code == 400:
            print("✓ Server correctly rejects incomplete data")
            return True
        else:
            print(f"✗ Server did not reject invalid data (status: {response.status_code})")
            return False
    except Exception as e:
        print(f"✗ Invalid data test failed: {e}")
        return False

def main():
    """Run all tests"""
    print("=" * 60)
    print("Telemetry Server Test Suite")
    print("=" * 60)
    print(f"Testing server at {SERVER_URL}")
    print()
    
    results = []
    
    # Run tests
    results.append(("Health Check", test_health()))
    results.append(("Client Telemetry", test_telemetry_client()))
    results.append(("Server Telemetry", test_telemetry_server()))
    results.append(("Stats Endpoint", test_stats()))
    results.append(("Invalid Data Handling", test_invalid_data()))
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"{status}: {test_name}")
    
    print(f"\nTotal: {passed}/{total} tests passed")
    
    if passed == total:
        print("\n✓ All tests passed! Telemetry server is working correctly.")
        return 0
    else:
        print(f"\n✗ {total - passed} test(s) failed. Check the server configuration.")
        return 1

if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\n\nTests interrupted by user")
        sys.exit(1)
