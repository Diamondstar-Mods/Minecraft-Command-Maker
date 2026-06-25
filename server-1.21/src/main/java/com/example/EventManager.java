package com.example;

import com.google.gson.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Event Trigger system for Command Maker.
 * Listens to Minecraft events (join, death, chat, block break, etc.) and
 * executes configured commands when they fire.
 *
 * Config: config/CommandMaker/events.json
 */
public class EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-events");
    private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "events.json");

    private static final Map<String, EventConfig> events = new LinkedHashMap<>();
    private static final Set<UUID> knownPlayers = new HashSet<>(); // for first-join detection
    private static boolean initialized = false;

    // Last kill tracking: source player UUID -> kill timestamp
    private static final Map<UUID, Long> recentKills = new HashMap<>();
    private static final Map<UUID, UUID> killMap = new HashMap<>(); // victim -> killer

    /**
     * An event configuration.
     */
    public static class EventConfig {
        public String eventType;
        public List<String> commands;
        public int cooldown; // seconds between firings, 0 = no cooldown
        public List<String> blocks; // optional block filter for block_break/block_place events

        public EventConfig(String eventType) {
            this.eventType = eventType;
            this.commands = new ArrayList<>();
            this.cooldown = 0;
            this.blocks = new ArrayList<>();
        }
    }

    /**
     * Initialize the event system: load config and register Fabric event listeners.
     */
    public static void initialize() {
        if (initialized) return;
        initialized = true;

        loadEvents();
        registerListeners();
        LOGGER.info("EventManager initialized with {} configured event types", events.size());
    }

    /**
     * Reload events from config file.
     */
    public static void reload() {
        loadEvents();
        LOGGER.info("EventManager reloaded events config");
    }

    // ---- Event firing ----

    /**
     * Fire an event by name. Substitutes variables and executes all commands for that event type.
     */
    public static void fireEvent(String eventType, ServerPlayerEntity player, Map<String, Object> context) {
        EventConfig cfg = events.get(eventType);
        if (cfg == null || cfg.commands.isEmpty()) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerCommandSource source = player.getCommandSource().withSilent();

        for (String rawCommand : cfg.commands) {
            try {
                String command = substituteEventVariables(rawCommand, player, context);
                var dispatcher = server.getCommandManager().getDispatcher();
                var parsed = dispatcher.parse(command, source);
                dispatcher.execute(parsed);
            } catch (Exception e) {
                LOGGER.error("Failed to execute event command for '{}': {}", eventType, e.getMessage());
            }
        }
    }

    private static String substituteEventVariables(String command, ServerPlayerEntity player, Map<String, Object> context) {
        String result = command;
        // Built-in variables
        result = result.replace("${player}", player.getName().getString());
        result = result.replace("${x}", String.valueOf((int) player.getX()));
        result = result.replace("${y}", String.valueOf((int) player.getY()));
        result = result.replace("${z}", String.valueOf((int) player.getZ()));
        result = result.replace("${uuid}", player.getUuid().toString());
        result = result.replace("${world}", player.getWorld().getRegistryKey().getValue().toString());

        // Context variables from event
        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                if (entry.getValue() != null) {
                    result = result.replace("${" + entry.getKey() + "}", entry.getValue().toString());
                }
            }
        }

        // Also apply standard VariableManager substitution (custom vars)
        // We call VariableManager separately since it needs a CommandContext.
        // For events, we handle basic substitutions here.

        // Placeholder resolution
        result = PlaceholderManager.resolvePlaceholders(result, player);

        return result;
    }

    // ---- Fabric event listeners ----

    private static void registerListeners() {
        // Player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            boolean isFirstJoin = knownPlayers.add(player.getUuid());
            if (isFirstJoin && events.containsKey("player_first_join")) {
                fireEvent("player_first_join", player, null);
            }
            if (events.containsKey("player_join")) {
                fireEvent("player_join", player, null);
            }
        });

        // Player leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (events.containsKey("player_leave")) {
                fireEvent("player_leave", player, null);
            }
        });

        // Player death
        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Track killer if another player
                if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                    killMap.put(player.getUuid(), killer.getUuid());
                    recentKills.put(killer.getUuid(), System.currentTimeMillis());
                }

                Map<String, Object> ctx = new HashMap<>();
                ctx.put("death_cause", damageSource.getName());
                if (damageSource.getAttacker() != null) {
                    ctx.put("killer", damageSource.getAttacker().getName().getString());
                }
                fireEvent("player_death", player, ctx);
            }
        });

        // Player respawn — we track via player respawn event
        // Actually use AFTER_DEATH + detect respawn via join tick difference...
        // Simpler: use ServerLivingEntityEvents.ALLOW_DEATH to track, and use
        // the next tick join as a respawn proxy.
        // For now, we handle respawn via the player's next interaction.
        // A practical approach: check if player was recently dead on next join/server tick.
        // We'll add respawn detection via a tick-based approach in ScoreboardManager integration.

        // Block break
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (events.containsKey("block_break")) {
                EventConfig cfg = events.get("block_break");
                String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
                if (cfg.blocks.isEmpty() || cfg.blocks.contains(blockId)) {
                    Map<String, Object> ctx = new HashMap<>();
                    ctx.put("block", blockId);
                    ctx.put("block_x", String.valueOf(pos.getX()));
                    ctx.put("block_y", String.valueOf(pos.getY()));
                    ctx.put("block_z", String.valueOf(pos.getZ()));
                    fireEvent("block_break", (ServerPlayerEntity) player, ctx);
                }
            }
        });

        // Chat message
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (events.containsKey("player_chat")) {
                ServerPlayerEntity player = sender;
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("message", message.getContent().getString());
                fireEvent("player_chat", player, ctx);
            }
        });

        // Tick counter (used by ScoreboardManager and for periodics)
        // Registered in ScoreboardManager instead to avoid duplication
    }

    // ---- Config persistence ----

    private static void loadEvents() {
        events.clear();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }
            String json = new String(Files.readAllBytes(CONFIG_PATH));
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                if (root.has("events") && root.get("events").isJsonObject()) {
                    JsonObject eventsObj = root.getAsJsonObject("events");
                    for (Map.Entry<String, JsonElement> entry : eventsObj.entrySet()) {
                        try {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            EventConfig cfg = new EventConfig(entry.getKey());
                            if (obj.has("commands") && obj.get("commands").isJsonArray()) {
                                for (JsonElement cmd : obj.getAsJsonArray("commands")) {
                                    cfg.commands.add(cmd.getAsString());
                                }
                            }
                            if (obj.has("cooldown")) {
                                cfg.cooldown = obj.get("cooldown").getAsInt();
                            }
                            if (obj.has("blocks") && obj.get("blocks").isJsonArray()) {
                                for (JsonElement block : obj.getAsJsonArray("blocks")) {
                                    cfg.blocks.add(block.getAsString());
                                }
                            }
                            events.put(entry.getKey(), cfg);
                        } catch (Exception e) {
                            LOGGER.warn("Skipping malformed event entry: {}", entry.getKey(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load events config", e);
        }
    }

    public static void saveEvents() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject();
            JsonObject eventsObj = new JsonObject();
            for (EventConfig cfg : events.values()) {
                JsonObject obj = new JsonObject();
                JsonArray cmdArray = new JsonArray();
                for (String cmd : cfg.commands) {
                    cmdArray.add(cmd);
                }
                obj.add("commands", cmdArray);
                if (cfg.cooldown > 0) {
                    obj.addProperty("cooldown", cfg.cooldown);
                }
                if (!cfg.blocks.isEmpty()) {
                    JsonArray blocksArray = new JsonArray();
                    for (String b : cfg.blocks) {
                        blocksArray.add(b);
                    }
                    obj.add("blocks", blocksArray);
                }
                eventsObj.add(cfg.eventType, obj);
            }
            root.add("events", eventsObj);
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save events config", e);
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject();
            JsonObject eventsObj = new JsonObject();

            String[] defaultEvents = {"player_join", "player_leave", "player_death", "player_chat",
                "block_break", "block_place", "player_first_join", "player_kill", "player_respawn"};
            for (String eventType : defaultEvents) {
                JsonObject obj = new JsonObject();
                obj.add("commands", new JsonArray());
                obj.addProperty("cooldown", 0);
                if ("block_break".equals(eventType) || "block_place".equals(eventType)) {
                    obj.add("blocks", new JsonArray());
                }
                eventsObj.add(eventType, obj);
            }

            // Add a working example
            JsonObject joinExample = eventsObj.getAsJsonObject("player_join");
            JsonArray joinCmds = new JsonArray();
            joinCmds.add("say Welcome ${player} to the server!");
            joinExample.add("commands", joinCmds);

            root.add("events", eventsObj);
            root.addProperty("_comment", "Available events: player_join, player_leave, player_death, player_chat, block_break, player_first_join, player_kill. Use ${player}, ${x}, ${y}, ${z}, ${world}, ${uuid}, ${block}, ${message}, ${death_cause}, ${killer} as variables.");
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE_NEW);
            LOGGER.info("Created default events config");
        } catch (Exception e) {
            LOGGER.error("Failed to create default events config", e);
        }
    }

    // ---- Public API ----

    /**
     * Get all configured events.
     */
    public static Map<String, EventConfig> getConfiguredEvents() {
        return Collections.unmodifiableMap(events);
    }

    /**
     * Get a specific event config.
     */
    public static EventConfig getEvent(String eventType) {
        return events.get(eventType);
    }

    /**
     * Check if an event type is configured.
     */
    public static boolean hasEvent(String eventType) {
        return events.containsKey(eventType);
    }
}
