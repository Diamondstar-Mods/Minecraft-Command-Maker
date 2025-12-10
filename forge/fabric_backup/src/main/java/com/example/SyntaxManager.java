package com.example;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Manages custom command syntax definitions
 * Loads from: config/CommandMaker/syntax.json
 */
public class SyntaxManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");
    private static final Path SYNTAX_CONFIG_PATH = Paths.get("config", "CommandMaker", "syntax.json");
    private static final Map<String, CommandSyntax> customSyntaxes = new HashMap<>();

    /**
     * Load all custom syntax definitions from config
     */
    public static void loadSyntaxDefinitions() {
        try {
            if (!Files.exists(SYNTAX_CONFIG_PATH)) {
                createDefaultSyntaxConfig();
            }

            String content = new String(Files.readAllBytes(SYNTAX_CONFIG_PATH));
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
                            LOGGER.info("Loaded custom syntax: {} -> {}", key, pattern);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load syntax definitions", e);
        }
    }

    /**
     * Create a default syntax.json with examples
     */
    private static void createDefaultSyntaxConfig() {
        try {
            Path folder = SYNTAX_CONFIG_PATH.getParent();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            JsonObject root = new JsonObject();

            // Example: TPA system
            JsonObject tpaObj = new JsonObject();
            tpaObj.addProperty("pattern", "/tpa <player>");
            tpaObj.addProperty("description", "Teleport request to a player");
            root.add("tpa", tpaObj);

            // Example: Give command
            JsonObject giveObj = new JsonObject();
            giveObj.addProperty("pattern", "/give <item> <amount>");
            giveObj.addProperty("description", "Give an item to sender");
            root.add("give", giveObj);

            // Example: Message command
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("pattern", "/msg <player> <message>");
            msgObj.addProperty("description", "Send a message to a player");
            root.add("msg", msgObj);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.write(SYNTAX_CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE_NEW);
            LOGGER.info("Created default syntax.json");
        } catch (Exception e) {
            LOGGER.error("Failed to create default syntax config", e);
        }
    }

    /**
     * Get a syntax definition by name
     */
    public static CommandSyntax getSyntax(String name) {
        return customSyntaxes.get(name);
    }

    /**
     * Check if a syntax exists
     */
    public static boolean hasSyntax(String name) {
        return customSyntaxes.containsKey(name);
    }

    /**
     * Get all registered syntaxes
     */
    public static Map<String, CommandSyntax> getAllSyntaxes() {
        return new HashMap<>(customSyntaxes);
    }

    /**
     * Try to match input against all registered syntaxes
     * Returns the matching syntax name and extracted parameters, or null if no match
     */
    public static SyntaxMatch matchInput(String input) {
        for (String syntaxName : customSyntaxes.keySet()) {
            CommandSyntax syntax = customSyntaxes.get(syntaxName);
            Map<String, String> params = syntax.extractParameters(input);
            if (!params.isEmpty()) {
                return new SyntaxMatch(syntaxName, syntax, params);
            }
        }
        return null;
    }

    /**
     * Helper class to store syntax match results
     */
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
