package com.example;

import com.google.gson.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Custom Scoreboard system for Command Maker.
 * Allows creators to define scoreboards in config with placeholders,
 * update intervals, and dynamic lines.
 *
 * Config: config/CommandMaker/scoreboards.json
 */
public class ScoreboardManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-scoreboards");
    private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "scoreboards.json");

    private static final Map<String, ScoreboardConfig> config = new LinkedHashMap<>();
    private static int tickCounter = 0;
    private static boolean initialized = false;

    /**
     * A scoreboard configuration.
     */
    public static class ScoreboardConfig {
        public String name;
        public String displayName;   // supports & color codes
        public String slot;          // "sidebar", "list", "belowName"
        public int updateInterval;   // ticks between updates (20 = 1 second)
        public List<String> lines;   // lines 0-14

        public ScoreboardConfig(String name) {
            this.name = name;
            this.displayName = "Scoreboard";
            this.slot = "sidebar";
            this.updateInterval = 20;
            this.lines = new ArrayList<>();
        }

        /** Returns the int display slot constant: 0=list, 1=sidebar, 2=belowName */
        public int getDisplaySlot() {
            return switch (slot.toLowerCase()) {
                case "list" -> 0;
                case "belowname" -> 2;
                default -> 1; // sidebar
            };
        }
    }

    /**
     * Initialize the scoreboard system. Must be called after server starts.
     */
    public static void initialize(MinecraftServer server) {
        if (initialized) return;
        initialized = true;

        loadConfig();
        createAllScoreboards(server);
        registerTickHandler();
        LOGGER.info("ScoreboardManager initialized with {} configured scoreboards", config.size());
    }

    /**
     * Reload scoreboards from config.
     */
    public static void reload(MinecraftServer server) {
        removeAllScoreboards(server);
        config.clear();
        loadConfig();
        createAllScoreboards(server);
        LOGGER.info("ScoreboardManager reloaded");
    }

    /**
     * Get all configured scoreboards.
     */
    public static Map<String, ScoreboardConfig> getConfiguredScoreboards() {
        return Collections.unmodifiableMap(config);
    }

    // ---- Internal ----

    private static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            updateScoreboards(server);
        });
    }

    private static void updateScoreboards(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        if (scoreboard == null) return;

        for (ScoreboardConfig cfg : config.values()) {
            if (cfg.updateInterval <= 0) continue;
            if (tickCounter % cfg.updateInterval != 0) continue;

            ScoreboardObjective objective = scoreboard.getObjective(cfg.name);
            if (objective == null) continue;

            // Update display name by recreating the objective
            String resolvedName = resolvePlaceholders(cfg.displayName, server);
            Text displayName = Text.literal(resolvedName.replace("&", "§"));
            // Re-create with same slot to update display name
            scoreboard.removeObjective(objective);
            ScoreboardObjective newObj = scoreboard.addObjective(
                cfg.name, ScoreboardCriterion.DUMMY,
                displayName, ScoreboardCriterion.RenderType.INTEGER
            );
            scoreboard.setObjectiveSlot(cfg.getDisplaySlot(), newObj);

            // Update dynamic lines
            updateDynamicLines(scoreboard, newObj, cfg, server);
        }
    }

    private static void updateDynamicLines(ServerScoreboard scoreboard, ScoreboardObjective objective,
                                            ScoreboardConfig cfg, MinecraftServer server) {
        // Remove old scores for this objective (clear previous lines)
        // We use unique fake player names per scoreboard
        for (int i = 0; i < 15; i++) {
            String fakePlayer = "§" + (char)('0' + (i % 10)) + "§r" + cfg.name + "_" + String.format("%02d", i);
            scoreboard.resetPlayerScore(fakePlayer, objective);
        }

        int score = cfg.lines.size() - 1;
        for (int lineIdx = 0; lineIdx < cfg.lines.size(); lineIdx++) {
            String line = cfg.lines.get(lineIdx);
            String resolvedLine = resolvePlaceholders(line, server);
            resolvedLine = resolvedLine.replace("&", "§");
            if (resolvedLine.length() > 40) {
                resolvedLine = resolvedLine.substring(0, 40);
            }

            String fakePlayer = "§" + (char)('0' + (lineIdx % 10)) + "§r" + cfg.name + "_" + String.format("%02d", score);

            // Create/update team for this fake player to set display text
            String teamName = "cm_sb_" + cfg.name.hashCode() + "_" + lineIdx;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.addTeam(teamName);
            }
            team.setPrefix(Text.literal(resolvedLine));
            team.setSuffix(Text.literal(""));

            // Add fake player to team
            if (scoreboard.getTeam(fakePlayer) == null || !teamName.equals(scoreboard.getTeam(fakePlayer).getName())) {
                scoreboard.clearPlayerTeam(fakePlayer);
                scoreboard.addPlayerToTeam(fakePlayer, team);
            }

            // Set score value
            ScoreboardScore scoreAccess = scoreboard.getPlayerScore(fakePlayer, objective);
            scoreAccess.setScore(score);
            score--;
        }
    }

    private static void createAllScoreboards(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        if (scoreboard == null) return;

        for (ScoreboardConfig cfg : config.values()) {
            createScoreboard(scoreboard, cfg);
        }
    }

    private static void createScoreboard(ServerScoreboard scoreboard, ScoreboardConfig cfg) {
        // Remove existing if present
        ScoreboardObjective existing = scoreboard.getObjective(cfg.name);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }

        String displayName = resolvePlaceholders(cfg.displayName, null);
        ScoreboardObjective objective = scoreboard.addObjective(
            cfg.name,
            ScoreboardCriterion.DUMMY,
            Text.literal(displayName.replace("&", "§")),
            ScoreboardCriterion.RenderType.INTEGER
        );

        scoreboard.setObjectiveSlot(cfg.getDisplaySlot(), objective);
        LOGGER.info("Created scoreboard '{}' in slot '{}'", cfg.name, cfg.slot);
    }

    private static void removeAllScoreboards(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        if (scoreboard == null) return;

        for (ScoreboardConfig cfg : config.values()) {
            ScoreboardObjective obj = scoreboard.getObjective(cfg.name);
            if (obj != null) {
                scoreboard.removeObjective(obj);
            }
        }
    }

    // ---- Placeholder resolution for scoreboard lines ----

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
                long sum = 0;
                int count = 0;
                for (int i = tickTimes.length - 1; i >= 0 && count < 100; i--) {
                    sum += tickTimes[i];
                    count++;
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
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }
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
            JsonObject root = new JsonObject();
            JsonObject sbs = new JsonObject();
            for (ScoreboardConfig cfg : config.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("displayName", cfg.displayName);
                obj.addProperty("slot", cfg.slot);
                obj.addProperty("updateInterval", cfg.updateInterval);
                JsonArray linesArray = new JsonArray();
                for (String line : cfg.lines) {
                    linesArray.add(line);
                }
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
            JsonObject root = new JsonObject();
            JsonObject sbs = new JsonObject();

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
            root.addProperty("_comment", "Available placeholders: ${player_count}, ${server_max}, ${tps}. Max 15 lines per sidebar. Use & for color codes.");
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE_NEW);
            LOGGER.info("Created default scoreboards config");
        } catch (Exception e) {
            LOGGER.error("Failed to create default scoreboards config", e);
        }
    }
}
