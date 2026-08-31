package com.example;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

public class FunctionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmakerclient");
    private static final String MANIFEST_URL = "https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/functions.json";
    private static final Path FUNCTIONS_PATH = Paths.get("config", "CommandMaker", "Functions");
    private static Map<String, ManifestEntry> cachedManifest = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL = 300000; // 5 minutes

    public static Path getFunctionsPath() {
        return FUNCTIONS_PATH;
    }

    public static void executeFunction(String functionName, Minecraft client) {
        try {
            Path functionFile = FUNCTIONS_PATH.resolve(functionName + ".mcfunction");
            if (!Files.exists(functionFile)) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("§c✖ Function §f" + functionName + "§c not found"));
                }
                return;
            }
            List<String> lines = Files.readAllLines(functionFile);
            if (client.player == null) return;

            for (int idx = 0; idx < lines.size(); idx++) {
                String rawLine = lines.get(idx);
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    CMDMakerClient.executeClientCommand(line);
                } catch (Exception ex) {
                    int lineNum = idx + 1;
                    LOGGER.error("Failed to execute function '{}' at line {}: {}", functionName, lineNum, line, ex);
                    client.player.sendSystemMessage(
                        Component.literal("§c✖ Error in §f" + functionName + "§c at line §f" + lineNum + "§c: §7" + ex.getMessage()));
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute function '{}'", functionName, e);
            if (client.player != null) {
                client.player.sendSystemMessage(Component.literal("§c✖ Failed to run §f" + functionName + "§c: §7" + e.getMessage()));
            }
        }
    }

    public static void downloadFunction(String functionName, Minecraft client) {
        new Thread(() -> {
            try {
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/" + functionName + ".mcfunction"))
                    .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Path filePath = FUNCTIONS_PATH.resolve(functionName + ".mcfunction");
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    if (client.player != null) {
                        client.player.sendSystemMessage(
                            Component.literal("§a✔ Downloaded §f" + functionName + "§a successfully! §7Edit: config/CommandMaker/Functions/" + functionName + ".mcfunction"));
                    }
                } else {
                    if (client.player != null) {
                        client.player.sendSystemMessage(
                            Component.literal("§c✖ Download failed for §f" + functionName + "§c: HTTP §7" + response.statusCode()));
                    }
                }
            } catch (Exception e) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("§c✖ Download failed: §7" + e.getMessage()));
                }
            }
        }).start();
    }

    public static Map<String, ManifestEntry> fetchFunctionManifest() {
        long now = System.currentTimeMillis();
        if (cachedManifest != null && (now - cacheTimestamp) < CACHE_TTL) {
            return cachedManifest;
        }
        Map<String, ManifestEntry> manifest = new LinkedHashMap<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MANIFEST_URL))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonElement je = JsonParser.parseString(response.body());
                if (je.isJsonObject()) {
                    JsonObject obj = je.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        try {
                            JsonElement val = entry.getValue();
                            if (val.isJsonObject()) {
                                JsonObject valObj = val.getAsJsonObject();
                                String desc = valObj.has("desc") ? valObj.get("desc").getAsString() : "";
                                String icon = valObj.has("icon") ? valObj.get("icon").getAsString() : null;
                                manifest.put(entry.getKey(), new ManifestEntry(desc, icon));
                            } else if (val.isJsonPrimitive()) {
                                manifest.put(entry.getKey(), new ManifestEntry(val.getAsString(), null));
                            }
                        } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch function manifest, falling back to file list", e);
        }
        if (manifest.isEmpty()) {
            List<String> names = fetchDownloadableFunctionNames();
            for (String name : names) {
                manifest.put(name, new ManifestEntry("", null));
            }
        }
        cachedManifest = manifest;
        cacheTimestamp = now;
        return manifest;
    }

    public static List<String> fetchDownloadableFunctionNames() {
        List<String> names = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/Diamondstar-Mods/Minecraft-Command-Maker/contents/docs/cdn/functions"))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonElement je = JsonParser.parseString(response.body());
                if (je.isJsonArray()) {
                    for (JsonElement e : je.getAsJsonArray()) {
                        try {
                            JsonObject obj = e.getAsJsonObject();
                            String name = obj.get("name").getAsString();
                            if (name.endsWith(".mcfunction")) {
                                names.add(name.substring(0, name.length() - ".mcfunction".length()));
                            }
                        } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch downloadable function names", e);
        }
        return names;
    }

    public static List<String> listLocalFunctions() {
        List<String> names = new ArrayList<>();
        try {
            if (Files.exists(FUNCTIONS_PATH)) {
                Files.list(FUNCTIONS_PATH)
                    .filter(path -> path.toString().endsWith(".mcfunction"))
                    .forEach(path -> {
                        String name = path.getFileName().toString().replace(".mcfunction", "");
                        names.add(name);
                    });
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to list local functions", e);
        }
        return names;
    }
}
