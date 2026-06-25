package com.example;

import com.google.gson.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class SyntaxManager {
    private static Logger logger;
    private static Path configFile;
    private static Path dataFolder;
    private static final Map<String, CommandSyntax> customSyntaxes = new LinkedHashMap<>();

    /** Bukkit/Paper/Folia/Purpur entry point */
    public static void init(Plugin plugin) {
        init(plugin.getDataFolder().toPath(), plugin.getLogger());
    }

    /** Sponge / generic entry point */
    public static void init(Path dataFolder, Logger logger) {
        SyntaxManager.logger = logger;
        SyntaxManager.dataFolder = dataFolder;
        configFile = dataFolder.resolve("syntax.json");
        loadSyntaxDefinitions();
    }

    public static void loadSyntaxDefinitions() {
        customSyntaxes.clear();
        try {
            if (!Files.exists(configFile)) createDefaultSyntaxConfig();

            String content = Files.readString(configFile);
            JsonElement element = JsonParser.parseString(content);

            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                for (String key : jsonObject.keySet()) {
                    JsonElement syntaxElement = jsonObject.get(key);
                    if (syntaxElement.isJsonObject()) {
                        JsonObject syntaxObj = syntaxElement.getAsJsonObject();
                        String pattern = syntaxObj.has("pattern") ? syntaxObj.get("pattern").getAsString() : "";
                        String description = syntaxObj.has("description") ? syntaxObj.get("description").getAsString() : "";

                        if (!pattern.isEmpty()) {
                            CommandSyntax syntax = new CommandSyntax(key, pattern, description);
                            customSyntaxes.put(key, syntax);
                            logger.info("Loaded custom syntax: " + key + " -> " + pattern);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to load syntax definitions: " + e.getMessage());
        }
    }

    private static void createDefaultSyntaxConfig() {
        try {
            Files.createDirectories(dataFolder);

            JsonObject root = new JsonObject();

            JsonObject tpaObj = new JsonObject();
            tpaObj.addProperty("pattern", "/tpa <player>");
            tpaObj.addProperty("description", "Teleport request to a player");
            root.add("tpa", tpaObj);

            JsonObject giveObj = new JsonObject();
            giveObj.addProperty("pattern", "/give <item> <amount>");
            giveObj.addProperty("description", "Give an item to sender");
            root.add("give", giveObj);

            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("pattern", "/msg <player> <message>");
            msgObj.addProperty("description", "Send a message to a player");
            root.add("msg", msgObj);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.writeString(configFile, json, StandardOpenOption.CREATE_NEW);
            logger.info("Created default syntax.json");
        } catch (Exception e) {
            logger.severe("Failed to create default syntax config: " + e.getMessage());
        }
    }

    public static CommandSyntax getSyntax(String name) {
        return customSyntaxes.get(name);
    }

    public static boolean hasSyntax(String name) {
        return customSyntaxes.containsKey(name);
    }

    public static Map<String, CommandSyntax> getAllSyntaxes() {
        return new LinkedHashMap<>(customSyntaxes);
    }

    public static SyntaxMatch matchInput(String input) {
        for (Map.Entry<String, CommandSyntax> entry : customSyntaxes.entrySet()) {
            Map<String, String> params = entry.getValue().extractParameters(input);
            if (!params.isEmpty()) {
                return new SyntaxMatch(entry.getKey(), entry.getValue(), params);
            }
        }
        return null;
    }

    public static class SyntaxMatch {
        public final String syntaxName;
        public final CommandSyntax syntax;
        public final Map<String, String> parameters;

        public SyntaxMatch(String syntaxName, CommandSyntax syntax, Map<String, String> parameters) {
            this.syntaxName = syntaxName;
            this.syntax = syntax;
            this.parameters = parameters;
        }
    }
}
