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

public class FunctionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");

    public static int executeFunction(String functionName, CommandContext<ServerCommandSource> ctx) {
        try {
            Path functionFile = AliasManager.getFunctionsPath().resolve(functionName + ".mcfunction");
            if (!Files.exists(functionFile)) {
                ctx.getSource().sendFeedback(() -> Text.literal("Function '" + functionName + "' not found."), false);
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
                    ctx.getSource().sendFeedback(() -> Text.literal("Error executing function '" + functionName + "' line " + lineNum + ": " + errorMsg), false);
                    return executed;
                }
            }
            return executed;
        } catch (Exception e) {
            LOGGER.error("Failed to execute function '{}'", functionName, e);
            ctx.getSource().sendFeedback(() -> Text.literal("Failed to execute function '" + functionName + "': " + e.getMessage()), false);
            return 0;
        }
    }

    public static void downloadFunction(String functionName, ServerCommandSource source) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://commandmakerwiki.lucasgeitgey.com/cdn/functions/" + functionName + ".mcfunction"))
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Path filePath = AliasManager.getFunctionsPath().resolve(functionName + ".mcfunction");
                    Files.write(filePath, response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    source.sendFeedback(() -> Text.literal("Downloaded function '" + functionName + "' successfully."), false);
                } else {
                    source.sendFeedback(() -> Text.literal("Failed to download function '" + functionName + "': HTTP " + response.statusCode()), false);
                }
            } catch (Exception e) {
                source.sendFeedback(() -> Text.literal("Failed to download function '" + functionName + "': " + e.getMessage()), false);
            }
        }).start();
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
        source.sendFeedback(() -> Text.literal("§6§lFetching available functions..."), false);
        new Thread(() -> {
            try {
                List<String> names = fetchDownloadableFunctionNames();
                if (names.isEmpty()) {
                    source.sendFeedback(() -> Text.literal("§cNo downloadable functions found."), false);
                } else {
                    source.sendFeedback(() -> Text.literal("§6§l📦 Downloadable Functions (" + names.size() + ") 📦"), false);
                    source.sendFeedback(() -> Text.literal("§7Use §e/commandmaker downloadfunction <name> §7to download."), false);
                    source.sendFeedback(() -> Text.literal("§7─────────────────────────────"), false);
                    for (String name : names) {
                        source.sendFeedback(() -> Text.literal("§a • §f" + name), false);
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
