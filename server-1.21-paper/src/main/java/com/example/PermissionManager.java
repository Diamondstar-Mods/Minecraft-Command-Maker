package com.example;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class PermissionManager {
    private static final Logger LOGGER = Logger.getLogger("CommandMaker");
    private static File permsFile;
    private static JsonObject permConfig = new JsonObject();
    private static boolean useLuckPerms = false;

    public enum PermissionLevel {
        NONE(0, "Player with no permissions"),
        ALIAS_USER(1, "Can use allowed aliases"),
        COMMAND_USER(2, "Can use /addcommand and /cmd"),
        MODERATOR(3, "Can manage some aliases"),
        ADMIN(4, "Full access to all features");

        public final int level;
        public final String description;

        PermissionLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }
    }

    /** Bukkit/Paper/Folia/Purpur entry point */
    public static void init(Plugin plugin) {
        init(plugin.getDataFolder().toPath());
    }

    /** Sponge / generic entry point */
    public static void init(java.nio.file.Path dataFolder) {
        permsFile = dataFolder.resolve("permissions.json").toFile();
        try { java.nio.file.Files.createDirectories(dataFolder); } catch (Exception ignored) {}
        ensurePermissionsConfigExists();
        loadPermissionsConfig();
        initializeLuckPerms();
        LOGGER.info("Permission Manager initialized successfully");
    }

    public static boolean hasPermission(CommandSender sender, String permissionNode) {
        if (sender == null) return false;
        if (!(sender instanceof Player player)) return true; // Console
        if (player.isOp()) return true;
        if (player.hasPermission(permissionNode)) return true;
        if (useLuckPerms && checkLuckPermsPermission(player, permissionNode)) return true;
        return checkConfigPermission(player.getUniqueId(), permissionNode);
    }

    public static boolean canUseAlias(CommandSender sender, String aliasName) {
        if (sender == null) return false;
        if (!(sender instanceof Player)) return true;
        if (sender.isOp()) return true;
        String aliasPermission = "cmdmaker.alias." + aliasName;
        return hasPermission(sender, aliasPermission);
    }

    public static boolean canManageAliases(CommandSender sender) {
        if (sender == null) return false;
        if (!(sender instanceof Player)) return true;
        if (sender.isOp()) return true;
        return hasPermission(sender, "cmdmaker.manage.alias");
    }

    public static boolean canUseCmdCommand(CommandSender sender) {
        if (sender == null) return false;
        if (!(sender instanceof Player)) return true;
        if (sender.isOp()) return true;
        return hasPermission(sender, "cmdmaker.cmd");
    }

    public static boolean canUseAddCommand(CommandSender sender) {
        if (sender == null) return false;
        if (!(sender instanceof Player)) return true;
        if (sender.isOp()) return true;
        return hasPermission(sender, "cmdmaker.addcommand");
    }

    private static boolean checkConfigPermission(UUID playerUuid, String permissionNode) {
        try {
            if (!permConfig.has("permissions")) return false;

            JsonObject perms = permConfig.getAsJsonObject("permissions");
            String uuidStr = playerUuid.toString();

            if (perms.has(uuidStr)) {
                JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
                for (JsonElement elem : playerPerms) {
                    if (elem.getAsString().equals(permissionNode)) return true;
                }
            }

            String[] parts = permissionNode.split("\\.");
            if (parts.length > 1) {
                String wildcardPerm = parts[0] + ".*";
                if (perms.has(uuidStr)) {
                    JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
                    for (JsonElement elem : playerPerms) {
                        if (elem.getAsString().equals(wildcardPerm)) return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static void initializeLuckPerms() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                LOGGER.info("LuckPerms detected - using LuckPerms for permissions");
                useLuckPerms = true;
            } else {
                LOGGER.info("LuckPerms not found - using built-in permissions only");
                useLuckPerms = false;
            }
        } catch (Exception e) {
            useLuckPerms = false;
        }
    }

    private static boolean checkLuckPermsPermission(Player player, String permissionNode) {
        return player.hasPermission(permissionNode);
    }

    private static void ensurePermissionsConfigExists() {
        try {
            if (!permsFile.exists()) {
                JsonObject defaultConfig = createDefaultPermissionsConfig();
                String jsonStr = new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig);
                Files.write(permsFile.toPath(), jsonStr.getBytes(), StandardOpenOption.CREATE_NEW);
                LOGGER.info("Created default permissions config");
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to ensure permissions config exists: " + e.getMessage());
        }
    }

    private static void loadPermissionsConfig() {
        try {
            if (!permsFile.exists()) ensurePermissionsConfigExists();
            String json = new String(Files.readAllBytes(permsFile.toPath()));
            permConfig = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.severe("Failed to load permissions config: " + e.getMessage());
            permConfig = createDefaultPermissionsConfig();
        }
    }

    private static JsonObject createDefaultPermissionsConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("version", "1.0");
        config.addProperty("description", "CMDMaker Advanced Permissions Configuration");

        JsonObject aliases = new JsonObject();
        JsonObject exampleAlias = new JsonObject();
        exampleAlias.addProperty("permission", "cmdmaker.alias.home");
        exampleAlias.addProperty("description", "Allow players to use the home alias");
        exampleAlias.addProperty("enabled", true);
        aliases.add("home", exampleAlias);

        JsonObject tpAlias = new JsonObject();
        tpAlias.addProperty("permission", "cmdmaker.alias.tp");
        tpAlias.addProperty("description", "Allow players to use the teleport alias");
        tpAlias.addProperty("enabled", true);
        aliases.add("tp", tpAlias);
        config.add("aliases", aliases);

        JsonObject globalPerms = new JsonObject();
        globalPerms.addProperty("cmdmaker.cmd", "Allow use of /cmd command");
        globalPerms.addProperty("cmdmaker.addcommand", "Allow use of /addcommand");
        globalPerms.addProperty("cmdmaker.manage.alias", "Allow managing aliases");
        globalPerms.addProperty("cmdmaker.manage.permissions", "Allow managing permissions");
        globalPerms.addProperty("cmdmaker.*", "Allow all cmdmaker features");
        config.add("globalPermissions", globalPerms);

        JsonObject playerPerms = new JsonObject();
        playerPerms.add("permissions", new JsonObject());
        config.add("players", playerPerms);

        JsonObject luckPermsSettings = new JsonObject();
        luckPermsSettings.addProperty("syncInterval", 300);
        luckPermsSettings.addProperty("enabled", true);
        luckPermsSettings.addProperty("groupPrefix", "cmdmaker");
        config.add("luckperms", luckPermsSettings);

        return config;
    }

    public static PermissionLevel getPermissionLevel(CommandSender sender) {
        try {
            if (sender == null || !(sender instanceof Player)) return PermissionLevel.ADMIN;
            if (sender.isOp()) return PermissionLevel.ADMIN;
            if (hasPermission(sender, "cmdmaker.admin")) return PermissionLevel.ADMIN;
            if (hasPermission(sender, "cmdmaker.moderator")) return PermissionLevel.MODERATOR;
            if (hasPermission(sender, "cmdmaker.cmd")) return PermissionLevel.COMMAND_USER;
            return PermissionLevel.NONE;
        } catch (Exception e) {
            return PermissionLevel.NONE;
        }
    }
}
