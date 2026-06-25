package com.example;

import com.google.gson.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class AliasManager {
    private static Logger logger;
    private static Path dataFolder;
    private static Path configFile;
    private static Path functionsDir;
    private static final Map<String, String> aliases = new LinkedHashMap<>();

    /** Bukkit/Paper/Folia/Purpur entry point */
    public static void init(Plugin plugin) {
        init(plugin.getDataFolder().toPath(), plugin.getLogger());
    }

    /** Sponge / generic entry point */
    public static void init(Path dataFolder, Logger logger) {
        AliasManager.logger = logger;
        AliasManager.dataFolder = dataFolder;
        configFile = dataFolder.resolve("aliases.json");
        functionsDir = dataFolder.resolve("Functions");
        ensureConfigExists();
        loadAliases();
    }

    public static File getFunctionsDir() {
        return functionsDir.toFile();
    }

    public static Map<String, String> getAliases() {
        return Collections.unmodifiableMap(aliases);
    }

    public static boolean hasAlias(String name) {
        return aliases.containsKey(name);
    }

    public static String getAliasTarget(String name) {
        return aliases.get(name);
    }

    public static void addAlias(String name, String target) {
        aliases.put(name, target);
        saveAliases();
    }

    public static boolean removeAlias(String name) {
        boolean removed = aliases.remove(name) != null;
        if (removed) saveAliases();
        return removed;
    }

    private static void ensureConfigExists() {
        try {
            Files.createDirectories(dataFolder);
            if (!Files.exists(functionsDir)) {
                Files.createDirectories(functionsDir);
                Path exampleFunction = functionsDir.resolve("mobsoff.mcfunction");
                if (!Files.exists(exampleFunction)) {
                    List<String> functionLines = Arrays.asList(
                        "# Example function: Disable monster spawns",
                        "spawn rates monster 0",
                        "say Monster spawns disabled!"
                    );
                    Files.write(exampleFunction, functionLines, StandardOpenOption.CREATE_NEW);
                }
            }
            if (!Files.exists(configFile)) {
                List<String> lines = new ArrayList<>();
                lines.add("# CommandMaker Aliases Config");
                lines.add("# Each entry is an alias and its command target.");
                lines.add("# You can use variables like ${player}, ${x}, ${y}, ${z} in the command.");
                lines.add("# For functions, use \"function:name\" to reference config/CommandMaker/Functions/name.mcfunction");
                lines.add("# Example:");
                lines.add("#   teleport=tp ${player} 0 100 0");
                lines.add("#   greet=say Hello, ${player}!");
                lines.add("#   mobsoff=function:mobsoff");
                lines.add("{");
                lines.add("}");
                Files.write(configFile, lines, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            logger.severe("Failed to create config folder or files: " + e.getMessage());
        }
    }

    static void loadAliases() {
        aliases.clear();
        try {
            if (!Files.exists(configFile)) {
                ensureConfigExists();
            }
            List<String> lines = Files.readAllLines(configFile);
            StringBuilder jsonBuilder = new StringBuilder();
            List<String> legacyLines = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
                jsonBuilder.append(line).append("\n");
                if (!trimmed.startsWith("{") && trimmed.contains("=") && !trimmed.contains("{") && !trimmed.contains("}")) {
                    legacyLines.add(line);
                }
            }
            String json = jsonBuilder.toString().trim();
            boolean loaded = false;

            if (!json.isEmpty() && !json.equals("{}")) {
                try {
                    JsonElement pe = JsonParser.parseString(json);
                    if (pe != null && pe.isJsonObject()) {
                        JsonObject obj = pe.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                            try {
                                aliases.put(entry.getKey(), entry.getValue().getAsString());
                            } catch (Exception e) {
                                logger.warning("Skipping malformed alias entry: " + entry.getKey());
                            }
                        }
                        loaded = true;
                        logger.info("Loaded " + aliases.size() + " aliases from JSON config");
                    }
                } catch (Exception je) {
                    logger.warning("JSON parse failed, attempting legacy key=value format: " + je.getMessage());
                }
            }

            if (!loaded && !legacyLines.isEmpty()) {
                for (String l : legacyLines) {
                    String trimmed = l.trim();
                    if (trimmed.startsWith("#") || trimmed.startsWith("//") || !trimmed.contains("=")) continue;
                    try {
                        int idx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, idx).trim();
                        String val = trimmed.substring(idx + 1).trim();
                        if (!key.isEmpty()) aliases.put(key, val);
                    } catch (Exception e) {
                        logger.warning("Skipping malformed legacy entry: " + l);
                    }
                }
                if (!aliases.isEmpty()) {
                    loaded = true;
                    logger.info("Loaded " + aliases.size() + " aliases from legacy key=value format");
                }
            }

            if (!loaded) {
                logger.info("No aliases loaded (config empty or all entries malformed).");
            }

            loadFunctionAliases();
        } catch (Exception e) {
            logger.severe("Failed to load aliases config: " + e.getMessage());
        }
    }

    private static void loadFunctionAliases() {
        try {
            if (!Files.exists(functionsDir)) return;
            int count = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(functionsDir, "*.mcfunction")) {
                for (Path f : stream) {
                    String fileName = f.getFileName().toString();
                    String aliasName = fileName.substring(0, fileName.length() - 11);
                    String functionTarget = "function:" + aliasName;
                    if (!aliases.containsKey(aliasName)) {
                        aliases.put(aliasName, functionTarget);
                        count++;
                    }
                }
            }
            if (count > 0) {
                logger.info("Auto-loaded " + count + " function aliases from Functions folder");
            }
        } catch (Exception e) {
            logger.severe("Failed to auto-load function aliases: " + e.getMessage());
        }
    }

    static void saveAliases() {
        try {
            Files.createDirectories(dataFolder);
            List<String> lines = new ArrayList<>();
            lines.add("# CommandMaker Aliases Config");
            lines.add("# Each entry is an alias and its command target.");
            lines.add("# You can use variables like ${player}, ${x}, ${y}, ${z} in the command.");
            lines.add("# For functions, use \"function:name\" to reference config/CommandMaker/Functions/name.mcfunction");
            lines.add("# Example:");
            lines.add("#   teleport=tp ${player} 0 100 0");
            lines.add("#   greet=say Hello, ${player}!");
            lines.add("#   mobsoff=function:mobsoff");
            JsonObject obj = new JsonObject();
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                obj.addProperty(entry.getKey(), entry.getValue());
            }
            lines.add(obj.toString());
            Files.write(configFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.severe("Failed to save aliases config: " + e.getMessage());
        }
    }
}
