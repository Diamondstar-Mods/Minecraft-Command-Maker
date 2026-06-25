package com.example;

import com.google.gson.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Module Manager — handles .cmk file import/export.
 *
 * A .cmk file is a ZIP archive containing:
 *   module.json          — metadata (name, version, author, description)
 *   aliases.json         — aliases to import
 *   syntax.json          — syntax definitions
 *   events.json          — event triggers
 *   scoreboards.json     — scoreboard definitions
 *   cooldowns.json       — cooldown configs
 *   variables.json       — preset variables
 *   functions/           — .mcfunction files
 *   permissions.json     — (optional) permission grants
 */
public class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("cmdmaker-modules");
    private static final Path MODULES_PATH = Paths.get("config", "CommandMaker", "packages");

    /**
     * Metadata for a .cmk module.
     */
    public static class ModuleInfo {
        public String name;
        public String version;
        public String author;
        public String description;
        public String cmVersion; // Command Maker version this was made for

        public ModuleInfo() {
            this.name = "unknown";
            this.version = "1.0";
            this.author = "";
            this.description = "";
            this.cmVersion = "3.x";
        }
    }

    // ---- Export ----

    /**
     * Export current configuration as a .cmk module file.
     *
     * @param moduleName       name for the module
     * @param includeFunctions whether to include .mcfunction files
     * @param source           command source for feedback
     * @return true if export succeeded
     */
    public static boolean exportModule(String moduleName, boolean includeFunctions, CommandSourceStack source) {
        try {
            Files.createDirectories(MODULES_PATH);
            Path outputFile = MODULES_PATH.resolve(moduleName + ".cmk");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile.toFile()))) {

                // module.json metadata
                JsonObject meta = new JsonObject();
                meta.addProperty("name", moduleName);
                meta.addProperty("version", "1.0");
                meta.addProperty("author", source.getTextName());
                meta.addProperty("description", "Exported from Command Maker");
                meta.addProperty("cmVersion", "3.2.1");
                addZipEntry(zos, "module.json", new GsonBuilder().setPrettyPrinting().create().toJson(meta).getBytes());

                // aliases.json
                if (!AliasManager.getAliases().isEmpty()) {
                    JsonObject aliases = new JsonObject();
                    for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                        aliases.addProperty(entry.getKey(), entry.getValue());
                    }
                    addZipEntry(zos, "aliases.json", new GsonBuilder().setPrettyPrinting().create().toJson(aliases).getBytes());
                }

                // syntax.json
                Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                if (!syntaxes.isEmpty()) {
                    JsonObject syntaxObj = new JsonObject();
                    for (CommandSyntax cs : syntaxes.values()) {
                        JsonObject s = new JsonObject();
                        s.addProperty("pattern", cs.getPattern());
                        s.addProperty("description", cs.getDescription());
                        syntaxObj.add(cs.getName(), s);
                    }
                    addZipEntry(zos, "syntax.json", new GsonBuilder().setPrettyPrinting().create().toJson(syntaxObj).getBytes());
                }

                // events.json — copy from config if exists
                Path eventsFile = Paths.get("config", "CommandMaker", "events.json");
                if (Files.exists(eventsFile)) {
                    addZipEntry(zos, "events.json", Files.readAllBytes(eventsFile));
                }

                // scoreboards.json — copy from config if exists
                Path scoreboardFile = Paths.get("config", "CommandMaker", "scoreboards.json");
                if (Files.exists(scoreboardFile)) {
                    addZipEntry(zos, "scoreboards.json", Files.readAllBytes(scoreboardFile));
                }

                // cooldowns.json — copy from config if exists
                Path cooldownFile = Paths.get("config", "CommandMaker", "cooldowns.json");
                if (Files.exists(cooldownFile)) {
                    addZipEntry(zos, "cooldowns.json", Files.readAllBytes(cooldownFile));
                }

                // functions
                if (includeFunctions) {
                    Path functionsDir = AliasManager.getFunctionsPath();
                    if (Files.exists(functionsDir)) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(functionsDir, "*.mcfunction")) {
                            for (Path funcFile : stream) {
                                String funcName = funcFile.getFileName().toString();
                                addZipEntry(zos, "functions/" + funcName, Files.readAllBytes(funcFile));
                            }
                        }
                    }
                }

                // variables.json — export preset variables (player-specific vars not exported)
                JsonObject varsObj = new JsonObject();
                varsObj.addProperty("_comment", "Variables to set when this module is imported");
                varsObj.add("variables", new JsonObject());
                addZipEntry(zos, "variables.json", new GsonBuilder().setPrettyPrinting().create().toJson(varsObj).getBytes());
            }

            source.sendSuccess(() -> Component.literal("§a✔ Module exported: §f" + moduleName + ".cmk §7| " + outputFile), false);
            LOGGER.info("Module exported: {}", outputFile);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to export module '{}'", moduleName, e);
            source.sendSuccess(() -> Component.literal("§c✖ Export failed: §7" + e.getMessage()), false);
            return false;
        }
    }

    // ---- Import ----

    /**
     * Import a .cmk module file.
     *
     * @param moduleName name of the .cmk file (without extension)
     * @param overwrite  whether to overwrite existing aliases on conflict
     * @param source     command source for feedback
     * @return true if import succeeded
     */
    public static boolean importModule(String moduleName, boolean overwrite, CommandSourceStack source) {
        try {
            Path moduleFile = MODULES_PATH.resolve(moduleName + ".cmk");
            if (!Files.exists(moduleFile)) {
                source.sendSuccess(() -> Component.literal("§c✖ Module file not found: §f" + moduleName + ".cmk"), false);
                return false;
            }

            ModuleInfo info = new ModuleInfo();
            List<String> imported = new ArrayList<>();
            List<String> skipped = new ArrayList<>();

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(moduleFile.toFile()))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];

                // First pass: read module.json for metadata
                Map<String, byte[]> entries = new LinkedHashMap<>();
                while ((entry = zis.getNextEntry()) != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    entries.put(entry.getName(), baos.toByteArray());
                    zis.closeEntry();
                }

                // Process module.json
                if (entries.containsKey("module.json")) {
                    String metaJson = new String(entries.get("module.json"));
                    JsonObject meta = JsonParser.parseString(metaJson).getAsJsonObject();
                    info.name = meta.has("name") ? meta.get("name").getAsString() : moduleName;
                    info.version = meta.has("version") ? meta.get("version").getAsString() : "1.0";
                    info.author = meta.has("author") ? meta.get("author").getAsString() : "";
                    info.description = meta.has("description") ? meta.get("description").getAsString() : "";
                    info.cmVersion = meta.has("cmVersion") ? meta.get("cmVersion").getAsString() : "3.x";
                }

                // Import aliases
                if (entries.containsKey("aliases.json")) {
                    String aliasJson = new String(entries.get("aliases.json"));
                    JsonObject aliases = JsonParser.parseString(aliasJson).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> e : aliases.entrySet()) {
                        String aliasName = e.getKey();
                        String target = e.getValue().getAsString();
                        if (AliasManager.hasAlias(aliasName) && !overwrite) {
                            skipped.add("alias:" + aliasName);
                        } else {
                            if (AliasManager.hasAlias(aliasName)) {
                                AliasManager.removeAlias(aliasName);
                            }
                            AliasManager.addAlias(aliasName, target);
                            imported.add("alias:" + aliasName);
                        }
                    }
                }

                // Import syntax definitions
                if (entries.containsKey("syntax.json")) {
                    // Merge into existing syntax config
                    Path syntaxFile = Paths.get("config", "CommandMaker", "syntax.json");
                    JsonObject existingSyntax = new JsonObject();
                    if (Files.exists(syntaxFile)) {
                        existingSyntax = JsonParser.parseString(new String(Files.readAllBytes(syntaxFile))).getAsJsonObject();
                    }
                    JsonObject newSyntax = JsonParser.parseString(new String(entries.get("syntax.json"))).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> e : newSyntax.entrySet()) {
                        if (existingSyntax.has(e.getKey()) && !overwrite) {
                            skipped.add("syntax:" + e.getKey());
                        } else {
                            existingSyntax.add(e.getKey(), e.getValue());
                            imported.add("syntax:" + e.getKey());
                        }
                    }
                    Files.write(syntaxFile, new GsonBuilder().setPrettyPrinting().create().toJson(existingSyntax).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    SyntaxManager.loadSyntaxDefinitions();
                }

                // Import events
                if (entries.containsKey("events.json")) {
                    Path eventsFile = Paths.get("config", "CommandMaker", "events.json");
                    JsonObject existingEvents = new JsonObject();
                    if (Files.exists(eventsFile)) {
                        existingEvents = JsonParser.parseString(new String(Files.readAllBytes(eventsFile))).getAsJsonObject();
                    }
                    JsonObject newEvents = JsonParser.parseString(new String(entries.get("events.json"))).getAsJsonObject();
                    if (newEvents.has("events")) {
                        JsonObject newEventsObj = newEvents.getAsJsonObject("events");
                        if (!existingEvents.has("events")) {
                            existingEvents.add("events", new JsonObject());
                        }
                        JsonObject existingEventsObj = existingEvents.getAsJsonObject("events");
                        for (Map.Entry<String, JsonElement> e : newEventsObj.entrySet()) {
                            if (existingEventsObj.has(e.getKey()) && !overwrite) {
                                skipped.add("event:" + e.getKey());
                            } else {
                                existingEventsObj.add(e.getKey(), e.getValue());
                                imported.add("event:" + e.getKey());
                            }
                        }
                    }
                    Files.write(eventsFile, new GsonBuilder().setPrettyPrinting().create().toJson(existingEvents).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }

                // Import scoreboards
                if (entries.containsKey("scoreboards.json")) {
                    Path sbFile = Paths.get("config", "CommandMaker", "scoreboards.json");
                    JsonObject existingSB = new JsonObject();
                    if (Files.exists(sbFile)) {
                        existingSB = JsonParser.parseString(new String(Files.readAllBytes(sbFile))).getAsJsonObject();
                    }
                    JsonObject newSB = JsonParser.parseString(new String(entries.get("scoreboards.json"))).getAsJsonObject();
                    if (newSB.has("scoreboards") && newSB.get("scoreboards").isJsonObject()) {
                        JsonObject newSBObj = newSB.getAsJsonObject("scoreboards");
                        if (!existingSB.has("scoreboards")) {
                            existingSB.add("scoreboards", new JsonObject());
                        }
                        JsonObject existingSBObj = existingSB.getAsJsonObject("scoreboards");
                        for (Map.Entry<String, JsonElement> e : newSBObj.entrySet()) {
                            if (existingSBObj.has(e.getKey()) && !overwrite) {
                                skipped.add("scoreboard:" + e.getKey());
                            } else {
                                existingSBObj.add(e.getKey(), e.getValue());
                                imported.add("scoreboard:" + e.getKey());
                            }
                        }
                    }
                    Files.write(sbFile, new GsonBuilder().setPrettyPrinting().create().toJson(existingSB).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }

                // Import cooldowns
                if (entries.containsKey("cooldowns.json")) {
                    Path cdFile = Paths.get("config", "CommandMaker", "cooldowns.json");
                    JsonObject existingCD = new JsonObject();
                    if (Files.exists(cdFile)) {
                        existingCD = JsonParser.parseString(new String(Files.readAllBytes(cdFile))).getAsJsonObject();
                    }
                    JsonObject newCD = JsonParser.parseString(new String(entries.get("cooldowns.json"))).getAsJsonObject();
                    if (newCD.has("cooldowns") && newCD.get("cooldowns").isJsonObject()) {
                        JsonObject newCDObj = newCD.getAsJsonObject("cooldowns");
                        if (!existingCD.has("cooldowns")) {
                            existingCD.add("cooldowns", new JsonObject());
                        }
                        JsonObject existingCDObj = existingCD.getAsJsonObject("cooldowns");
                        for (Map.Entry<String, JsonElement> e : newCDObj.entrySet()) {
                            if (existingCDObj.has(e.getKey()) && !overwrite) {
                                skipped.add("cooldown:" + e.getKey());
                            } else {
                                existingCDObj.add(e.getKey(), e.getValue());
                                imported.add("cooldown:" + e.getKey());
                            }
                        }
                    }
                    Files.write(cdFile, new GsonBuilder().setPrettyPrinting().create().toJson(existingCD).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }

                // Import functions
                for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                    if (e.getKey().startsWith("functions/") && e.getKey().endsWith(".mcfunction")) {
                        String funcName = e.getKey().substring("functions/".length());
                        Path destFile = AliasManager.getFunctionsPath().resolve(funcName);
                        if (Files.exists(destFile) && !overwrite) {
                            skipped.add("function:" + funcName);
                        } else {
                            Files.createDirectories(destFile.getParent());
                            Files.write(destFile, e.getValue(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            imported.add("function:" + funcName);
                        }
                    }
                }

                // Import variables
                if (entries.containsKey("variables.json")) {
                    String varJson = new String(entries.get("variables.json"));
                    JsonObject varRoot = JsonParser.parseString(varJson).getAsJsonObject();
                    if (varRoot.has("variables") && varRoot.get("variables").isJsonObject()) {
                        JsonObject variables = varRoot.getAsJsonObject("variables");
                        if (variables.size() > 0) {
                            imported.add("variables:" + variables.size() + " preset(s)");
                        }
                    }
                }
            }

            // Reload all
            AliasManager.loadAliases();

            // Send feedback
            String summary = String.format("§a✔ Imported §f%s§a v%s by §f%s§a: §f%d§a items",
                info.name, info.version, info.author, imported.size());
            source.sendSuccess(() -> Component.literal(summary), false);
            for (String item : imported) {
                source.sendSuccess(() -> Component.literal("  §a+ §f" + item), false);
            }
            if (!skipped.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§e⚠ Skipped §f" + skipped.size() + "§e items (use --overwrite to replace):"), false);
                for (String item : skipped) {
                    source.sendSuccess(() -> Component.literal("  §7- §f" + item), false);
                }
            }
            LOGGER.info("Module '{}' imported: {} items, {} skipped", info.name, imported.size(), skipped.size());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to import module '{}'", moduleName, e);
            source.sendSuccess(() -> Component.literal("§c✖ Import failed: §7" + e.getMessage()), false);
            return false;
        }
    }

    /**
     * List available .cmk module files.
     */
    public static List<String> listModules() {
        List<String> modules = new ArrayList<>();
        try {
            if (Files.exists(MODULES_PATH)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(MODULES_PATH, "*.cmk")) {
                    for (Path path : stream) {
                        String name = path.getFileName().toString();
                        modules.add(name.substring(0, name.length() - 4)); // remove .cmk
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to list modules", e);
        }
        Collections.sort(modules);
        return modules;
    }

    /**
     * Read metadata from a .cmk file without importing it.
     */
    public static ModuleInfo getModuleInfo(String moduleName) {
        try {
            Path moduleFile = MODULES_PATH.resolve(moduleName + ".cmk");
            if (!Files.exists(moduleFile)) return null;

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(moduleFile.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if ("module.json".equals(entry.getName())) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        String json = baos.toString();
                        JsonObject meta = JsonParser.parseString(json).getAsJsonObject();
                        ModuleInfo info = new ModuleInfo();
                        info.name = meta.has("name") ? meta.get("name").getAsString() : moduleName;
                        info.version = meta.has("version") ? meta.get("version").getAsString() : "?";
                        info.author = meta.has("author") ? meta.get("author").getAsString() : "?";
                        info.description = meta.has("description") ? meta.get("description").getAsString() : "";
                        info.cmVersion = meta.has("cmVersion") ? meta.get("cmVersion").getAsString() : "?";
                        return info;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to read module info for '{}'", moduleName, e);
        }
        return null;
    }

    /**
     * Get the modules directory path.
     */
    public static Path getModulesPath() {
        return MODULES_PATH;
    }

    // ---- Helpers ----

    private static void addZipEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }
}
