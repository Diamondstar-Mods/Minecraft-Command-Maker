package com.example;

import com.google.gson.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in Cooldown System for Command Maker aliases.
 * Supports per-player and global cooldowns with configurable messages.
 * Config: config/CommandMaker/cooldowns.json
 */
public class CooldownManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-cooldowns");
    private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "cooldowns.json");

    // aliasName -> playerUUID -> expireTimestamp (millis since epoch)
    private static final Map<String, Map<UUID, Long>> playerCooldowns = new ConcurrentHashMap<>();
    // aliasName -> expireTimestamp (millis since epoch)
    private static final Map<String, Long> globalCooldowns = new ConcurrentHashMap<>();
    // aliasName -> CooldownConfig
    private static final Map<String, CooldownConfig> config = new LinkedHashMap<>();

    /**
     * A single cooldown configuration entry.
     */
    public static class CooldownConfig {
        public String alias;
        public String type;      // "player" or "global"
        public int seconds;
        public String message;   // optional custom message, {remaining} gets replaced

        public CooldownConfig(String alias, String type, int seconds, String message) {
            this.alias = alias;
            this.type = type;
            this.seconds = seconds;
            this.message = message != null ? message : "&cPlease wait {remaining}s before using /" + alias + " again!";
        }
    }

    /**
     * Initialize cooldown system: load config from file.
     */
    public static void initialize() {
        loadConfig();
        LOGGER.info("CooldownManager initialized with {} configured cooldowns", config.size());
    }

    /**
     * Check if the given player is allowed to use the given alias.
     * If blocked, sends a cooldown message to the player and returns false.
     */
    public static boolean checkCooldown(String alias, CommandSourceStack source) {
        CooldownConfig cfg = config.get(alias);
        if (cfg == null) return true; // no cooldown configured

        ServerPlayer player = source.getPlayer();
        if (player == null) return true; // console always allowed

        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();

        if ("global".equals(cfg.type)) {
            Long expire = globalCooldowns.get(alias);
            if (expire != null && now < expire) {
                long remaining = (expire - now) / 1000;
                sendCooldownMessage(source, cfg, remaining);
                return false;
            }
            globalCooldowns.put(alias, now + cfg.seconds * 1000L);
        } else {
            // per-player cooldown
            Map<UUID, Long> playerMap = playerCooldowns.computeIfAbsent(alias, k -> new ConcurrentHashMap<>());
            Long expire = playerMap.get(uuid);
            if (expire != null && now < expire) {
                long remaining = (expire - now) / 1000;
                sendCooldownMessage(source, cfg, remaining);
                return false;
            }
            playerMap.put(uuid, now + cfg.seconds * 1000L);
        }
        return true;
    }

    /**
     * Get remaining cooldown seconds for a player on an alias, or 0 if none.
     */
    public static long getRemaining(String alias, ServerPlayer player) {
        if (player == null) return 0;
        CooldownConfig cfg = config.get(alias);
        if (cfg == null) return 0;

        long now = System.currentTimeMillis();

        if ("global".equals(cfg.type)) {
            Long expire = globalCooldowns.get(alias);
            if (expire != null && now < expire) {
                return (expire - now) / 1000;
            }
        } else {
            Map<UUID, Long> playerMap = playerCooldowns.get(alias);
            if (playerMap != null) {
                Long expire = playerMap.get(player.getUUID());
                if (expire != null && now < expire) {
                    return (expire - now) / 1000;
                }
            }
        }
        return 0;
    }

    /**
     * Get remaining cooldown seconds for a player name on an alias. Searches online players.
     */
    public static long getRemainingByName(String alias, String playerName, CommandSourceStack source) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayer(playerName);
        if (player != null) {
            return getRemaining(alias, player);
        }
        return 0;
    }

    /**
     * Set a cooldown configuration.
     */
    public static void setCooldown(String alias, String type, int seconds, String message) {
        CooldownConfig cfg = new CooldownConfig(alias, type, seconds, message);
        config.put(alias, cfg);
        saveConfig();
        LOGGER.info("Set {} cooldown for '{}': {} seconds", type, alias, seconds);
    }

    /**
     * Remove a cooldown configuration and clear any active cooldowns.
     */
    public static boolean clearCooldown(String alias) {
        playerCooldowns.remove(alias);
        globalCooldowns.remove(alias);
        boolean removed = config.remove(alias) != null;
        if (removed) {
            saveConfig();
        }
        return removed;
    }

    /**
     * Get all configured cooldowns.
     */
    public static Map<String, CooldownConfig> getConfiguredCooldowns() {
        return Collections.unmodifiableMap(config);
    }

    /**
     * Get a specific cooldown config.
     */
    public static CooldownConfig getConfig(String alias) {
        return config.get(alias);
    }

    private static void sendCooldownMessage(CommandSourceStack source, CooldownConfig cfg, long remaining) {
        String msg = cfg.message.replace("{remaining}", String.valueOf(remaining))
            .replace("&", "§");
        source.sendSuccess(() -> Component.literal(msg), false);
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
                if (root.has("cooldowns") && root.get("cooldowns").isJsonObject()) {
                    JsonObject cooldowns = root.getAsJsonObject("cooldowns");
                    for (Map.Entry<String, JsonElement> entry : cooldowns.entrySet()) {
                        try {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            String type = obj.has("type") ? obj.get("type").getAsString() : "player";
                            int seconds = obj.has("seconds") ? obj.get("seconds").getAsInt() : 30;
                            String message = obj.has("message") ? obj.get("message").getAsString() : null;
                            config.put(entry.getKey(), new CooldownConfig(entry.getKey(), type, seconds, message));
                        } catch (Exception e) {
                            LOGGER.warn("Skipping malformed cooldown entry: {}", entry.getKey(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load cooldowns config", e);
        }
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject();
            JsonObject cooldowns = new JsonObject();
            for (CooldownConfig cfg : config.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", cfg.type);
                obj.addProperty("seconds", cfg.seconds);
                obj.addProperty("message", cfg.message);
                cooldowns.add(cfg.alias, obj);
            }
            root.add("cooldowns", cooldowns);
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save cooldowns config", e);
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject();
            JsonObject cooldowns = new JsonObject();

            JsonObject example = new JsonObject();
            example.addProperty("type", "player");
            example.addProperty("seconds", 30);
            example.addProperty("message", "&cWait {remaining}s before using this again!");
            cooldowns.add("example_cooldown", example);

            root.add("cooldowns", cooldowns);
            root.addProperty("_comment", "Set \"type\" to \"player\" or \"global\". Use {remaining} in messages to show seconds left.");
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE_NEW);
            LOGGER.info("Created default cooldowns config");
        } catch (Exception e) {
            LOGGER.error("Failed to create default cooldowns config", e);
        }
    }
}
