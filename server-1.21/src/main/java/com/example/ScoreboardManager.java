package com.example;

import com.google.gson.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Custom Scoreboard system for Command Maker.
 * Allows creators to define scoreboards in config with placeholders,
 * update intervals, and dynamic lines. Uses vanilla /scoreboard commands
 * for reliable team-based line rendering.
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
        createEnabledScoreboards(server);
        registerTickHandler();
        LOGGER.info("ScoreboardManager initialized with {} configured scoreboards", config.size());
    }

    public static void reload(MinecraftServer server) {
        removeAllScoreboards(server);
        config.clear();
        loadConfig();
        createEnabledScoreboards(server);
        LOGGER.info("ScoreboardManager reloaded");
    }

    public static Map<String, ScoreboardConfig> getConfiguredScoreboards() {
        return Collections.unmodifiableMap(config);
    }

    // ---- Internal ----

    private static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 20 == 0) { // check every second
                updateAllScoreboards(server);
            }
        });
    }

    private static void updateAllScoreboards(MinecraftServer server) {
        for (ScoreboardConfig cfg : config.values()) {
            if (!cfg.enabled) continue;
            if (cfg.updateInterval <= 0) continue;
            if (tickCounter % cfg.updateInterval != 0) continue;
            updateScoreboardLines(server, cfg);
        }
    }

    private static void updateScoreboardLines(MinecraftServer server, ScoreboardConfig cfg) {
        var dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();

        // Reset old fake player scores
        for (int i = 0; i < 15; i++) {
            String fp = "cm" + hash(cfg.name) + "l" + i;
            try {
                String cmd = "scoreboard players reset " + fp + " " + cfg.name;
                dispatcher.execute(dispatcher.parse(cmd, source));
            } catch (Exception ignored) {}
        }

        // Set new scores with teams for display text
        int scoreValue = cfg.lines.size() - 1;
        for (int lineIdx = 0; lineIdx < cfg.lines.size() && lineIdx < 15; lineIdx++) {
            String line = cfg.lines.get(lineIdx);
            String resolvedLine = resolvePlaceholders(line, server);
            resolvedLine = resolvedLine.replace("&", "§");
            if (resolvedLine.length() > 40) {
                resolvedLine = resolvedLine.substring(0, 40);
            }

            String fp = "cm" + hash(cfg.name) + "l" + lineIdx;
            String tn = "cm" + hash(cfg.name) + "t" + lineIdx;
            try {
                // Create team if needed, set prefix text
                String addTeamCmd = "team add " + tn;
                try { dispatcher.execute(dispatcher.parse(addTeamCmd, source)); } catch (Exception ignored) {}
                String prefixCmd = "team modify " + tn + " prefix \"" +
                    resolvedLine.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                dispatcher.execute(dispatcher.parse(prefixCmd, source));
                // Join fake player to team
                String joinCmd = "team join " + tn + " " + fp;
                try { dispatcher.execute(dispatcher.parse(joinCmd, source)); } catch (Exception ignored) {}
                // Set score
                String setCmd = "scoreboard players set " + fp + " " + cfg.name + " " + scoreValue;
                dispatcher.execute(dispatcher.parse(setCmd, source));
            } catch (Exception e) {
                LOGGER.debug("Scoreboard line update failed for '{}' line {}: {}", cfg.name, lineIdx, e.getMessage());
            }
            scoreValue--;
        }
    }

    private static void createEnabledScoreboards(MinecraftServer server) {
        var dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();

        for (ScoreboardConfig cfg : config.values()) {
            if (!cfg.enabled) {
                LOGGER.info("Scoreboard '{}' is disabled — skipping", cfg.name);
                continue;
            }
            String displayName = resolvePlaceholders(cfg.displayName, null).replace("&", "§");
            try {
                // Remove existing
                String removeCmd = "scoreboard objectives remove " + cfg.name;
                try { dispatcher.execute(dispatcher.parse(removeCmd, source)); } catch (Exception ignored) {}

                // Create
                String createCmd = "scoreboard objectives add " + cfg.name + " dummy \"" +
                    displayName.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                dispatcher.execute(dispatcher.parse(createCmd, source));

                // Set display slot
                String slotName = switch (cfg.slot.toLowerCase()) {
                    case "list" -> "list";
                    case "belowname" -> "belowname";
                    default -> "sidebar";
                };
                String displayCmd = "scoreboard objectives setdisplay " + slotName + " " + cfg.name;
                dispatcher.execute(dispatcher.parse(displayCmd, source));

                LOGGER.info("Created scoreboard '{}' in slot '{}' (enabled)", cfg.name, cfg.slot);
            } catch (Exception e) {
                LOGGER.error("Failed to create scoreboard '{}': {}", cfg.name, e.getMessage());
            }
        }
    }

    private static void removeAllScoreboards(MinecraftServer server) {
        var dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();
        for (ScoreboardConfig cfg : config.values()) {
            try {
                String cmd = "scoreboard objectives remove " + cfg.name;
                dispatcher.execute(dispatcher.parse(cmd, source));
            } catch (Exception ignored) {}
        }
    }

    // Simple hash for compact fake player / team names
    private static int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = h * 31 + s.charAt(i);
        }
        return Math.abs(h) % 100000;
    }

    // ---- Placeholder resolution ----

    private static String resolvePlaceholders(String text, MinecraftServer server) {
        if (text == null) return "";
        if (server != null) {
            int online = server.getPlayerManager().getPlayerList().size();
            int max = server.getPlayerManager().getMaxPlayerCount();
            text = text.replace("${player_count}", String.valueOf(online));
            text = text.replace("${server_online}", String.valueOf(online));
            text = text.replace("${server_max}", String.valueOf(max));
            text = text.replace("${tps}", String.format("%.1f", getAverageTPS(server)));
        }
        return text;
    }

    private static float getAverageTPS(MinecraftServer server) {
        try {
            long[] tickTimes = server.getTickTimes();
            if (tickTimes != null && tickTimes.length > 0) {
                long sum = 0; int count = 0;
                for (int i = tickTimes.length - 1; i >= 0 && count < 100; i--) {
                    sum += tickTimes[i]; count++;
                }
                if (count > 0) {
                    float avg = sum / (float) count / 1000000f;
                    return avg > 0 ? Math.min(1000f / avg, 20f) : 20f;
                }
            }
        } catch (Exception ignored) {}
        return 20.0f;
    }

    // ---- Config persistence ----

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
                            cfg.enabled = obj.has("enabled") ? obj.get("enabled").getAsBoolean() : false;
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
                obj.addProperty("enabled", cfg.enabled);
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
            example.addProperty("enabled", false);
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
            root.addProperty("_comment", "Set \"enabled\": true to show a scoreboard. Available placeholders: ${player_count}, ${server_max}, ${tps}. Max 15 lines per sidebar. Use & for color codes.");
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("Created default scoreboards config (example is disabled)");
        } catch (Exception e) {
            LOGGER.error("Failed to create default scoreboards config", e);
        }
    }
}
