package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class TelemetryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-telemetry");
    private static final String TELEMETRY_SERVER = "http://92.239.83.83:5000/telemetry";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void sendTelemetry(String edition) {
        new Thread(() -> {
            try {
                String senderIP = getLocalIP();
                
                String jsonBody = String.format(
                    "{\"edition\":\"%s\",\"ip\":\"%s\"}",
                    edition,
                    senderIP
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TELEMETRY_SERVER))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

                LOGGER.info("Telemetry sent successfully. Status: " + response.statusCode() + ", Edition: " + edition + ", IP: " + senderIP);
            } catch (Exception e) {
                LOGGER.warn("Failed to send telemetry: " + e.getMessage());
            }
        }).start();
    }

    private static String getLocalIP() {
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = in.readLine();
            in.close();
            return ip != null ? ip.trim() : "unknown";
        } catch (Exception e1) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e2) {
                LOGGER.warn("Could not determine IP address: " + e2.getMessage());
                return "unknown";
            }
        }
    }
}
