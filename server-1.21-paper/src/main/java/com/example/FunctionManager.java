package com.example;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class FunctionManager {
    private static final Logger LOGGER = Logger.getLogger("CommandMaker");
    private static final String MANIFEST_URL = "https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/functions.json";
    private static Map<String, ManifestEntry> cachedManifest = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL = 300000;

    public static void executeFunction(String functionName, CommandSender sender) {
        try {
            File functionFile = new File(AliasManager.getFunctionsDir(), functionName + ".mcfunction");
            if (!functionFile.exists()) {
                sender.sendMessage("§c✖ Function §f" + functionName + "§c not found");
                return;
            }
            List<String> lines = Files.readAllLines(functionFile.toPath());
            int executed = 0;
            Player player = sender instanceof Player ? (Player) sender : null;
            for (int idx = 0; idx < lines.size(); idx++) {
                String rawLine = lines.get(idx);
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    String command = VariableManager.substituteVariables(line, player);
                    Bukkit.dispatchCommand(sender, command);
                    executed++;
                } catch (Exception ex) {
                    int lineNum = idx + 1;
                    String errorMsg = ex.getMessage();
                    LOGGER.warning("Failed to execute function '" + functionName + "' at line " + lineNum + ": " + errorMsg);
                    sender.sendMessage("§c✖ Error in §f" + functionName + "§c at line §f" + lineNum + "§c: §7" + errorMsg);
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to execute function '" + functionName + "': " + e.getMessage());
            sender.sendMessage("§c✖ Failed to run §f" + functionName + "§c: §7" + e.getMessage());
        }
    }

    public static void downloadFunction(String functionName, CommandSender sender) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/" + functionName + ".mcfunction"))
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    File filePath = new File(AliasManager.getFunctionsDir(), functionName + ".mcfunction");
                    Files.write(filePath.toPath(), response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    sender.sendMessage("§a✔ Downloaded §f" + functionName + "§a successfully! §7Edit: plugins/CommandMaker/Functions/" + functionName + ".mcfunction");
                } else {
                    sender.sendMessage("§c✖ Download failed for §f" + functionName + "§c: HTTP §7" + response.statusCode());
                }
            } catch (Exception e) {
                sender.sendMessage("§c✖ Download failed: §7" + e.getMessage());
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
            LOGGER.warning("Failed to fetch function manifest: " + e.getMessage());
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
            LOGGER.warning("Failed to fetch downloadable function names: " + e.getMessage());
        }
        return names;
    }

    public static void listDownloadableFunctions(CommandSender sender) {
        sender.sendMessage("§6⌛ Fetching function library...");
        new Thread(() -> {
            try {
                Map<String, ManifestEntry> manifest = fetchFunctionManifest();
                if (manifest.isEmpty()) {
                    sender.sendMessage("§c✖ No downloadable functions available");
                } else {
                    sender.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    sender.sendMessage("§6§l📦 Downloadable Functions §7(§f" + manifest.size() + "§7 available)");
                    sender.sendMessage("§7Use §e/cmd downloadfunction <name> §7or open the GUI with §e/cmd functions");
                    sender.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                        String name = entry.getKey();
                        String desc = entry.getValue().description;
                        if (desc != null && !desc.isEmpty()) {
                            sender.sendMessage("§a  ◆ §f" + name + " §8▶ §7" + desc);
                        } else {
                            sender.sendMessage("§a  ◆ §f" + name);
                        }
                    }
                    sender.sendMessage("§7─────────────────────────────");
                }
            } catch (Exception e) {
                sender.sendMessage("§cFailed to fetch function list: " + e.getMessage());
            }
        }).start();
    }

    public static List<String> listLocalFunctions() {
        List<String> names = new ArrayList<>();
        try {
            File functionsDir = AliasManager.getFunctionsDir();
            if (functionsDir.exists()) {
                File[] files = functionsDir.listFiles((dir, name) -> name.endsWith(".mcfunction"));
                if (files != null) {
                    for (File f : files) {
                        names.add(f.getName().replace(".mcfunction", ""));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to list local functions: " + e.getMessage());
        }
        return names;
    }
}
