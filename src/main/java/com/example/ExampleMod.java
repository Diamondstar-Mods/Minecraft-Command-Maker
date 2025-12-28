package com.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;


import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "cmdmaker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
	private static final Path TELEMETRY_CONFIG_PATH = Paths.get("config", "CommandMaker", "telementry.yml");
	private static final Path FUNCTIONS_PATH = Paths.get("config", "CommandMaker", "Functions");
	private static final Map<String, String> aliases = new HashMap<>();
	// Store per-player custom variables: player UUID -> (varName -> value)
	private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

	@Override
	public void onInitialize() {
		ensureConfigExists();
		loadAliases();
		SyntaxManager.loadSyntaxDefinitions();
		loadTelemetryConfig();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
			registerSetCmdVariable(dispatcher);
			registerDeleteAliasMenu(dispatcher);
			registerSyntaxCommand(dispatcher);
			registerCmdCommand(dispatcher);
		});
		
		LOGGER.info("Alias mod initialized!");
	}

	// Ensure config folder and config files exist, create with defaults if not
	private void ensureConfigExists() {
		try {
			Path folder = CONFIG_PATH.getParent();
			if (!Files.exists(folder)) {
				Files.createDirectories(folder);
			}
			if (!Files.exists(FUNCTIONS_PATH)) {
				Files.createDirectories(FUNCTIONS_PATH);
				// Create example function file
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
			if (!Files.exists(TELEMETRY_CONFIG_PATH)) {
				List<String> lines = new ArrayList<>();
				lines.add("# CommandMaker Telemetry Config");
				lines.add("# Set to true to allow anonymous telemetry (helps improve the mod)");
				lines.add("# Set to false to disable");
				lines.add("allow-telementry=true");
				Files.write(TELEMETRY_CONFIG_PATH, lines, StandardOpenOption.CREATE_NEW);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create config folder or files", e);
		}
	}

	// Load telemetry config and send request if enabled
	private void loadTelemetryConfig() {
		try {
			if (Files.exists(TELEMETRY_CONFIG_PATH)) {
				List<String> lines = Files.readAllLines(TELEMETRY_CONFIG_PATH);
				for (String line : lines) {
					line = line.trim();
					if (line.startsWith("allow-telementry=")) {
						String value = line.substring("allow-telementry=".length());
						if ("true".equalsIgnoreCase(value)) {
							sendTelemetryRequest();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to load telemetry config", e);
		}
	}

	// Send anonymous telemetry request in background
	private void sendTelemetryRequest() {
		new Thread(() -> {
			try {
				LOGGER.info("Sending anonymous telemetry request...");
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder()
					.uri(java.net.URI.create("https://commandmakerwiki.lucasgeitgey.com/redirects/telementry.html"))
					.timeout(java.time.Duration.ofSeconds(5))
					.build();
				HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
				LOGGER.info("Telemetry request sent successfully, response status: " + response.statusCode());
			} catch (Exception e) {
				LOGGER.warn("Telemetry request failed", e);
			}
		}).start();
	}

	// Register /addcommand (reload|add|del) ...
	private void registerAddCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("addcommand")
				.then(
					net.minecraft.server.command.CommandManager.literal("reload")
						.executes(ctx -> {
							loadAliases();
							// Remove all old aliases from dispatcher before re-registering
							for (String alias : new HashSet<>(aliases.keySet())) {
								dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
							}
							registerAliases(dispatcher);
							ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Aliases reloaded."), false);
							return 1;
						})
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("add")
						.then(
							net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
								.then(
									net.minecraft.server.command.CommandManager.argument("target", StringArgumentType.greedyString())
										.executes(ctx -> {
											String alias = StringArgumentType.getString(ctx, "alias");
											String target = StringArgumentType.getString(ctx, "target");
											aliases.put(alias, target);
											saveAliases();
											registerAlias(dispatcher, alias, target);
											ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Alias /" + alias + " -> " + target + " added."), false);
											return 1;
										})
								)
						)
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("del")
						.then(
							net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
								.executes(ctx -> {
									String alias = StringArgumentType.getString(ctx, "alias");
									if (aliases.remove(alias) != null) {
										saveAliases();
										dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
										ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Alias /" + alias + " removed."), false);
									} else {
										ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Alias /" + alias + " not found."), false);
									}
									return 1;
								})
						)
				)
		);
	}

	// Register /setcmdvariable <variable> <value> command
	private void registerSetCmdVariable(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("setcmdvariable")
				.then(net.minecraft.server.command.CommandManager.argument("variable", StringArgumentType.word())
					.then(net.minecraft.server.command.CommandManager.argument("value", StringArgumentType.greedyString())
						.executes(ctx -> {
							String var = StringArgumentType.getString(ctx, "variable");
							String value = StringArgumentType.getString(ctx, "value");
							ServerCommandSource source = ctx.getSource();
							UUID uuid = null;
							try {
								uuid = source.getPlayer().getUuid();
							} catch (Exception e) {
								source.sendFeedback(() -> net.minecraft.text.Text.literal("Only players can set variables."), false);
								return 0;
							}
							playerVariables.computeIfAbsent(uuid, k -> new HashMap<>()).put(var, value);
							source.sendFeedback(() -> net.minecraft.text.Text.literal("Set variable ${" + var + "} = " + value), false);
							return 1;
						})
					)
				)
		);
	}


	private void registerAliases(CommandDispatcher<ServerCommandSource> dispatcher) {
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			registerAlias(dispatcher, entry.getKey(), entry.getValue());
		}
	}

	private void registerAlias(CommandDispatcher<ServerCommandSource> dispatcher, String alias, String target) {
		// Remove old alias if re-registering
		dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal(alias)
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					if (target.startsWith("function:")) {
						String functionName = target.substring("function:".length());
						return executeFunction(functionName, ctx);
					} else {
						String command = substituteVariables(target, ctx);
						CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
						ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
						return cmdDispatcher.execute(parsed);
					}
				})
				.then(net.minecraft.server.command.CommandManager.argument("args", StringArgumentType.greedyString())
					.executes(ctx -> {
						if (target.startsWith("function:")) {
							String functionName = target.substring("function:".length());
							return executeFunction(functionName, ctx);
						}
						String args = StringArgumentType.getString(ctx, "args");
						String full = alias + " " + args;
						
						// Try to match against custom syntax definitions
						SyntaxManager.SyntaxMatch match = SyntaxManager.matchInput(full);
						String command;
						
						if (match != null) {
							// Custom syntax matched - substitute syntax parameters
							String substituted = match.syntax.substituteParameters(target, match.parameters);
							command = substituteVariables(substituted, ctx);
						} else {
							// No syntax match - use default behavior
							command = target + " " + args;
							command = substituteVariables(command, ctx);
						}
						
						ServerCommandSource source = ctx.getSource();
						CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
						ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
						return cmdDispatcher.execute(parsed);
					})
				)
		);
	}

	// Substitute variables in the command string, e.g. ${player}, ${x}, etc. and custom variables
	private String substituteVariables(String command, CommandContext<ServerCommandSource> ctx) {
		ServerCommandSource source = ctx.getSource();
		Map<String, String> vars = new HashMap<>();
		try {
			vars.put("player", source.getName());
			vars.put("x", String.valueOf(source.getPosition().x));
			vars.put("y", String.valueOf(source.getPosition().y));
			vars.put("z", String.valueOf(source.getPosition().z));
			// Add custom variables for this player
			UUID uuid = source.getPlayer() != null ? source.getPlayer().getUuid() : null;
			if (uuid != null && playerVariables.containsKey(uuid)) {
				vars.putAll(playerVariables.get(uuid));
			}
		} catch (Exception ignored) {}
		for (Map.Entry<String, String> entry : vars.entrySet()) {
			command = command.replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return command;
	}

	// Execute a function from config/CommandMaker/Functions/<functionName>.mcfunction
	private int executeFunction(String functionName, CommandContext<ServerCommandSource> ctx) {
		try {
			Path functionFile = FUNCTIONS_PATH.resolve(functionName + ".mcfunction");
			if (!Files.exists(functionFile)) {
				ctx.getSource().sendFeedback(() -> Text.literal("Function '" + functionName + "' not found."), false);
				return 0;
			}
			List<String> lines = Files.readAllLines(functionFile);
			CommandDispatcher<ServerCommandSource> cmdDispatcher = ctx.getSource().getServer().getCommandManager().getDispatcher();
			int executed = 0;
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				String command = substituteVariables(line, ctx);
				ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, ctx.getSource());
				cmdDispatcher.execute(parsed);
				executed++;
			}
			return executed;
		} catch (Exception e) {
			LOGGER.error("Failed to execute function '" + functionName + "'", e);
			ctx.getSource().sendFeedback(() -> Text.literal("Failed to execute function '" + functionName + "'."), false);
			return 0;
		}
	}

	private void loadAliases() {
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
				// Skip comments and empty lines
				if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
				// Collect for JSON parsing
				jsonBuilder.append(line).append("\n");
				// Also track potential legacy key=value lines
				if (!trimmed.startsWith("{") && trimmed.contains("=") && !trimmed.contains("{") && !trimmed.contains("}")) {
					legacyLines.add(line);
				}
			}
			String json = jsonBuilder.toString().trim();
			boolean loaded = false;
			
			// Try JSON parsing first
			if (!json.isEmpty() && !json.equals("{}")) {
				try {
					JsonElement pe = JsonParser.parseString(json);
					if (pe != null && pe.isJsonObject()) {
						JsonObject obj = pe.getAsJsonObject();
						for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
							try {
								aliases.put(entry.getKey(), entry.getValue().getAsString());
							} catch (Exception e) {
								LOGGER.warn("Skipping malformed alias entry: " + entry.getKey(), e);
							}
						}
						loaded = true;
						LOGGER.info("Loaded " + aliases.size() + " aliases from JSON config");
					}
				} catch (Exception je) {
					LOGGER.warn("JSON parse failed, attempting legacy key=value format", je);
				}
			}
			
			// Fallback to legacy key=value format if JSON failed
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
						LOGGER.warn("Skipping malformed legacy entry: " + l, e);
					}
				}
				if (!aliases.isEmpty()) {
					loaded = true;
					LOGGER.info("Loaded " + aliases.size() + " aliases from legacy key=value format");
				}
			}
			
			if (!loaded) {
				LOGGER.info("No aliases loaded (config empty or all entries malformed). Server will continue normally.");
			}
			
			// Auto-load function aliases for any .mcfunction files
			loadFunctionAliases();
		} catch (Exception e) {
			LOGGER.error("Failed to load aliases config (server will continue without aliases)", e);
		}
	}

	private void loadFunctionAliases() {
		try {
			if (!Files.exists(FUNCTIONS_PATH)) {
				return; // No functions folder, nothing to load
			}
			
			int functionAliasesAdded = 0;
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(FUNCTIONS_PATH, "*.mcfunction")) {
				for (Path functionFile : stream) {
					String fileName = functionFile.getFileName().toString();
					if (fileName.endsWith(".mcfunction")) {
						String aliasName = fileName.substring(0, fileName.length() - 11); // Remove .mcfunction
						String functionTarget = "function:" + aliasName;
						
						// Only add if not already defined in config (config takes priority)
						if (!aliases.containsKey(aliasName)) {
							aliases.put(aliasName, functionTarget);
							functionAliasesAdded++;
						}
					}
				}
			}
			
			if (functionAliasesAdded > 0) {
				LOGGER.info("Auto-loaded " + functionAliasesAdded + " function aliases from Functions folder");
			}
		} catch (Exception e) {
			LOGGER.error("Failed to auto-load function aliases", e);
		}
	}

	private void saveAliases() {
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

	private void registerDeleteAliasMenu(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("deletealias")
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					if (aliases.isEmpty()) {
						source.sendFeedback(() -> net.minecraft.text.Text.literal("§cNo aliases to delete."), false);
						return 0;
					}
					// Send a message listing all aliases with delete instructions
					source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Delete Aliases Menu:"), false);
					final int[] index = {1};
					for (String alias : aliases.keySet()) {
						String cmd = aliases.get(alias);
						int idx = index[0];
						source.sendFeedback(() -> net.minecraft.text.Text.literal("  §f[" + idx + "] §6/" + alias + " §7-> §f" + cmd), false);
						index[0]++;
					}
					source.sendFeedback(() -> net.minecraft.text.Text.literal("§7Use: §f/addcommand del <alias>§7 to delete"), false);
					return 1;
				})
				.then(net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
					.executes(ctx -> {
						String alias = StringArgumentType.getString(ctx, "alias");
						ServerCommandSource source = ctx.getSource();
						if (aliases.containsKey(alias)) {
							aliases.remove(alias);
							saveAliases();
							dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
							source.sendFeedback(() -> net.minecraft.text.Text.literal("§6[" + alias + "] was deleted"), false);
							return 1;
						} else {
							source.sendFeedback(() -> net.minecraft.text.Text.literal("§cAlias '§f" + alias + "§c' not found."), false);
							return 0;
						}
					})
				)
		);
	}

	/**
	 * Register /syntax command to view and test custom syntax definitions
	 */
	private void registerSyntaxCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("syntax")
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
					if (syntaxes.isEmpty()) {
						source.sendFeedback(() -> net.minecraft.text.Text.literal("§cNo custom syntaxes defined."), false);
						return 0;
					}
					source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Available Custom Syntaxes:"), false);
					for (String name : syntaxes.keySet()) {
						CommandSyntax syntax = syntaxes.get(name);
						String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
						source.sendFeedback(() -> net.minecraft.text.Text.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
					}
					return 1;
				})
		);
	}

	private int addCommand(ServerCommandSource source, String alias, String command, CommandDispatcher<ServerCommandSource> dispatcher) {
		if (aliases.containsKey(alias)) {
			source.sendFeedback(() -> net.minecraft.text.Text.literal("§cAlias '§f" + alias + "§c' already exists. Use /deletealias " + alias + " first."), false);
			return 0;
		}
		aliases.put(alias, command);
		saveAliases();
		registerAlias(dispatcher, alias, command);
		source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Added alias: §f/" + alias + " §7-> §f" + command), false);
		return 1;
	}

	private int deleteAlias(ServerCommandSource source, String alias, CommandDispatcher<ServerCommandSource> dispatcher) {
		if (aliases.containsKey(alias)) {
			aliases.remove(alias);
			saveAliases();
			dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
			source.sendFeedback(() -> net.minecraft.text.Text.literal("§6[" + alias + "] was deleted"), false);
			return 1;
		} else {
			source.sendFeedback(() -> net.minecraft.text.Text.literal("§cAlias '§f" + alias + "§c' not found."), false);
			return 0;
		}
	}

	private int reloadCommands(ServerCommandSource source, CommandDispatcher<ServerCommandSource> dispatcher) {
		loadAliases();
		SyntaxManager.loadSyntaxDefinitions();
		// Re-register aliases
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			registerAlias(dispatcher, entry.getKey(), entry.getValue());
		}
		source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Reloaded aliases and syntax definitions."), false);
		return 1;
	}

	private int listSyntax(ServerCommandSource source) {
		Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
		if (syntaxes.isEmpty()) {
			source.sendFeedback(() -> net.minecraft.text.Text.literal("§cNo custom syntaxes defined."), false);
			return 0;
		}
		source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Available Custom Syntaxes:"), false);
		for (String name : syntaxes.keySet()) {
			CommandSyntax syntax = syntaxes.get(name);
			String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
			source.sendFeedback(() -> net.minecraft.text.Text.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
		}
		return 1;
	}

	private int setVariable(ServerCommandSource source, String key, String value) {
		UUID uuid = null;
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

	/**
	 * Register /cmd command with subcommands
	 */
	private void registerCmdCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("cmd")
				.then(net.minecraft.server.command.CommandManager.literal("add")
					.then(net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
						.then(net.minecraft.server.command.CommandManager.argument("command", StringArgumentType.greedyString())
							.executes(ctx -> {
								String alias = StringArgumentType.getString(ctx, "alias");
								String command = StringArgumentType.getString(ctx, "command");
								return addCommand(ctx.getSource(), alias, command, dispatcher);
							})
						)
					)
				)
				.then(net.minecraft.server.command.CommandManager.literal("del")
					.then(net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
						.executes(ctx -> {
							String alias = StringArgumentType.getString(ctx, "alias");
							return deleteAlias(ctx.getSource(), alias, dispatcher);
						})
					)
				)
				.then(net.minecraft.server.command.CommandManager.literal("reload")
					.executes(ctx -> reloadCommands(ctx.getSource(), dispatcher))
				)
				.then(net.minecraft.server.command.CommandManager.literal("gui")
					.executes(ctx -> {
						ServerCommandSource source = ctx.getSource();
						source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Use §f/deletealiases-gui §6to open the GUI"), false);
						return 1;
					})
				)
				.then(net.minecraft.server.command.CommandManager.literal("syntax")
					.executes(ctx -> listSyntax(ctx.getSource()))
				)
				.then(net.minecraft.server.command.CommandManager.literal("setvar")
					.then(net.minecraft.server.command.CommandManager.argument("key", StringArgumentType.word())
						.then(net.minecraft.server.command.CommandManager.argument("value", StringArgumentType.greedyString())
							.executes(ctx -> {
								String key = StringArgumentType.getString(ctx, "key");
								String value = StringArgumentType.getString(ctx, "value");
								return setVariable(ctx.getSource(), key, value);
							})
						)
					)
				)
				.then(net.minecraft.server.command.CommandManager.literal("donate")
					.executes(ctx -> {
						ServerCommandSource source = ctx.getSource();
						source.sendFeedback(() -> Text.literal("§6§l❤️ Support us on Patreon! ❤️"), false);
						String tellrawCmd = "tellraw " + source.getName() + " {\"text\":\"https://www.patreon.com/15305135/join\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.patreon.com/15305135/join\"}}";
						CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
						ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(tellrawCmd, source);
						cmdDispatcher.execute(parsed);
						source.sendFeedback(() -> Text.literal("§7Click the link above to open in your browser!"), false);
						return 1;
					})
				)
				.then(net.minecraft.server.command.CommandManager.literal("wiki")
					.executes(ctx -> {
						ServerCommandSource source = ctx.getSource();
						source.sendFeedback(() -> Text.literal("§6§l📚 Command Maker Wiki 📚"), false);
						String tellrawCmd = "tellraw " + source.getName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com\"}}";
						CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
						ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(tellrawCmd, source);
						cmdDispatcher.execute(parsed);
						source.sendFeedback(() -> Text.literal("§7Click the link above to open the wiki in your browser!"), false);
						return 1;
					})
				)
		);
	}
}