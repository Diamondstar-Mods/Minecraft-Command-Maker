package com.example;

/**
 * Telemetry removed. No-op stub kept in OldServerSrc so legacy builds compile.
 */
public final class TelemetryManager {
    private TelemetryManager() {}

    public static void sendTelemetry(String edition) {
        // telemetry intentionally removed
    }

    private static String getLocalIP() {
        return "unknown";
    }
}
