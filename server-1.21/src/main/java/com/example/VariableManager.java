package com.example;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");

    private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

    public static String substituteVariables(String command, CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        Map<String, String> vars = new HashMap<>();
        try {
            vars.put("player", source.getName());
            vars.put("x", String.valueOf(source.getPosition().x));
            vars.put("y", String.valueOf(source.getPosition().y));
            vars.put("z", String.valueOf(source.getPosition().z));
            UUID uuid = source.getPlayer() != null ? source.getPlayer().getUuid() : null;
            if (uuid != null && playerVariables.containsKey(uuid)) {
                vars.putAll(playerVariables.get(uuid));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve built-in variables for command substitution", e);
        }
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            command = command.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        // Resolve cooldown variables: ${cooldown_<alias>_remaining}
        ServerPlayerEntity player = null;
        try { player = source.getPlayer(); } catch (Exception ignored) {}
        if (player != null) {
            java.util.regex.Pattern cdPattern = java.util.regex.Pattern.compile("\\$\\{cooldown_([^}]+)_remaining\\}");
            java.util.regex.Matcher cdMatcher = cdPattern.matcher(command);
            StringBuffer cdSb = new StringBuffer();
            while (cdMatcher.find()) {
                String alias = cdMatcher.group(1);
                long remaining = CooldownManager.getRemaining(alias, player);
                cdMatcher.appendReplacement(cdSb, String.valueOf(remaining));
            }
            cdMatcher.appendTail(cdSb);
            command = cdSb.toString();
        }

        // Resolve %placeholder% patterns via PlaceholderManager
        command = PlaceholderManager.resolvePlaceholders(command, source);

        return command;
    }

    public static int setVariable(ServerCommandSource source, String key, String value) {
        UUID uuid;
        try {
            uuid = source.getPlayer().getUuid();
        } catch (Exception e) {
            source.sendFeedback(() -> net.minecraft.text.Text.literal("Only players can set variables."), false);
            return 0;
        }
        playerVariables.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
        source.sendFeedback(() -> net.minecraft.text.Text.literal("Set variable ${" + key + "} = " + value), false);
        return 1;
    }
}
