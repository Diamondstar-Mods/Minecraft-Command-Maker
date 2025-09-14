package com.diamondstar.dlb;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/**
 * Diamondstar Lib (DLB): Utility library for Command Maker mods. Provides helpers for config, JSON, and command registration.
 */
public class DLB {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Loads a JSON file from the given path, returns a JsonObject or null if error.
     */
    public static JsonObject loadJson(Path path) {
        try {
            if (!Files.exists(path)) return new JsonObject();
            String content = Files.readString(path);
            return JsonParser.parseString(content).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves a JsonObject to the given path.
     */
    public static boolean saveJson(Path path, JsonObject obj) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(obj));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that a string is valid JSON.
     */
    public static boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Utility to parse command arguments from a string.
     */
    public static List<String> parseArgs(String args) {
        if (args == null || args.isEmpty()) return List.of();
        return Arrays.asList(args.split(" "));
    }
}
