package com.example;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");

    private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

    public static String substituteVariables(String command, CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Map<String, String> vars = new HashMap<>();
        try {
            vars.put("player", source.getTextName());
            vars.put("x", String.valueOf(source.getPosition().x));
            vars.put("y", String.valueOf(source.getPosition().y));
            vars.put("z", String.valueOf(source.getPosition().z));
            UUID uuid = source.getPlayer() != null ? source.getPlayer().getUUID() : null;
            if (uuid != null && playerVariables.containsKey(uuid)) {
                vars.putAll(playerVariables.get(uuid));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve built-in variables for command substitution", e);
        }
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            command = command.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return command;
    }

    public static int setVariable(CommandSourceStack source, String key, String value) {
        UUID uuid;
        try {
            uuid = source.getPlayer().getUUID();
        } catch (Exception e) {
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Only players can set variables."), false);
            return 0;
        }
        playerVariables.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Set variable ${" + key + "} = " + value), false);
        return 1;
    }
}
