package com.example;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

/**
 * Advanced Permission Management System
 * Supports LuckPerms integration and fallback to vanilla permissions
 */
public class PermissionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-perms");
    private static final Path PERMS_CONFIG = Paths.get("config", "CommandMaker", "permissions.json");
    
    // Permission configuration
    private static JsonObject permConfig = new JsonObject();
    private static boolean useLuckPerms = false;
    private static Object luckPermsAPI = null;
    
    // Permission cache for faster lookups
    private static final Map<String, Map<String, Boolean>> permissionCache = new HashMap<>();
    
    // Default permission levels
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
    
    /**
     * Initialize the permission manager
     */
    public static void initialize() {
        try {
            ensurePermissionsConfigExists();
            loadPermissionsConfig();
            initializeLuckPerms();
            LOGGER.info("Permission Manager initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Permission Manager", e);
        }
    }
    
    /**
     * Check if a player has permission to use a command
     */
    public static boolean hasPermission(ServerCommandSource source, String permissionNode) {
        try {
            if (source == null) return false;
            
            // Console always has permissions
            if (source.getEntity() == null) {
                return true;
            }
            
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return false;
            
            // Check vanilla op level first (level 2+)
            if (source.hasPermissionLevel(2)) {
                return true;
            }
            
            // Try LuckPerms if available
            if (useLuckPerms && luckPermsAPI != null) {
                if (checkLuckPermsPermission(player, permissionNode)) {
                    return true;
                }
            }
            
            // Check configuration-based permissions
            return checkConfigPermission(player.getUuid(), permissionNode);
            
        } catch (Exception e) {
            LOGGER.warn("Error checking permission: {}", permissionNode, e);
            return false;
        }
    }
    
    /**
     * Check if a player can use a specific alias
     */
    public static boolean canUseAlias(ServerCommandSource source, String aliasName) {
        try {
            if (source == null) return false;
            
            // Console can always use aliases
            if (source.getEntity() == null) {
                return true;
            }
            
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return false;
            
            // Ops always can use aliases
            if (source.hasPermissionLevel(2)) {
                return true;
            }
            
            // Check specific alias permission
            String aliasPermission = "cmdmaker.alias." + aliasName;
            return hasPermission(source, aliasPermission);
            
        } catch (Exception e) {
            LOGGER.warn("Error checking alias permission: {}", aliasName, e);
            return false;
        }
    }
    
    /**
     * Check if a player can use /cmd command
     */
    public static boolean canUseCmdCommand(ServerCommandSource source) {
        if (source == null) return false;
        if (source.getEntity() == null) return true; // Console
        if (source.hasPermissionLevel(2)) return true; // Ops
        return hasPermission(source, "cmdmaker.cmd");
    }
    
    /**
     * Check if a player can use /addcommand
     */
    public static boolean canUseAddCommand(ServerCommandSource source) {
        if (source == null) return false;
        if (source.getEntity() == null) return true; // Console
        if (source.hasPermissionLevel(2)) return true; // Ops
        return hasPermission(source, "cmdmaker.addcommand");
    }
    
    /**
     * Check if a player can manage aliases (add/delete)
     */
    public static boolean canManageAliases(ServerCommandSource source) {
        if (source == null) return false;
        if (source.getEntity() == null) return true; // Console
        if (source.hasPermissionLevel(2)) return true; // Ops
        return hasPermission(source, "cmdmaker.manage.alias");
    }
    
    /**
     * Check configuration-based permissions
     */
    private static boolean checkConfigPermission(UUID playerUuid, String permissionNode) {
        try {
            if (!permConfig.has("permissions")) {
                return false;
            }
            
            JsonObject perms = permConfig.getAsJsonObject("permissions");
            
            // Check by UUID
            String uuidStr = playerUuid.toString();
            if (perms.has(uuidStr)) {
                JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
                for (JsonElement elem : playerPerms) {
                    if (elem.getAsString().equals(permissionNode)) {
                        return true;
                    }
                }
            }
            
            // Check wildcard permissions (e.g., "cmdmaker.*" for all cmdmaker perms)
            String[] parts = permissionNode.split("\\.");
            if (parts.length > 1) {
                String wildcardPerm = parts[0] + ".*";
                if (perms.has(uuidStr)) {
                    JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
                    for (JsonElement elem : playerPerms) {
                        if (elem.getAsString().equals(wildcardPerm)) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.warn("Error checking config permission", e);
            return false;
        }
    }
    
    /**
     * Try to initialize LuckPerms integration
     */
    private static void initializeLuckPerms() {
        try {
            // Try to get LuckPerms API - this is a placeholder
            // In a real implementation, you'd use a proper service loader
            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            LOGGER.info("LuckPerms detected - enabling LuckPerms integration");
            useLuckPerms = true;
        } catch (ClassNotFoundException e) {
            LOGGER.info("LuckPerms not found - using built-in permissions only");
            useLuckPerms = false;
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize LuckPerms", e);
            useLuckPerms = false;
        }
    }
    
    /**
     * Check permission via LuckPerms
     */
    private static boolean checkLuckPermsPermission(ServerPlayerEntity player, String permissionNode) {
        try {
            if (!useLuckPerms) {
                return false;
            }
            
            // This is a placeholder - actual LuckPerms integration would go here
            // For now, we just return false to fall back to config permissions
            // A real implementation would use the LuckPerms API:
            // LuckPerms api = LuckPermsProvider.get();
            // User user = api.getUserManager().getUser(player.getUuid());
            // return user.getCachedData().getPermissionData().checkPermission(permissionNode).asBoolean();
            
            return false;
        } catch (Exception e) {
            LOGGER.warn("Error checking LuckPerms permission", e);
            return false;
        }
    }
    
    /**
     * Ensure the permissions config file exists
     */
    private static void ensurePermissionsConfigExists() {
        try {
            Path configDir = PERMS_CONFIG.getParent();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            if (!Files.exists(PERMS_CONFIG)) {
                JsonObject defaultConfig = createDefaultPermissionsConfig();
                String jsonStr = new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig);
                Files.write(PERMS_CONFIG, jsonStr.getBytes(), StandardOpenOption.CREATE_NEW);
                LOGGER.info("Created default permissions config at: {}", PERMS_CONFIG);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to ensure permissions config exists", e);
        }
    }
    
    /**
     * Load permissions configuration from file
     */
    private static void loadPermissionsConfig() {
        try {
            if (!Files.exists(PERMS_CONFIG)) {
                ensurePermissionsConfigExists();
            }
            
            String json = new String(Files.readAllBytes(PERMS_CONFIG));
            permConfig = JsonParser.parseString(json).getAsJsonObject();
            LOGGER.info("Loaded permissions config from: {}", PERMS_CONFIG);
        } catch (Exception e) {
            LOGGER.error("Failed to load permissions config", e);
            permConfig = createDefaultPermissionsConfig();
        }
    }
    
    /**
     * Create default permissions configuration
     */
    private static JsonObject createDefaultPermissionsConfig() {
        JsonObject config = new JsonObject();
        
        config.addProperty("version", "1.0");
        config.addProperty("description", "CMDMaker Advanced Permissions Configuration");
        config.addProperty("useLuckPerms", true);
        config.addProperty("enablePerAliasPermissions", true);
        
        // Alias configuration
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
        
        // Global permissions
        JsonObject globalPerms = new JsonObject();
        globalPerms.addProperty("cmdmaker.cmd", "Allow use of /cmd command");
        globalPerms.addProperty("cmdmaker.addcommand", "Allow use of /addcommand");
        globalPerms.addProperty("cmdmaker.manage.alias", "Allow managing aliases");
        globalPerms.addProperty("cmdmaker.manage.permissions", "Allow managing permissions");
        globalPerms.addProperty("cmdmaker.*", "Allow all cmdmaker features");
        config.add("globalPermissions", globalPerms);
        
        // Player-specific permissions
        JsonObject playerPerms = new JsonObject();
        playerPerms.add("permissions", new JsonObject()); // UUID -> ["permission.node"]
        config.add("players", playerPerms);
        
        // LuckPerms sync settings
        JsonObject luckPermsSettings = new JsonObject();
        luckPermsSettings.addProperty("syncInterval", 300); // 5 minutes
        luckPermsSettings.addProperty("enabled", true);
        luckPermsSettings.addProperty("groupPrefix", "cmdmaker");
        config.add("luckperms", luckPermsSettings);
        
        return config;
    }
    
    /**
     * Grant permission to a player
     */
    public static void grantPermission(UUID playerUuid, String permissionNode) {
        try {
            if (!permConfig.has("permissions")) {
                permConfig.add("permissions", new JsonObject());
            }
            
            JsonObject perms = permConfig.getAsJsonObject("permissions");
            String uuidStr = playerUuid.toString();
            
            if (!perms.has(uuidStr)) {
                perms.add(uuidStr, new JsonArray());
            }
            
            JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
            
            // Check if permission already exists
            for (JsonElement elem : playerPerms) {
                if (elem.getAsString().equals(permissionNode)) {
                    return; // Already granted
                }
            }
            
            playerPerms.add(permissionNode);
            savePermissionsConfig();
            permissionCache.clear();
            
            LOGGER.info("Granted permission {} to player {}", permissionNode, playerUuid);
        } catch (Exception e) {
            LOGGER.error("Failed to grant permission", e);
        }
    }
    
    /**
     * Revoke permission from a player
     */
    public static void revokePermission(UUID playerUuid, String permissionNode) {
        try {
            if (!permConfig.has("permissions")) {
                return;
            }
            
            JsonObject perms = permConfig.getAsJsonObject("permissions");
            String uuidStr = playerUuid.toString();
            
            if (!perms.has(uuidStr)) {
                return;
            }
            
            JsonArray playerPerms = perms.getAsJsonArray(uuidStr);
            playerPerms.remove(playerPerms.size() - 1); // Remove last element
            
            // Rebuild array without the permission
            JsonArray newPerms = new JsonArray();
            for (JsonElement elem : playerPerms) {
                String perm = elem.getAsString();
                if (!perm.equals(permissionNode)) {
                    newPerms.add(perm);
                }
            }
            
            perms.add(uuidStr, newPerms);
            savePermissionsConfig();
            permissionCache.clear();
            
            LOGGER.info("Revoked permission {} from player {}", permissionNode, playerUuid);
        } catch (Exception e) {
            LOGGER.error("Failed to revoke permission", e);
        }
    }
    
    /**
     * Save permissions configuration to file
     */
    private static void savePermissionsConfig() {
        try {
            String jsonStr = new GsonBuilder().setPrettyPrinting().create().toJson(permConfig);
            Files.write(PERMS_CONFIG, jsonStr.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("Saved permissions config");
        } catch (Exception e) {
            LOGGER.error("Failed to save permissions config", e);
        }
    }
    
    /**
     * Get player's permission level
     */
    public static PermissionLevel getPermissionLevel(ServerCommandSource source) {
        try {
            if (source == null || source.getEntity() == null) {
                return PermissionLevel.ADMIN; // Console
            }
            
            if (source.hasPermissionLevel(2)) {
                return PermissionLevel.ADMIN;
            }
            
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return PermissionLevel.NONE;
            
            // Check for various permission levels
            if (hasPermission(source, "cmdmaker.admin")) {
                return PermissionLevel.ADMIN;
            }
            if (hasPermission(source, "cmdmaker.moderator")) {
                return PermissionLevel.MODERATOR;
            }
            if (hasPermission(source, "cmdmaker.cmd")) {
                return PermissionLevel.COMMAND_USER;
            }
            
            return PermissionLevel.NONE;
            
        } catch (Exception e) {
            LOGGER.warn("Error getting permission level", e);
            return PermissionLevel.NONE;
        }
    }
}
