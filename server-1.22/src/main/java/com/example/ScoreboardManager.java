package com.example;

import com.google.gson.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Custom Scoreboard system for Command Maker (MC 26.1).
 * Uses command-based scoreboard creation to avoid version-specific scoreboard API issues.
 *
 * Config: config/CommandMaker/scoreboards.json
 */
public class ScoreboardManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-scoreboards");
    private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "scoreboards.json");

    private static final Map<String, ScoreboardConfig> config = new LinkedHashMap<>();
    private static int tickCounter = 0;
    private static boolean initialized = false;

    public static class ScoreboardConfig {
        public String name;
        public String displayName;
        public String slot;
        public int updateInterval;
        public boolean enabled;
        public List<String> lines;

        public ScoreboardConfig(String name) {
            this.name = name;
            this.displayName = "Scoreboard";
            this.slot = "sidebar";
            this.updateInterval = 20;
            this.enabled = false;
            this.lines = new ArrayList<>();
        }
    }

    public static void initialize(MinecraftServer server) {
        if (initialized) return;
        initialized = true;
        loadConfig();
        createAllScoreboards(server);
        registerTickHandler();
        LOGGER.info("ScoreboardManager initialized with {} configured scoreboards", config.size());
    }

    public static void reload(MinecraftServer server) {
        removeAllScoreboards(server);
        config.clear();
        loadConfig();
        createAllScoreboards(server);
        LOGGER.info("ScoreboardManager reloaded");
    }

    public static Map<String, ScoreboardConfig> getConfiguredScoreboards() {
        return Collections.unmodifiableMap(config);
    }

    private static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            updateScoreboards(server);
        });
    }

    private static void updateScoreboards(MinecraftServer server) {
        for (ScoreboardConfig cfg : config.values()) {
            if (cfg.updateInterval <= 0) continue;
            if (tickCounter % cfg.updateInterval != 0) continue;
            updateScoreboardLines(server, cfg);
        }
    }

    private static void updateScoreboardLines(MinecraftServer server, ScoreboardConfig cfg) {
        var dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = server.createCommandSourceStack();

        // Clear old scores
        for (int i = 0; i < 15; i++) {
            String fakePlayer = "cm" + cfg.name.hashCode() + "l" + i;
            try {
                String cmd = "scoreboard players reset " + fakePlayer + " " + cfg.name;
                dispatcher.execute(dispatcher.parse(cmd, source));
            } catch (Exception ignored) {}
        }

        // Set new scores
        int scoreValue = cfg.lines.size() - 1;
        for (int lineIdx = 0; lineIdx < cfg.lines.size(); lineIdx++) {
            String line = cfg.lines.get(lineIdx);
            String resolvedLine = resolvePlaceholders(line, server);
            resolvedLine = resolvedLine.replace("&", "§");
            if (resolvedLine.length() > 40) {
                resolvedLine = resolvedLine.substring(0, 40);
            }

            String fakePlayer = "cm" + cfg.name.hashCode() + "l" + lineIdx;
            try {
                // Use a team for display text
                String teamName = "cm" + cfg.name.hashCode() + "t" + lineIdx;
                try {
                    String addTeamCmd = "team add " + teamName;
                    dispatcher.execute(dispatcher.parse(addTeamCmd, source));
                } catch (Exception ignored) {}
                try {
                    String prefixCmd = "team modify " + teamName + " prefix " + escapeForCommand(resolvedLine);
                    dispatcher.execute(dispatcher.parse(prefixCmd, source));
                } catch (Exception ignored) {}
                try {
                    String joinCmd = "team join " + teamName + " " + fakePlayer;
                    dispatcher.execute(dispatcher.parse(joinCmd, source));
                } catch (Exception ignored) {}

                // Set the score
                String cmd = "scoreboard players set " + fakePlayer + " " + cfg.name + " " + scoreValue;
                dispatcher.execute(dispatcher.parse(cmd, source));
            } catch (Exception ignored) {}

            scoreValue--;
        }
    }

    private static void createAllScoreboards(MinecraftServer server) {
        var dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = server.createCommandSourceStack();

        for (ScoreboardConfig cfg : config.values()) {
            String displayName = resolvePlaceholders(cfg.displayName, null).replace("&", "§");
            try {
                try {
                    String removeCmd = "scoreboard objectives remove " + cfg.name;
                    dispatcher.execute(dispatcher.parse(removeCmd, source));
                } catch (Exception ignored) {}

                String createCmd = "scoreboard objectives add " + cfg.name + " dummy " + escapeForCommand(displayName);
                dispatcher.execute(dispatcher.parse(createCmd, source));

                String slotName = switch (cfg.slot.toLowerCase()) {
                    case "list" -> "list";
                    case "belowname" -> "belowname";
                    default -> "sidebar";
                };
                String displayCmd = "scoreboard objectives setdisplay " + slotName + " " + cfg.name;
                dispatcher.execute(dispatcher.parse(displayCmd, source));

                LOGGER.info("Created scoreboard '{}' in slot '{}'", cfg.name, cfg.slot);
            } catch (Exception e) {
                LOGGER.error("Failed to create scoreboard '{}': {}", cfg.name, e.getMessage());
            }
        }
    }

    private static void removeAllScoreboards(MinecraftServer server) {
        var dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = server.createCommandSourceStack();
        for (ScoreboardConfig cfg : config.values()) {
            try {
                String cmd = "scoreboard objectives remove " + cfg.name;
                dispatcher.execute(dispatcher.parse(cmd, source));
            } catch (Exception ignored) {}
        }
    }

    private static String escapeForCommand(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String resolvePlaceholders(String text, MinecraftServer server) {
        if (text == null) return "";
        if (server != null) {
            int online = server.getPlayerList().getPlayerCount();
            int max = server.getMaxPlayers();
            text = text.replace("${player_count}", String.valueOf(online));
            text = text.replace("${server_online}", String.valueOf(online));
            text = text.replace("${server_max}", String.valueOf(max));
            text = text.replace("${tps}", String.format("%.1f", getAverageTPS(server)));
        }
        return text;
    }

    private static float getAverageTPS(MinecraftServer server) {
        // TODO: fix for MC 26.1 mappings
        return 20.0f;
    }

    private static void loadConfig() {
        config.clear();
        try {
            if (!Files.exists(CONFIG_PATH)) createDefaultConfig();
            String json = new String(Files.readAllBytes(CONFIG_PATH));
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                if (root.has("scoreboards") && root.get("scoreboards").isJsonObject()) {
                    JsonObject sbs = root.getAsJsonObject("scoreboards");
                    for (Map.Entry<String, JsonElement> entry : sbs.entrySet()) {
                        try {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            ScoreboardConfig cfg = new ScoreboardConfig(entry.getKey());
                            cfg.displayName = obj.has("displayName") ? obj.get("displayName").getAsString() : entry.getKey();
                            cfg.slot = obj.has("slot") ? obj.get("slot").getAsString() : "sidebar";
                            cfg.updateInterval = obj.has("updateInterval") ? obj.get("updateInterval").getAsInt() : 20;
                            if (obj.has("lines") && obj.get("lines").isJsonArray()) {
                                for (JsonElement line : obj.getAsJsonArray("lines")) {
                                    cfg.lines.add(line.getAsString());
                                }
                            }
                            config.put(entry.getKey(), cfg);
                        } catch (Exception e) {
                            LOGGER.warn("Skipping malformed scoreboard entry: {}", entry.getKey(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load scoreboards config", e);
        }
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject(); JsonObject sbs = new JsonObject();
            for (ScoreboardConfig cfg : config.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("displayName", cfg.displayName);
                obj.addProperty("slot", cfg.slot);
                obj.addProperty("updateInterval", cfg.updateInterval);
                JsonArray linesArray = new JsonArray();
                for (String line : cfg.lines) linesArray.add(line);
                obj.add("lines", linesArray);
                sbs.add(cfg.name, obj);
            }
            root.add("scoreboards", sbs);
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save scoreboards config", e);
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject(); JsonObject sbs = new JsonObject();
            JsonObject example = new JsonObject();
            example.addProperty("displayName", "&6&lServer Info");
            example.addProperty("slot", "sidebar");
            example.addProperty("updateInterval", 20);
            JsonArray lines = new JsonArray();
            lines.add("&6&lMy Server");
            lines.add("&f");
            lines.add("&7Players: &a${player_count}/${server_max}");
            lines.add("&7TPS: &a${tps}");
            lines.add("&f");
            lines.add("&7Powered by");
            lines.add("&eCommand Maker");
            example.add("lines", lines);
            sbs.add("server_info", example);
            root.add("scoreboards", sbs);
            root.addProperty("_comment", "Available placeholders: ${player_count}, ${server_max}, ${tps}. Use & for color codes.");
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE_NEW);
            LOGGER.info("Created default scoreboards config");
        } catch (Exception e) {
            LOGGER.error("Failed to create default scoreboards config", e);
        }
    }
}
