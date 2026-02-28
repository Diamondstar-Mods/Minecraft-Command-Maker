// Moved to oldtel/TelemetryManager.java
    /**
     * Sends telemetry data to the remote server
     * @param edition "server" or "client"
     */
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

    /**
     * Gets the local IP address of the machine
     */
    private static String getLocalIP() {
        try {
            // Try to get the IP by connecting to a external DNS
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = in.readLine();
            in.close();
            return ip != null ? ip.trim() : "unknown";
        } catch (Exception e1) {
            try {
                // Fallback: try local hostname
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e2) {
                LOGGER.warn("Could not determine IP address: " + e2.getMessage());
                return "unknown";
            }
        }
    }
}
