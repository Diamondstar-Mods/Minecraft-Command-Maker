package com.example;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;

public class AliasManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker");

    static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
    static final Path FUNCTIONS_PATH = Paths.get("config", "CommandMaker", "Functions");
    private static final Map<String, String> aliases = new LinkedHashMap<>();

    public static Path getFunctionsPath() {
        return FUNCTIONS_PATH;
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
        if (removed) {
            saveAliases();
        }
        return removed;
    }

    static void ensureConfigExists() {
        try {
            Path folder = CONFIG_PATH.getParent();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            if (!Files.exists(FUNCTIONS_PATH)) {
                Files.createDirectories(FUNCTIONS_PATH);
                Path exampleFunction = FUNCTIONS_PATH.resolve("mobsoff.mcfunction");
                if (!Files.exists(exampleFunction)) {
                    List<String> functionLines = Arrays.asList(
                        "# Example function: Disable monster spawns",
                        "spawn rates monster 0",
                        "say Monster spawns disabled!"
                    );
                    Files.write(exampleFunction, functionLines, StandardOpenOption.CREATE_NEW);
                }
            }
            if (!Files.exists(CONFIG_PATH)) {
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
                Files.write(CONFIG_PATH, lines, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create config folder or files", e);
        }
    }

    static void loadAliases() {
        aliases.clear();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                ensureConfigExists();
            }
            List<String> lines = Files.readAllLines(CONFIG_PATH);
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
                                LOGGER.warn("Skipping malformed alias entry: {}", entry.getKey(), e);
                            }
                        }
                        loaded = true;
                        LOGGER.info("Loaded {} aliases from JSON config", aliases.size());
                    }
                } catch (Exception je) {
                    LOGGER.warn("JSON parse failed, attempting legacy key=value format", je);
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
                        if (!key.isEmpty()) {
                            aliases.put(key, val);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Skipping malformed legacy entry: {}", l, e);
                    }
                }
                if (!aliases.isEmpty()) {
                    loaded = true;
                    LOGGER.info("Loaded {} aliases from legacy key=value format", aliases.size());
                }
            }

            if (!loaded) {
                LOGGER.info("No aliases loaded (config empty or all entries malformed). Server will continue normally.");
            }

            loadFunctionAliases();
        } catch (Exception e) {
            LOGGER.error("Failed to load aliases config (server will continue without aliases)", e);
        }
    }

    private static void loadFunctionAliases() {
        try {
            if (!Files.exists(FUNCTIONS_PATH)) {
                return;
            }
            int functionAliasesAdded = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FUNCTIONS_PATH, "*.mcfunction")) {
                for (Path functionFile : stream) {
                    String fileName = functionFile.getFileName().toString();
                    if (fileName.endsWith(".mcfunction")) {
                        String aliasName = fileName.substring(0, fileName.length() - 11);
                        String functionTarget = "function:" + aliasName;
                        if (!aliases.containsKey(aliasName)) {
                            aliases.put(aliasName, functionTarget);
                            functionAliasesAdded++;
                        }
                    }
                }
            }
            if (functionAliasesAdded > 0) {
                LOGGER.info("Auto-loaded {} function aliases from Functions folder", functionAliasesAdded);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to auto-load function aliases", e);
        }
    }

    static void saveAliases() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
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
            Files.write(CONFIG_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("Failed to save aliases config", e);
        }
    }
}
