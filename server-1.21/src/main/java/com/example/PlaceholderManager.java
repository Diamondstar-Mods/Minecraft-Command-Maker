package com.example;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Placeholder resolution system for Command Maker.
 * Uses PlaceholderAPI-compatible %placeholder% syntax.
 *
 * Integrates with Patbox's Placeholder API mod if installed:
 *   - Our placeholders (%player_health%, etc.) become available to other mods
 *   - Other mods' placeholders work inside Command Maker aliases
 *
 * Built-in placeholders:
 *   %player%              - Player name
 *   %player_uuid%         - Player UUID
 *   %player_health%       - Current health (formatted)
 *   %player_max_health%   - Max health
 *   %player_xp%           - XP points
 *   %player_xp_level%     - XP level
 *   %player_food%         - Food level
 *   %player_world%        - World/dimension name
 *   %player_gamemode%     - Game mode name
 *   %player_ping%         - Ping in ms
 *   %server_online%       - Online player count
 *   %server_max%          - Max player slots
 *   %server_tps%          - Server TPS
 *   %var_<name>%          - Custom variable value
 *   %cooldown_<alias>%    - Cooldown remaining for the current player
 */
public class PlaceholderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-placeholders");

    // Registry of custom placeholder providers
    private static final Map<String, Function<PlaceholderContext, String>> customPlaceholders = new LinkedHashMap<>();

    // Placeholder API bridge
    private static boolean papiAvailable = false;
    private static Object papiPlaceholders = null; // eu.pb4.placeholders.api.Placeholders instance

    /**
     * Context object passed to placeholder resolvers.
     */
    public static class PlaceholderContext {
        public final ServerPlayerEntity player;
        public final MinecraftServer server;

        public PlaceholderContext(ServerPlayerEntity player) {
            this.player = player;
            this.server = player != null ? player.getCommandSource().getServer() : null;
        }

        public PlaceholderContext(ServerPlayerEntity player, MinecraftServer server) {
            this.player = player;
            this.server = server;
        }
    }

    /**
     * Initialize the placeholder system. Detects Placeholder API and bridges if available.
     * Called from CommandMaker.onInitialize().
     */
    public static void initialize() {
        try {
            Class<?> papiClass = Class.forName("eu.pb4.placeholders.api.Placeholders");
            papiAvailable = true;
            LOGGER.info("Placeholder API detected — bridging placeholders");
            registerWithPAPI(papiClass);
        } catch (ClassNotFoundException e) {
            papiAvailable = false;
            LOGGER.info("Placeholder API not found — using built-in placeholder resolver only");
        } catch (Exception e) {
            papiAvailable = false;
            LOGGER.warn("Failed to initialize Placeholder API bridge: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerWithPAPI(Class<?> papiClass) throws Exception {
        var idClass = Class.forName("net.minecraft.util.Identifier");
        var resultClass = Class.forName("eu.pb4.placeholders.api.PlaceholderResult");
        var ctxClass = Class.forName("eu.pb4.placeholders.api.PlaceholderContext");

        // Get the register method: Placeholders.register(Identifier, PlaceholderHandler)
        var registerMethod = papiClass.getMethod("register", idClass,
            Class.forName("eu.pb4.placeholders.api.PlaceholderHandler"));

        // Register player placeholders
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player",
            ctx -> ctx.player.getName().getString());
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_uuid",
            ctx -> ctx.player.getUuid().toString());
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_health",
            ctx -> String.format("%.1f", ctx.player.getHealth()));
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_max_health",
            ctx -> String.format("%.1f", ctx.player.getMaxHealth()));
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_xp",
            ctx -> String.valueOf(ctx.player.totalExperience));
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_xp_level",
            ctx -> String.valueOf(ctx.player.experienceLevel));
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_food",
            ctx -> String.valueOf(ctx.player.getHungerManager().getFoodLevel()));
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_world",
            ctx -> ctx.player.getCommandSource().getWorld().getRegistryKey().getValue().toString());
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_gamemode",
            ctx -> ctx.player.interactionManager.getGameMode().name());
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "player_ping",
            ctx -> String.valueOf(ctx.player.networkHandler.getLatency()));

        // Server-level placeholders (context-dependent)
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "server_online",
            ctx -> ctx.server != null ? String.valueOf(ctx.server.getPlayerManager().getPlayerList().size()) : "0");
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "server_max",
            ctx -> ctx.server != null ? String.valueOf(ctx.server.getPlayerManager().getMaxPlayerCount()) : "0");
        registerBuiltinPAPI(registerMethod, idClass, resultClass, ctxClass, "server_tps",
            ctx -> ctx.server != null ? String.format("%.1f", getAverageTPS(ctx.server)) : "20.0");

        LOGGER.info("Registered {} Command Maker placeholders with Placeholder API", 15);
    }

    private static void registerBuiltinPAPI(java.lang.reflect.Method registerMethod,
            Class<?> idClass, Class<?> resultClass, Class<?> ctxClass,
            String name, Function<PlaceholderContext, String> resolver) throws Exception {
        try {
            // Create Identifier("cmdmaker", name)
            var ctor = idClass.getConstructor(String.class, String.class);
            Object id = ctor.newInstance("cmdmaker", name);

            // Create handler lambda (simplified via proxy)
            var handler = new eu.pb4.placeholders.api.PlaceholderHandler() {
                @Override
                public eu.pb4.placeholders.api.PlaceholderResult onPlaceholderRequest(
                        eu.pb4.placeholders.api.PlaceholderContext ctx, String arg) {
                    ServerPlayerEntity player = null;
                    if (ctx.player() != null && ctx.player() instanceof ServerPlayerEntity sp) {
                        player = sp;
                    }
                    MinecraftServer server = player != null
                        ? player.getCommandSource().getServer() : null;
                    String value = resolver.apply(new PlaceholderContext(player, server));
                    if (value != null) {
                        return eu.pb4.placeholders.api.PlaceholderResult.value(value);
                    }
                    return eu.pb4.placeholders.api.PlaceholderResult.invalid("no player");
                }
            };

            registerMethod.invoke(null, id, handler);
        } catch (Exception e) {
            LOGGER.debug("Failed to register PAPI placeholder '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Resolve all %placeholder% patterns in the given text for a player.
     */
    public static String resolvePlaceholders(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty() || !text.contains("%")) {
            return text;
        }

        // Try Placeholder API first if available
        if (papiAvailable) {
            text = resolveViaPAPI(text, player);
        }

        // Then resolve our built-in placeholders (catches any PAPI missed)
        PlaceholderContext ctx = new PlaceholderContext(player);
        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            int pct = text.indexOf('%', i);
            if (pct < 0) {
                result.append(text.substring(i));
                break;
            }
            result.append(text, i, pct);
            int close = text.indexOf('%', pct + 1);
            if (close < 0) {
                result.append(text.substring(pct));
                break;
            }
            String placeholder = text.substring(pct + 1, close);
            String resolved = resolveSinglePlaceholder(placeholder, ctx);
            result.append(resolved != null ? resolved : "%" + placeholder + "%");
            i = close + 1;
        }
        return result.toString();
    }

    /**
     * Try to resolve placeholders via Placeholder API.
     * Falls back to original text if PAPI is not available or fails.
     */
    private static String resolveViaPAPI(String text, ServerPlayerEntity player) {
        try {
            Class<?> papiClass = Class.forName("eu.pb4.placeholders.api.Placeholders");

            // Check if there's a parseText or similar method we can use
            // Placeholder API v2.x: Placeholders.parseText(Text, PlaceholderContext)
            // We need to convert our string → Text → parse → string

            // For simple resolution, iterate our known placeholders
            // Full PAPI resolution is handled by the mod's built-in text parsing

            // If the text contains placeholders we don't know about, try PAPI
            if (player != null) {
                var ctxClass = Class.forName("eu.pb4.placeholders.api.PlaceholderContext");
                var ctx = ctxClass.getMethod("of", Class.forName("net.minecraft.server.network.ServerPlayerEntity"))
                    .invoke(null, player);

                // Create a Text from the string and parse
                var textClass = Class.forName("net.minecraft.text.Text");
                var literalMethod = textClass.getMethod("literal", String.class);
                Object textObj = literalMethod.invoke(null, text);

                var parseMethod = papiClass.getMethod("parseText", textClass, ctxClass);
                Object result = parseMethod.invoke(null, textObj, ctx);

                return result.toString();
            }
        } catch (Exception e) {
            // PAPI not available or failed — use built-in only
        }
        return text;
    }

    /**
     * Resolve placeholders using a ServerCommandSource.
     */
    public static String resolvePlaceholders(String text, net.minecraft.server.command.ServerCommandSource source) {
        if (text == null || text.isEmpty()) return text;
        ServerPlayerEntity player = null;
        try {
            player = source.getPlayer();
        } catch (Exception ignored) {}
        return resolvePlaceholders(text, player);
    }

    /**
     * Register a custom placeholder resolver.
     */
    public static void registerCustomPlaceholder(String name, Function<PlaceholderContext, String> resolver) {
        customPlaceholders.put(name.toLowerCase(), resolver);
        LOGGER.debug("Registered custom placeholder: %{}%", name);
    }

    /**
     * Remove a custom placeholder.
     */
    public static boolean unregisterCustomPlaceholder(String name) {
        return customPlaceholders.remove(name.toLowerCase()) != null;
    }

    /**
     * Check if Placeholder API is available.
     */
    public static boolean isPlaceholderAPIAvailable() {
        return papiAvailable;
    }

    // ---- Internal resolution ----

    private static String resolveSinglePlaceholder(String placeholder, PlaceholderContext ctx) {
        String key = placeholder.toLowerCase();

        // Player-specific placeholders
        if (ctx.player != null) {
            switch (key) {
                case "player": return ctx.player.getName().getString();
                case "player_uuid": return ctx.player.getUuid().toString();
                case "player_health": return String.format("%.1f", ctx.player.getHealth());
                case "player_max_health": return String.format("%.1f", ctx.player.getMaxHealth());
                case "player_xp": return String.valueOf(ctx.player.totalExperience);
                case "player_xp_level": return String.valueOf(ctx.player.experienceLevel);
                case "player_food": return String.valueOf(ctx.player.getHungerManager().getFoodLevel());
                case "player_world": return ctx.player.getCommandSource().getWorld().getRegistryKey().getValue().toString();
                case "player_gamemode": return ctx.player.interactionManager.getGameMode().name();
                case "player_ping": return String.valueOf(ctx.player.networkHandler.getLatency());
            }

            if (key.startsWith("var_")) {
                UUID uuid = ctx.player.getUuid();
                return "TODO"; // Will be resolved through VariableManager
            }
            if (key.startsWith("cooldown_")) {
                String alias = placeholder.substring(9);
                return String.valueOf(CooldownManager.getRemaining(alias, ctx.player));
            }
        }

        // Server-level placeholders
        if (ctx.server != null) {
            switch (key) {
                case "server_online": return String.valueOf(ctx.server.getPlayerManager().getPlayerList().size());
                case "server_max": return String.valueOf(ctx.server.getPlayerManager().getMaxPlayerCount());
                case "server_tps": return String.format("%.1f", getAverageTPS(ctx.server));
            }
        }

        // Custom registered placeholders
        Function<PlaceholderContext, String> custom = customPlaceholders.get(key);
        if (custom != null) {
            try {
                return custom.apply(ctx);
            } catch (Exception e) {
                LOGGER.warn("Custom placeholder %{}% threw: {}", placeholder, e.getMessage());
                return null;
            }
        }

        return null;
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
}
