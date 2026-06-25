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
 * This is a Fabric-native implementation. On the Paper/Bukkit module,
 * this can bridge to the real PlaceholderAPI while keeping the same
 * placeholder names for cross-platform module compatibility.
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
    // placeholder name -> (player, server) -> resolved value
    private static final Map<String, Function<PlaceholderContext, String>> customPlaceholders = new LinkedHashMap<>();

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
     * Resolve all %placeholder% patterns in the given text for a player.
     */
    public static String resolvePlaceholders(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty() || !text.contains("%")) {
            return text;
        }
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
                // no closing % — treat the rest as literal
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
     * Resolve placeholders using a ServerPlayerEntity directly.
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
     * @param name placeholder name (without % delimiters)
     * @param resolver function that returns the replacement string
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

    // ---- Internal resolution ----

    private static String resolveSinglePlaceholder(String placeholder, PlaceholderContext ctx) {
        String key = placeholder.toLowerCase();

        // Player-specific placeholders (require player to be online)
        if (ctx.player != null) {
            switch (key) {
                case "player":
                    return ctx.player.getName().getString();
                case "player_uuid":
                    return ctx.player.getUuid().toString();
                case "player_health":
                    return String.format("%.1f", ctx.player.getHealth());
                case "player_max_health":
                    return String.format("%.1f", ctx.player.getMaxHealth());
                case "player_xp":
                    return String.valueOf(ctx.player.totalExperience);
                case "player_xp_level":
                    return String.valueOf(ctx.player.experienceLevel);
                case "player_food":
                    return String.valueOf(ctx.player.getHungerManager().getFoodLevel());
                case "player_world":
                    return ctx.player.getCommandSource().getWorld().getRegistryKey().getValue().toString();
                case "player_gamemode":
                    return ctx.player.interactionManager.getGameMode().name();
                case "player_ping":
                    return String.valueOf(ctx.player.networkHandler.getLatency());
            }

            // %var_<name>% - custom variable
            if (key.startsWith("var_")) {
                String varName = placeholder.substring(4); // after "var_"
                // Access player variables through VariableManager
                UUID uuid = ctx.player.getUuid();
                return "TODO"; // Will be resolved through VariableManager integration
            }

            // %cooldown_<alias>% - cooldown remaining
            if (key.startsWith("cooldown_")) {
                String alias = placeholder.substring(9); // after "cooldown_"
                long remaining = CooldownManager.getRemaining(alias, ctx.player);
                return String.valueOf(remaining);
            }
        }

        // Server-level placeholders
        if (ctx.server != null) {
            switch (key) {
                case "server_online":
                    return String.valueOf(ctx.server.getPlayerManager().getPlayerList().size());
                case "server_max":
                    return String.valueOf(ctx.server.getPlayerManager().getMaxPlayerCount());
                case "server_tps":
                    return String.format("%.1f", getAverageTPS(ctx.server));
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

        return null; // unknown placeholder, keep as-is
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
}
