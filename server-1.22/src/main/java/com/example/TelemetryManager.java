package com.example;

/**
 * Telemetry removed. This no-op stub preserves the original API so the project compiles
 * while the full telemetry implementation is archived under `oldtel/`.
 */
public final class TelemetryManager {
    private TelemetryManager() {}

    /**
     * No-op replacement for telemetry sending.
     */
    public static void sendTelemetry(String edition) {
        // telemetry intentionally removed
    }

    /**
     * Returns a harmless default instead of attempting network IO.
     */
    private static String getLocalIP() {
        return "unknown";
    }
}
