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
        public List<String> lines;   // lines 0-14 (0 = top of sidebar when sorted descending)

        public ScoreboardConfig(String name) {
            this.name = name;
            this.displayName = "Scoreboard";
            this.slot = "sidebar";
            this.updateInterval = 20; // 1 second
            this.lines = new ArrayList<>();
        }

        public int getDisplaySlot() {
            return switch (slot.toLowerCase()) {
                case "list" -> ScoreboardDisplaySlot.LIST;
                case "belowname" -> ScoreboardDisplaySlot.BELOW_NAME;
                case "sidebar.team.black", "sidebar.team.dark_blue", "sidebar.team.dark_green",
                     "sidebar.team.dark_aqua", "sidebar.team.dark_red", "sidebar.team.dark_purple",
                     "sidebar.team.gold", "sidebar.team.gray", "sidebar.team.dark_gray",
                     "sidebar.team.blue", "sidebar.team.green", "sidebar.team.aqua",
                     "sidebar.team.red", "sidebar.team.light_purple", "sidebar.team.yellow",
                     "sidebar.team.white" -> ScoreboardDisplaySlot.SIDEBAR;
                default -> ScoreboardDisplaySlot.SIDEBAR;
            };
        }
    }

    /**
     * Initialize the scoreboard system.
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

            ScoreboardObjective objective = scoreboard.getNullableObjective(cfg.name);
            if (objective == null) continue;

            // Update display name
            String resolvedName = resolvePlaceholders(cfg.displayName, server);
            scoreboard.updateObjective(objective,
                Text.literal(resolvedName.replace("&", "§")),
                objective.getCriterion(),
                objective.getDisplayName(),
                objective.getRenderType()
            );

            // Update dynamic lines
            updateDynamicLines(scoreboard, objective, cfg, server);
        }
    }

    private static void updateDynamicLines(ServerScoreboard scoreboard, ScoreboardObjective objective,
                                            ScoreboardConfig cfg, MinecraftServer server) {
        int onlineCount = server.getPlayerManager().getPlayerList().size();
        int maxPlayers = server.getPlayerManager().getMaxPlayerCount();
        float tps = getAverageTPS(server);

        // Minecraft scoreboards use fake player names for each line
        // Each line needs a unique "player" name; we use color-coded entries
        // Lines are displayed from top to bottom
        // Score value determines ordering (higher = higher on sidebar with descending sort)

        int score = cfg.lines.size();
        for (String line : cfg.lines) {
            // Resolve placeholders in the line text
            String resolvedLine = resolvePlaceholders(line, server);
            resolvedLine = resolvedLine.replace("&", "§");

            // Truncate to 40 chars (Minecraft limit)
            if (resolvedLine.length() > 40) {
                resolvedLine = resolvedLine.substring(0, 40);
            }

            // Each line in a sidebar scoreboard is a ScoreHolder (fake player)
            // Use a color-code trick with zero-width characters to allow duplicate text lines
            String fakePlayer = generateFakePlayerName(cfg.lines.indexOf(line), cfg.name, score);
            ScoreAccess scoreAccess = scoreboard.getOrCreateScore(
                scoreboard.getScoreHolder(fakePlayer),
                objective,
                true
            );
            scoreAccess.setScore(score);

            // Update the display name of the team for this fake player
            updateFakePlayerTeam(scoreboard, fakePlayer, resolvedLine);

            score--;
        }
    }

    /**
     * Generate a unique fake player name for each scoreboard line.
     * Uses Minecraft's color code trick: §0 through §f are invisible zero-width chars.
     */
    private static String generateFakePlayerName(int lineIndex, String scoreboardName, int score) {
        // Use a unique but invisible name: § + color code based on index
        // Up to 16 unique entries per line index using the 16 color codes
        char color = (char) ('0' + (lineIndex % 10));
        return "§" + color + "§r" + scoreboardName + "_" + String.format("%02d", score);
    }

    private static void updateFakePlayerTeam(ServerScoreboard scoreboard, String fakePlayer, String text) {
        String teamName = "cm_" + sanitizeTeamName(fakePlayer);
        Team team = scoreboard.getNullableTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
            team.setDisplayName(Text.literal(text));
        }
        // Add the fake player to the team
        if (scoreboard.getScoreHolderTeam(fakePlayer) == null ||
            !teamName.equals(scoreboard.getScoreHolderTeam(fakePlayer).getName())) {
            scoreboard.removeScoreHolderFromTeam(fakePlayer, scoreboard.getScoreHolderTeam(fakePlayer));
            scoreboard.addScoreHolderToTeam(fakePlayer, team);
        }
        // Update the team prefix/suffix for the text
        team.setPrefix(Text.literal(text));
        team.setSuffix(Text.literal(""));
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
        ScoreboardObjective existing = scoreboard.getNullableObjective(cfg.name);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }

        String displayName = resolvePlaceholders(cfg.displayName, null);
        ScoreboardObjective objective = scoreboard.addObjective(
            cfg.name,
            ScoreboardCriterion.DUMMY,
            Text.literal(displayName.replace("&", "§")),
            ScoreboardCriterion.RenderType.INTEGER,
            false,
            null
        );

        scoreboard.setObjectiveSlot(cfg.getDisplaySlot(), objective);
        LOGGER.info("Created scoreboard '{}' in slot '{}'", cfg.name, cfg.slot);
    }

    private static void removeAllScoreboards(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        if (scoreboard == null) return;

        for (ScoreboardConfig cfg : config.values()) {
            ScoreboardObjective obj = scoreboard.getNullableObjective(cfg.name);
            if (obj != null) {
                scoreboard.removeObjective(obj);
            }
        }
    }

    private static String sanitizeTeamName(String name) {
        // Team names max 128 chars, alphanumeric + underscore
        return name.replaceAll("[^a-zA-Z0-9_]", "_").substring(0, Math.min(name.length(), 128));
    }

    // ---- Placeholder resolution for scoreboard lines ----

    private static String resolvePlaceholders(String text, MinecraftServer server) {
        if (text == null) return "";

        // Server-level placeholders
        if (server != null) {
            int online = server.getPlayerManager().getPlayerList().size();
            int max = server.getPlayerManager().getMaxPlayerCount();
            text = text.replace("${player_count}", String.valueOf(online));
            text = text.replace("${server_online}", String.valueOf(online));
            text = text.replace("${server_max}", String.valueOf(max));
            text = text.replace("${tps}", String.format("%.1f", getAverageTPS(server)));
        }

        // Note: player-specific placeholders (${player_name}, ${player_health}) are not
        // resolved here because scoreboards are server-wide. Use events or variables instead.

        return text;
    }

    private static float getAverageTPS(MinecraftServer server) {
        // Use the server's tick time to calculate TPS
        try {
            // Average tick time in milliseconds over the last 100 ticks
            long[] tickTimes = server.getTickTimes();
            if (tickTimes != null && tickTimes.length > 0) {
                long sum = 0;
                int count = 0;
                for (int i = tickTimes.length - 1; i >= 0 && count < 100; i--) {
                    sum += tickTimes[i];
                    count++;
                }
                if (count > 0) {
                    float avg = sum / (float) count / 1000000f; // nanos to millis
                    float tps = avg > 0 ? Math.min(1000f / avg, 20f) : 20f;
                    return tps;
                }
            }
        } catch (Exception ignored) {
            // getTickTimes might not be accessible or may throw
        }
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
