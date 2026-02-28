package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import com.mojang.brigadier.ParseResults;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CMDMakerClient implements ClientModInitializer {
	public static final String MOD_ID = "cmdmakerclient";
	public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

	private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
	private static final Path FUNCTIONS_PATH = Paths.get("config", "CommandMaker", "Functions");
	private static final Path SETTINGS_PATH = Paths.get("config", "CommandMaker", "settings", "opSettings.yml");
	private static final Map<String, String> aliases = new HashMap<>();
	// Store per-player custom variables: player UUID -> (varName -> value)
	private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();
	// Operator settings
	private static boolean onlyOperatorsCanRunAliases = false;
	private static final Set<String> commandsRunByNonAdminsAllowOverride = new HashSet<>();
	private static final Set<String> commandsRunInConsoleMode = new HashSet<>();

	@Override
	public void onInitializeClient() {
		ensureConfigExists();
		loadAliases();
		loadSettings();
		SyntaxManager.loadSyntaxDefinitions();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			registercmd(dispatcher);
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
			registerSetCmdVariable(dispatcher);
			registerDeleteAliasMenu(dispatcher);
			registerSyntaxCommand(dispatcher);
			registerCmdCommand(dispatcher);
		});

		LOGGER.info("CMDMaker Client mod initialized!");
	}

	// Ensure config folder and config files exist, create with defaults if not
	private void ensureConfigExists() {
		try {
			LOGGER.info("Ensuring config files exist...");
			Path folder = CONFIG_PATH.getParent();
			if (!Files.exists(folder)) {
				Files.createDirectories(folder);
				LOGGER.info("Created config directory: {}", folder);
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
				LOGGER.info("Created aliases config file: {}", CONFIG_PATH);
			}

			if (!Files.exists(SETTINGS_PATH)) {
				LOGGER.info("Settings file doesn't exist, creating: {}", SETTINGS_PATH);
				Path settingsFolder = SETTINGS_PATH.getParent();
				if (!Files.exists(settingsFolder)) {
					Files.createDirectories(settingsFolder);
					LOGGER.info("Created settings directory: {}", settingsFolder);
				}
				List<String> lines = new ArrayList<>();
				lines.add("# CommandMaker Operator Settings");
				lines.add("# This file controls operator permissions for alias execution");
				lines.add("#");
				lines.add("# Set to true to restrict alias execution to operators only (permission level 2+)");
				lines.add("# Set to false to allow all players to run aliases (default)");
				lines.add("onlyOperatorsCanRunAliases= false");
				lines.add("");
				lines.add("# Commands that non-admin players can run even when onlyOperatorsCanRunAliases=true");
				lines.add("# These aliases will be allowed for regular players as exceptions");
				lines.add("commandsRunByNonAdminsAllowOverride:");
				lines.add("#\thelp      # Allow non-admins to use help commands");
				lines.add("#\ttp        # Allow non-admins to use teleport commands");
				lines.add("#\thome      # Allow non-admins to use home commands");
				lines.add("#\tspawn     # Allow non-admins to use spawn commands");
				lines.add("");
				lines.add("# Commands that should run in console/server mode instead of player mode");
				lines.add("# These aliases will execute as the server rather than the player who ran them");
				lines.add("commandsRunInConsoleMode:");
				lines.add("#\tban       # Ban commands should run as console");
				lines.add("#\tkick      # Kick commands should run as console");
				lines.add("#\top        # OP commands should run as console");
				lines.add("#\tdeop      # DEOP commands should run as console");
				Files.write(SETTINGS_PATH, lines, StandardOpenOption.CREATE_NEW);
				LOGGER.info("Created settings file: {}", SETTINGS_PATH);
			} else {
				LOGGER.info("Settings file already exists: {}", SETTINGS_PATH);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create config folder or files", e);
		}
	}

	// Load operator settings
	private void loadSettings() {
		try {
			if (Files.exists(SETTINGS_PATH)) {
				List<String> lines = Files.readAllLines(SETTINGS_PATH);
				String currentSection = null;
				
				for (String line : lines) {
					String trimmed = line.trim();
					
					// Skip empty lines and comments
					if (trimmed.isEmpty() || trimmed.startsWith("#")) {
						continue;
					}
					
					// Check for main settings
					if (trimmed.startsWith("onlyOperatorsCanRunAliases=")) {
						String value = trimmed.substring("onlyOperatorsCanRunAliases=".length()).trim();
						onlyOperatorsCanRunAliases = "true".equalsIgnoreCase(value);
						continue;
					}
					
					// Check for section headers
					if (trimmed.equals("commandsRunByNonAdminsAllowOverride:")) {
						currentSection = "allowOverride";
						continue;
					}
					if (trimmed.equals("commandsRunInConsoleMode:")) {
						currentSection = "consoleMode";
						continue;
					}
					
					// Process indented lines based on current section
					if (line.startsWith("\t") && currentSection != null) {
						String command = trimmed;
						if ("allowOverride".equals(currentSection)) {
							commandsRunByNonAdminsAllowOverride.add(command);
						} else if ("consoleMode".equals(currentSection)) {
							commandsRunInConsoleMode.add(command);
						}
					}
				}
				
				LOGGER.info("Loaded operator settings: onlyOperatorsCanRunAliases={}, allowOverrideCommands={}, consoleModeCommands={}", 
					onlyOperatorsCanRunAliases, commandsRunByNonAdminsAllowOverride.size(), commandsRunInConsoleMode.size());
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load operator settings", e);
		}
	}

	// Load aliases from config file
	private void loadAliases() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				String content = new String(Files.readAllBytes(CONFIG_PATH));
				JsonElement element = JsonParser.parseString(content);
				if (element.isJsonObject()) {
					JsonObject jsonObject = element.getAsJsonObject();
					for (String key : jsonObject.keySet()) {
						JsonElement valueElement = jsonObject.get(key);
						if (valueElement.isJsonPrimitive()) {
							aliases.put(key, valueElement.getAsString());
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load aliases", e);
		}
	}

	// Save aliases to config file
	private void saveAliases() {
		try {
			JsonObject jsonObject = new JsonObject();
			for (Map.Entry<String, String> entry : aliases.entrySet()) {
				jsonObject.addProperty(entry.getKey(), entry.getValue());
			}
			String json = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
			Files.write(CONFIG_PATH, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception e) {
			LOGGER.error("Failed to save aliases", e);
		}
	}

	// Register the /cmd command and its subcommands
	private void registercmd(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("cmd")
				.then(
					ClientCommandManager.literal("reload")
						.executes(ctx -> {
							loadAliases();
							// Remove all old aliases from dispatcher before re-registering
							for (String alias : new HashSet<>(aliases.keySet())) {
								dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
							}
							registerAliases(dispatcher);
							ctx.getSource().sendFeedback(Text.literal("Aliases reloaded."));
							return 1;
						})
				)
				.then(
					ClientCommandManager.literal("add")
						.then(
							ClientCommandManager.argument("alias", StringArgumentType.word())
								.then(
									ClientCommandManager.argument("target", StringArgumentType.greedyString())
										.executes(ctx -> {
											// Check if only operators can manage aliases
											if (onlyOperatorsCanRunAliases) {
												ctx.getSource().sendError(Text.literal("Only operators can add aliases. This setting is controlled server-side."));
												return 0;
											}
											
											String alias = StringArgumentType.getString(ctx, "alias");
											String target = StringArgumentType.getString(ctx, "target");
											aliases.put(alias, target);
											saveAliases();
											registerAlias(dispatcher, alias, target);
											ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " -> " + target + " added."));
											return 1;
										})
								)
						)
				)
				.then(
					ClientCommandManager.literal("del")
						.then(
							ClientCommandManager.argument("alias", StringArgumentType.word())
								.executes(ctx -> {
									// Check if only operators can manage aliases
									if (onlyOperatorsCanRunAliases) {
										ctx.getSource().sendError(Text.literal("Only operators can delete aliases. This setting is controlled server-side."));
										return 0;
									}
									
									String alias = StringArgumentType.getString(ctx, "alias");
									if (aliases.remove(alias) != null) {
										saveAliases();
										dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
										ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " removed."));
									} else {
										ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " not found."));
									}
									return 1;
								})
						)
				)
				.then(
					ClientCommandManager.literal("function")
						.then(
							ClientCommandManager.argument("functionName", StringArgumentType.word())
								.executes(ctx -> {
									String functionName = StringArgumentType.getString(ctx, "functionName");
									return executeFunction(functionName, ctx);
								})
						)
				)
				.then(
					ClientCommandManager.literal("list")
						.executes(ctx -> {
							FabricClientCommandSource source = ctx.getSource();
							source.sendFeedback(Text.literal("Aliases:"));
							for (Map.Entry<String, String> entry : aliases.entrySet()) {
								source.sendFeedback(Text.literal("  /" + entry.getKey() + " -> " + entry.getValue()));
							}
							source.sendFeedback(Text.literal("Functions:"));
							try {
								Files.list(FUNCTIONS_PATH)
									.filter(path -> path.toString().endsWith(".mcfunction"))
									.forEach(path -> {
										String name = path.getFileName().toString().replace(".mcfunction", "");
										source.sendFeedback(Text.literal("  " + name));
									});
							} catch (Exception e) {
								source.sendFeedback(Text.literal("Error listing functions: " + e.getMessage()));
							}
							return 1;
						})
				)
				.then(ClientCommandManager.literal("downloadfunction")
					.then(ClientCommandManager.argument("function", StringArgumentType.word())
						.executes(ctx -> {
							String function = StringArgumentType.getString(ctx, "function");
							new Thread(() -> {
								try {
									HttpClient client = HttpClient.newHttpClient();
									HttpRequest request = HttpRequest.newBuilder()
										.uri(java.net.URI.create("https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/" + function + ".mcfunction"))
										.build();
									HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
									if (response.statusCode() == 200) {
										Path filePath = FUNCTIONS_PATH.resolve(function + ".mcfunction");
										Files.write(filePath, response.body().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
										ctx.getSource().sendFeedback(Text.literal("Downloaded function '" + function + "' successfully."));
									} else {
										ctx.getSource().sendFeedback(Text.literal("Failed to download function '" + function + "': HTTP " + response.statusCode()));
									}
								} catch (Exception e) {
									ctx.getSource().sendFeedback(Text.literal("Failed to download function '" + function + "': " + e.getMessage()));
								}
							}).start();
							return 1;
						})
					)
				)
		);
	}

	// Register all aliases as commands
	private void registerAliases(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			registerAlias(dispatcher, entry.getKey(), entry.getValue());
		}
	}

	// Register a single alias
	private void registerAlias(CommandDispatcher<FabricClientCommandSource> dispatcher, String alias, String target) {
		dispatcher.register(
			ClientCommandManager.literal(alias)
				.executes(ctx -> executeAlias(target, ctx))
		);
	}

	// Execute an alias command
	private int executeAlias(String target, CommandContext<FabricClientCommandSource> ctx) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return 0;

			// Handle function calls
			if (target.startsWith("function:")) {
				String functionName = target.substring(9);
				return executeFunction(functionName, ctx);
			}

			// Replace variables
			String command = replaceVariables(target, ctx);

			// Ensure the command starts with /
			if (!command.startsWith("/")) {
				command = "/" + command;
			}

			// Execute the command on the server
			String finalCommand = command;
			client.execute(() -> {
				if (client.player != null) {
					client.player.networkHandler.sendChatMessage(finalCommand);
				}
			});
			return 1;
		} catch (Exception e) {
			LOGGER.error("Failed to execute alias: {}", target, e);
			ctx.getSource().sendError(Text.literal("Failed to execute alias: " + e.getMessage()));
			return 0;
		}
	}

	// Execute a function from the Functions folder
	private int executeFunction(String functionName, CommandContext<FabricClientCommandSource> ctx) {
		try {
			Path functionFile = FUNCTIONS_PATH.resolve(functionName + ".mcfunction");
			if (!Files.exists(functionFile)) {
				ctx.getSource().sendError(Text.literal("Function '" + functionName + "' not found."));
				return 0;
			}

			List<String> lines = Files.readAllLines(functionFile);
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return 0;

			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;

				// Replace variables
				line = replaceVariables(line, ctx);

				// Send each command to the server
				client.player.networkHandler.sendChatMessage("/" + line);
			}
			return 1;
		} catch (Exception e) {
			LOGGER.error("Failed to execute function: {}", functionName, e);
			ctx.getSource().sendError(Text.literal("Failed to execute function: " + e.getMessage()));
			return 0;
		}
	}

	// Replace variables in command string
	private String replaceVariables(String command, CommandContext<FabricClientCommandSource> ctx) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return command;

		String result = command;
		UUID playerUUID = client.player.getUuid();

		// Replace player variables
		result = result.replace("${player}", client.player.getName().getString());
		result = result.replace("${x}", String.valueOf((int) client.player.getX()));
		result = result.replace("${y}", String.valueOf((int) client.player.getY()));
		result = result.replace("${z}", String.valueOf((int) client.player.getZ()));

		// Replace custom player variables
		Map<String, String> playerVars = playerVariables.get(playerUUID);
		if (playerVars != null) {
			for (Map.Entry<String, String> entry : playerVars.entrySet()) {
				result = result.replace("${" + entry.getKey() + "}", entry.getValue());
			}
		}

		return result;
	}

	// Register /setcmdvar command
	private void registerSetCmdVariable(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("setcmdvar")
				.then(
					ClientCommandManager.argument("name", StringArgumentType.word())
						.then(
							ClientCommandManager.argument("value", StringArgumentType.greedyString())
								.executes(ctx -> {
									MinecraftClient client = MinecraftClient.getInstance();
									if (client.player == null) return 0;

									String name = StringArgumentType.getString(ctx, "name");
									String value = StringArgumentType.getString(ctx, "value");
									UUID playerUUID = client.player.getUuid();

									playerVariables.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(name, value);
									ctx.getSource().sendFeedback(Text.literal("Variable $" + name + " set to: " + value));
									return 1;
								})
						)
				)
		);
	}

	// Register /delaliasmenu command
	private void registerDeleteAliasMenu(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("delaliasmenu")
				.executes(ctx -> {
					// This would open a GUI for deleting aliases, but for client-side we'll just list them
					StringBuilder sb = new StringBuilder("Available aliases to delete:\n");
					for (String alias : aliases.keySet()) {
						sb.append("- ").append(alias).append(" -> ").append(aliases.get(alias)).append("\n");
					}
					ctx.getSource().sendFeedback(Text.literal(sb.toString()));
					return 1;
				})
		);
	}

	// Register /syntax command
	private void registerSyntaxCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("syntax")
				.then(
					ClientCommandManager.literal("list")
						.executes(ctx -> {
							StringBuilder sb = new StringBuilder("Available syntaxes:\n");
							for (Map.Entry<String, CommandSyntax> entry : SyntaxManager.getAllSyntaxes().entrySet()) {
								sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue().getPattern()).append("\n");
							}
							ctx.getSource().sendFeedback(Text.literal(sb.toString()));
							return 1;
						})
				)
				.then(
					ClientCommandManager.literal("help")
						.then(
							ClientCommandManager.argument("syntaxName", StringArgumentType.word())
								.executes(ctx -> {
									String syntaxName = StringArgumentType.getString(ctx, "syntaxName");
									CommandSyntax syntax = SyntaxManager.getSyntax(syntaxName);
									if (syntax != null) {
										ctx.getSource().sendFeedback(Text.literal(syntaxName + ": " + syntax.getPattern() + "\n" + syntax.getDescription()));
									} else {
										ctx.getSource().sendFeedback(Text.literal("Syntax '" + syntaxName + "' not found."));
									}
									return 1;
								})
						)
				)
		);
	}

	// Register /cmdcommand command (for compatibility)
	private void registerCmdCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("cmdcommand")
				.then(
					ClientCommandManager.argument("command", StringArgumentType.greedyString())
						.executes(ctx -> {
							String command = StringArgumentType.getString(ctx, "command");
							MinecraftClient client = MinecraftClient.getInstance();
							if (client.player != null) {
								client.player.networkHandler.sendChatMessage("/" + command);
							}
							return 1;
						})
				)
		);
	}

	// Register /addcommand command
	private void registerAddCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommandManager.literal("addcommand")
				.then(ClientCommandManager.literal("add")
					.then(ClientCommandManager.argument("alias", StringArgumentType.word())
						.then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
							.executes(ctx -> {
								String alias = StringArgumentType.getString(ctx, "alias");
								String command = StringArgumentType.getString(ctx, "command");
								aliases.put(alias, command);
								saveAliases();
								registerAlias(dispatcher, alias, command);
								ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " -> " + command + " added."));
								return 1;
							})
						)
					)
				)
				.then(ClientCommandManager.literal("del")
					.then(ClientCommandManager.argument("alias", StringArgumentType.word())
						.executes(ctx -> {
							String alias = StringArgumentType.getString(ctx, "alias");
							if (aliases.remove(alias) != null) {
								saveAliases();
								dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
								ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " removed."));
							} else {
								ctx.getSource().sendFeedback(Text.literal("Alias /" + alias + " not found."));
							}
							return 1;
						})
					)
				)
				.then(ClientCommandManager.literal("reload")
					.executes(ctx -> {
						loadAliases();
						// Remove all old aliases from dispatcher before re-registering
						for (String alias : new HashSet<>(aliases.keySet())) {
							dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
						}
						registerAliases(dispatcher);
						ctx.getSource().sendFeedback(Text.literal("Aliases reloaded."));
						return 1;
					})
				)
		);
	}
}