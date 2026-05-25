package com.example;

import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");
    private static final String MANIFEST_URL = "https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/functions.json";
    private static Map<String, String> cachedManifest = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL = 300000; // 5 minutes

    public static int executeFunction(String functionName, CommandContext<ServerCommandSource> ctx) {
        try {
            Path functionFile = AliasManager.getFunctionsPath().resolve(functionName + ".mcfunction");
            if (!Files.exists(functionFile)) {
                ctx.getSource().sendFeedback(() -> Text.literal("§c✖ Function §f" + functionName + "§c not found"), false);
                return 0;
            }
            List<String> lines = Files.readAllLines(functionFile);
            var cmdDispatcher = ctx.getSource().getServer().getCommandManager().getDispatcher();
            int executed = 0;
            for (int idx = 0; idx < lines.size(); idx++) {
                String rawLine = lines.get(idx);
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    String command = VariableManager.substituteVariables(line, ctx);
                    var parsed = cmdDispatcher.parse(command, ctx.getSource());
                    cmdDispatcher.execute(parsed);
                    executed++;
                } catch (Exception ex) {
                    int lineNum = idx + 1;
                    String errorMsg = ex.getMessage();
                    LOGGER.error("Failed to execute function '{}' at line {}: {}", functionName, lineNum, line, ex);
                    ctx.getSource().sendFeedback(() -> Text.literal("§c✖ Error in §f" + functionName + "§c at line §f" + lineNum + "§c: §7" + errorMsg), false);
                    return executed;
                }
            }
            return executed;
        } catch (Exception e) {
            LOGGER.error("Failed to execute function '{}'", functionName, e);
            ctx.getSource().sendFeedback(() -> Text.literal("§c✖ Failed to run §f" + functionName + "§c: §7" + e.getMessage()), false);
            return 0;
        }
    }

    public static void downloadFunction(String functionName, ServerCommandSource source) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/" + functionName + ".mcfunction"))
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Path filePath = AliasManager.getFunctionsPath().resolve(functionName + ".mcfunction");
                    Files.write(filePath, response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    source.sendFeedback(() -> Text.literal("§a✔ Downloaded §f" + functionName + "§a successfully! §7Edit: config/CommandMaker/Functions/" + functionName + ".mcfunction"), false);
                } else {
                    source.sendFeedback(() -> Text.literal("§c✖ Download failed for §f" + functionName + "§c: HTTP §7" + response.statusCode()), false);
                }
            } catch (Exception e) {
                source.sendFeedback(() -> Text.literal("§c✖ Download failed: §7" + e.getMessage()), false);
            }
        }).start();
    }

    public static Map<String, String> fetchFunctionManifest() {
        long now = System.currentTimeMillis();
        if (cachedManifest != null && (now - cacheTimestamp) < CACHE_TTL) {
            return cachedManifest;
        }
        Map<String, String> manifest = new LinkedHashMap<>();
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
                            manifest.put(entry.getKey(), entry.getValue().getAsString());
                        } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch function manifest, falling back to file list", e);
        }
        if (manifest.isEmpty()) {
            // Fallback: fetch from GitHub API without descriptions
            List<String> names = fetchDownloadableFunctionNames();
            for (String name : names) {
                manifest.put(name, "");
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

    public static void listDownloadableFunctions(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("§6⌛ Fetching function library..."), false);
        new Thread(() -> {
            try {
                Map<String, String> manifest = fetchFunctionManifest();
                if (manifest.isEmpty()) {
                    source.sendFeedback(() -> Text.literal("§c✖ No downloadable functions available"), false);
                } else {
                    source.sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
                    source.sendFeedback(() -> Text.literal("§6§l📦 Downloadable Functions §7(§f" + manifest.size() + "§7 available)"), false);
                    source.sendFeedback(() -> Text.literal("§7Use §e/cmd downloadfunction <name> §7or open the GUI with §e/cmd functions"), false);
                    source.sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
                    for (Map.Entry<String, String> entry : manifest.entrySet()) {
                        String name = entry.getKey();
                        String desc = entry.getValue();
                        if (desc != null && !desc.isEmpty()) {
                            source.sendFeedback(() -> Text.literal("§a  ◆ §f" + name + " §8▶ §7" + desc), false);
                        } else {
                            source.sendFeedback(() -> Text.literal("§a  ◆ §f" + name), false);
                        }
                    }
                    source.sendFeedback(() -> Text.literal("§7─────────────────────────────"), false);
                }
            } catch (Exception e) {
                source.sendFeedback(() -> Text.literal("§cFailed to fetch function list: " + e.getMessage()), false);
            }
        }).start();
    }

    public static List<String> listLocalFunctions() {
        List<String> names = new ArrayList<>();
        try {
            Path functionsPath = AliasManager.getFunctionsPath();
            if (Files.exists(functionsPath)) {
                Files.list(functionsPath)
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
