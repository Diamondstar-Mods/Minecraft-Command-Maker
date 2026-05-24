package com.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.ParseResults;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
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
import java.util.concurrent.CompletableFuture;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "cmdmaker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
	private static final Path FUNCTIONS_PATH = Paths.get("config", "CommandMaker", "Functions");
	private static final Map<String, String> aliases = new HashMap<>();
	// Store per-player custom variables: player UUID -> (varName -> value)
	private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();

	@Override
	public void onInitialize() {
		ensureConfigExists();
		loadAliases();
		SyntaxManager.loadSyntaxDefinitions();
		PermissionManager.initialize();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registercmd(dispatcher);
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
			registerSetCmdVariable(dispatcher);
			registerDeleteAliasMenu(dispatcher);
			registerSyntaxCommand(dispatcher);
			registerCmdCommand(dispatcher);
			registerPermissionCommand(dispatcher);
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
		} catch (Exception e) {
			LOGGER.error("Failed to create config folder or files", e);
		}
	}
	// Register /cmd (reload|add|del) ...
	private void registercmd(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("cmd")
				.requires(source -> PermissionManager.canUseCmdCommand(source))
				.then(
					LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
						.executes(ctx -> {
							if (!PermissionManager.canManageAliases(ctx.getSource())) {
								ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to reload aliases."), false);
								return 0;
							}
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
				LiteralArgumentBuilder.<ServerCommandSource>literal("add")
					.then(
						RequiredArgumentBuilder.<ServerCommandSource>argument("alias", StringArgumentType.word())
							.then(
								RequiredArgumentBuilder.<ServerCommandSource>argument("target", StringArgumentType.greedyString())
										.executes(ctx -> {
											if (!PermissionManager.canManageAliases(ctx.getSource())) {
												ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to add aliases."), false);
												return 0;
											}
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
									if (!PermissionManager.canManageAliases(ctx.getSource())) {
										ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to delete aliases."), false);
										return 0;
									}
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
				.then(
					net.minecraft.server.command.CommandManager.literal("function")
						.then(
							net.minecraft.server.command.CommandManager.argument("functionName", StringArgumentType.word())
								.executes(ctx -> {
									String functionName = StringArgumentType.getString(ctx, "functionName");
									return executeFunction(functionName, ctx);
								})
						)
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("list")
						.executes(ctx -> {
							ServerCommandSource source = ctx.getSource();
							source.sendFeedback(() -> Text.literal("Aliases:"), false);
							for (Map.Entry<String, String> entry : aliases.entrySet()) {
								source.sendFeedback(() -> Text.literal("  /" + entry.getKey() + " -> " + entry.getValue()), false);
							}
							source.sendFeedback(() -> Text.literal("Functions:"), false);
							try {
								Files.list(FUNCTIONS_PATH)
									.filter(path -> path.toString().endsWith(".mcfunction"))
									.forEach(path -> {
										String name = path.getFileName().toString().replace(".mcfunction", "");
										source.sendFeedback(() -> Text.literal("  " + name), false);
									});
							} catch (Exception e) {
								source.sendFeedback(() -> Text.literal("Error listing functions: " + e.getMessage()), false);
							}
							return 1;
						})
				)
		);
	}

	// Register /addcommand command
	private void registerAddCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("addcommand")
				.requires(source -> PermissionManager.canUseAddCommand(source))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("add")
					.then(RequiredArgumentBuilder.<ServerCommandSource>argument("alias", StringArgumentType.word())
						.then(RequiredArgumentBuilder.<ServerCommandSource>argument("command", StringArgumentType.greedyString())
							.executes(ctx -> {
								if (!PermissionManager.canManageAliases(ctx.getSource())) {
									ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to add aliases."), false);
									return 0;
								}
								String alias = StringArgumentType.getString(ctx, "alias");
								String command = StringArgumentType.getString(ctx, "command");
								aliases.put(alias, command);
								saveAliases();
								registerAlias(dispatcher, alias, command);
								ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Alias /" + alias + " -> " + command + " added."), false);
								return 1;
							})
						)
					)
				)
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("del")
					.then(RequiredArgumentBuilder.<ServerCommandSource>argument("alias", StringArgumentType.word())
						.executes(ctx -> {
							if (!PermissionManager.canManageAliases(ctx.getSource())) {
								ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to delete aliases."), false);
								return 0;
							}
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
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
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
		);
	}

	// Register /setcmdvariable <variable> <value> command
	private void registerSetCmdVariable(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("setcmdvariable")
				.then(RequiredArgumentBuilder.<ServerCommandSource>argument("variable", StringArgumentType.word())
					.then(RequiredArgumentBuilder.<ServerCommandSource>argument("value", StringArgumentType.greedyString())
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
			LiteralArgumentBuilder.<ServerCommandSource>literal(alias)
				.executes(ctx -> {
					ServerCommandSource source = ctx.getSource();
					
					// Check if player has permission to use this alias
					if (!PermissionManager.canUseAlias(source, alias)) {
						source.sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to use this alias."), false);
						return 0;
					}
					
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
						ServerCommandSource source = ctx.getSource();
						
						// Check if player has permission to use this alias
						if (!PermissionManager.canUseAlias(source, alias)) {
							source.sendFeedback(() -> net.minecraft.text.Text.literal("You don't have permission to use this alias."), false);
							return 0;
						}
						
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
			for (int idx = 0; idx < lines.size(); idx++) {
				String rawLine = lines.get(idx);
				String line = rawLine.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				try {
					String command = substituteVariables(line, ctx);
					ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, ctx.getSource());
					cmdDispatcher.execute(parsed);
					executed++;
				} catch (Exception ex) {
					int lineNum = idx + 1;
					String errorMsg = ex.getMessage();
					LOGGER.error("Failed to execute function '{}' at line {}: {}", functionName, lineNum, line, ex);
					ctx.getSource().sendFeedback(() -> Text.literal("Error executing function '" + functionName + "' line " + lineNum + ": " + errorMsg), false);
					return executed;
				}
			}
			return executed;
		} catch (Exception e) {
			LOGGER.error("Failed to execute function '" + functionName + "'", e);
			ctx.getSource().sendFeedback(() -> Text.literal("Failed to execute function '" + functionName + "': " + e.getMessage()), false);
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
					source.sendFeedback(() -> net.minecraft.text.Text.literal("§7Use: §f/cmd del <alias>§7 to delete"), false);
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

	private int cmd(ServerCommandSource source, String alias, String command, CommandDispatcher<ServerCommandSource> dispatcher) {
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
		// Ensure functions directory exists
		try {
			if (!Files.exists(FUNCTIONS_PATH)) {
				Files.createDirectories(FUNCTIONS_PATH);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to verify functions directory", e);
		}
		source.sendFeedback(() -> net.minecraft.text.Text.literal("§6Reloaded aliases, functions, and syntax definitions."), false);
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
								return cmd(ctx.getSource(), alias, command, dispatcher);
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
						String tellrawCmd = "tellraw " + source.getName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\"}}";
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
				.then(net.minecraft.server.command.CommandManager.literal("downloadfunction")
						.then(net.minecraft.server.command.CommandManager.argument("function", StringArgumentType.word())
							.suggests((ctx, builder) -> {
								try {
									HttpClient client = HttpClient.newHttpClient();
									HttpRequest request = HttpRequest.newBuilder()
										.uri(java.net.URI.create("https://api.github.com/repos/Diamondstar-Mods/Minecraft-Command-Maker/contents/docs/cdn/functions"))
										.build();

									CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> future = new CompletableFuture<>();
									client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((resp, ex) -> {
										try {
											if (ex == null && resp.statusCode() == 200) {
												JsonElement je = JsonParser.parseString(resp.body());
												if (je.isJsonArray()) {
													for (JsonElement e : je.getAsJsonArray()) {
														try {
															JsonObject obj = e.getAsJsonObject();
															String name = obj.get("name").getAsString();
															if (name.endsWith(".mcfunction")) {
																String bare = name.substring(0, name.length() - ".mcfunction".length());
																builder.suggest(bare);
															}
														} catch (Exception ignore) {}
													}
												}
											}
										} catch (Exception ignore) {}
										future.complete(builder.build());
									});
									return future;
								} catch (Exception e) {
									return builder.buildFuture();
								}
							})
							.executes(ctx -> {
								String function = StringArgumentType.getString(ctx, "function");
								new Thread(() -> {
									try {
										HttpClient client = HttpClient.newHttpClient();
										HttpRequest request = HttpRequest.newBuilder()
											.uri(java.net.URI.create("https://commandmakerwiki.lucasgeitgey.com/cdn/functions/" + function + ".mcfunction"))
											.build();
										HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
										if (response.statusCode() == 200) {
											Path filePath = FUNCTIONS_PATH.resolve(function + ".mcfunction");
											Files.write(filePath, response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
											ctx.getSource().sendFeedback(() -> Text.literal("Downloaded function '" + function + "' successfully."), false);
										} else {
											ctx.getSource().sendFeedback(() -> Text.literal("Failed to download function '" + function + "': HTTP " + response.statusCode()), false);
										}
									} catch (Exception e) {
										ctx.getSource().sendFeedback(() -> Text.literal("Failed to download function '" + function + "': " + e.getMessage()), false);
									}
								}).start();
								return 1;
							})
						)
				)
				.then(net.minecraft.server.command.CommandManager.literal("listdownloadablefunctions")
					.executes(ctx -> {
						ServerCommandSource source = ctx.getSource();
						source.sendFeedback(() -> Text.literal("§6§lFetching available functions..."), false);
						new Thread(() -> {
							try {
								HttpClient client = HttpClient.newHttpClient();
								HttpRequest request = HttpRequest.newBuilder()
									.uri(java.net.URI.create("https://api.github.com/repos/Diamondstar-Mods/Minecraft-Command-Maker/contents/docs/cdn/functions"))
									.build();
								HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
								if (response.statusCode() == 200) {
									JsonElement je = JsonParser.parseString(response.body());
									if (je.isJsonArray()) {
										java.util.List<String> names = new java.util.ArrayList<>();
										for (JsonElement e : je.getAsJsonArray()) {
											try {
												JsonObject obj = e.getAsJsonObject();
												String name = obj.get("name").getAsString();
												if (name.endsWith(".mcfunction")) {
													names.add(name.substring(0, name.length() - ".mcfunction".length()));
												}
											} catch (Exception ignore) {}
										}
										if (names.isEmpty()) {
											source.sendFeedback(() -> Text.literal("§cNo downloadable functions found."), false);
										} else {
											source.sendFeedback(() -> Text.literal("§6§l📦 Downloadable Functions (" + names.size() + ") 📦"), false);
											source.sendFeedback(() -> Text.literal("§7Use §e/commandmaker downloadfunction <name> §7to download."), false);
											source.sendFeedback(() -> Text.literal("§7─────────────────────────────"), false);
											for (String name : names) {
												source.sendFeedback(() -> Text.literal("§a • §f" + name), false);
											}
											source.sendFeedback(() -> Text.literal("§7─────────────────────────────"), false);
										}
									} else {
										source.sendFeedback(() -> Text.literal("§cUnexpected response format from server."), false);
									}
								} else {
									source.sendFeedback(() -> Text.literal("§cFailed to fetch function list: HTTP " + response.statusCode()), false);
								}
							} catch (Exception e) {
								source.sendFeedback(() -> Text.literal("§cFailed to fetch function list: " + e.getMessage()), false);
							}
						}).start();
						return 1;
					})
				)
		);
	}

	// Register /cmmakerperm command for managing permissions
	private void registerPermissionCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			net.minecraft.server.command.CommandManager.literal("cmmakerperm")
				.requires(source -> source.hasPermissionLevel(4)) // Ops only
				.then(
					net.minecraft.server.command.CommandManager.literal("grant")
						.then(
							net.minecraft.server.command.CommandManager.argument("player", StringArgumentType.word())
								.then(
									net.minecraft.server.command.CommandManager.argument("permission", StringArgumentType.greedyString())
										.executes(ctx -> {
											String playerName = StringArgumentType.getString(ctx, "player");
											String permission = StringArgumentType.getString(ctx, "permission");
											ctx.getSource().getServer().getPlayerManager().getPlayer(playerName);
											// This is a simplified version - in production you'd resolve the player UUID properly
											ctx.getSource().sendFeedback(() -> Text.literal("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
											return 1;
										})
								)
						)
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("revoke")
						.then(
							net.minecraft.server.command.CommandManager.argument("player", StringArgumentType.word())
								.then(
									net.minecraft.server.command.CommandManager.argument("permission", StringArgumentType.greedyString())
										.executes(ctx -> {
											ctx.getSource().sendFeedback(() -> Text.literal("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
											return 1;
										})
								)
						)
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("check")
						.then(
							net.minecraft.server.command.CommandManager.argument("player", StringArgumentType.word())
								.then(
									net.minecraft.server.command.CommandManager.argument("permission", StringArgumentType.greedyString())
										.executes(ctx -> {
											ctx.getSource().sendFeedback(() -> Text.literal("§cNote: For full permission checking, use LuckPerms"), false);
											return 1;
										})
								)
						)
				)
				.then(
					net.minecraft.server.command.CommandManager.literal("reload")
						.executes(ctx -> {
							PermissionManager.initialize();
							ctx.getSource().sendFeedback(() -> Text.literal("§aPermission config reloaded!"), false);
							return 1;
						})
				)
		);
	}
}