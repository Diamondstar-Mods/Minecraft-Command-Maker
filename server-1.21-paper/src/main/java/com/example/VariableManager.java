package com.example;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class VariableManager {
    private static final Logger LOGGER = Logger.getLogger("CommandMaker");
    private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

    public static String substituteVariables(String command, Player player) {
        Map<String, String> vars = new HashMap<>();
        try {
            if (player != null) {
                vars.put("player", player.getName());
                vars.put("x", String.valueOf(player.getLocation().getBlockX()));
                vars.put("y", String.valueOf(player.getLocation().getBlockY()));
                vars.put("z", String.valueOf(player.getLocation().getBlockZ()));
                UUID uuid = player.getUniqueId();
                if (playerVariables.containsKey(uuid)) {
                    vars.putAll(playerVariables.get(uuid));
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to resolve built-in variables: " + e.getMessage());
        }
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            command = command.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return command;
    }

    public static void setVariable(Player player, String key, String value) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        playerVariables.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
    }
}
