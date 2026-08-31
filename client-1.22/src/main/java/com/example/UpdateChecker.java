package com.example;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmakerclient");
    private static final String VERSION_URL =
        "https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/currentversion.txt";
    private static final String DOWNLOAD_URL =
        "https://commandmakerwiki.lucasgeitgey.com/download.html";

    private static String currentVersion;
    private static String latestVersion;
    private static boolean checked = false;
    private static String updateMessage = null;

    public static void check() {
        if (checked) return;
        checked = true;

        currentVersion = FabricLoader.getInstance()
            .getModContainer("nekkycommandmakerclient")
            .map(m -> m.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERSION_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    latestVersion = response.body().trim();
                    if (isNewer(latestVersion, currentVersion)) {
                        updateMessage = "§a§l⚡ " + latestVersion + " is available! §r§aYou are on version §f"
                            + currentVersion + "§a. §nUpdate here:§r§b " + DOWNLOAD_URL;
                        LOGGER.info("Update available: {} (current: {})", latestVersion, currentVersion);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Update check failed: {}", e.getMessage());
            }
        }, "CM-Client-UpdateChecker").start();
    }

    private static boolean isNewer(String latest, String current) {
        try {
            String[] lp = latest.replaceAll("[^0-9.]", "").split("\\.");
            String[] cp = current.replaceAll("[^0-9.]", "").split("\\.");
            int len = Math.max(lp.length, cp.length);
            for (int i = 0; i < len; i++) {
                int lv = i < lp.length ? Integer.parseInt(lp[i]) : 0;
                int cv = i < cp.length ? Integer.parseInt(cp[i]) : 0;
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    public static String getUpdateMessage() {
        return updateMessage;
    }
}
