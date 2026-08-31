package com.example;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmakerclient");
    private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

    public static String substituteVariables(String command, Minecraft client) {
        if (client.player == null) return command;

        String result = command;

        // Built-in variables
        result = result.replace("${player}", client.player.getName().getString());
        result = result.replace("${x}", String.valueOf((int) client.player.getX()));
        result = result.replace("${y}", String.valueOf((int) client.player.getY()));
        result = result.replace("${z}", String.valueOf((int) client.player.getZ()));

        // Custom player variables
        UUID playerUUID = client.player.getUUID();
        Map<String, String> vars = playerVariables.get(playerUUID);
        if (vars != null) {
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }

        return result;
    }

    public static void setVariable(Minecraft client, String key, String value) {
        if (client.player == null) return;
        UUID uuid = client.player.getUUID();
        playerVariables.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
    }

    public static String getVariable(Minecraft client, String key) {
        if (client.player == null) return null;
        UUID uuid = client.player.getUUID();
        Map<String, String> vars = playerVariables.get(uuid);
        return vars != null ? vars.get(key) : null;
    }
}
