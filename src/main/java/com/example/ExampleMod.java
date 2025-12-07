package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "cmdmaker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
	private static final Map<String, String> aliases = new HashMap<>();
	// Store per-player custom variables: player UUID -> (varName -> value)
	private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();
	private static boolean modLoadedMessageSent = false;

	@Override
	public void onInitialize() {
		ensureConfigExists();
		loadAliases();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
			registerSetCmdVariable(dispatcher);
			registerDeleteAliasMenu(dispatcher);
		});
		
		// Register server lifecycle event for singleplayer and server startup
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			modLoadedMessageSent = false;
		});
		
		// Register player join event to send message
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!modLoadedMessageSent) {
				// Send message to the joining player
				handler.player.sendMessage(
					Text.literal("§anek's Command Maker successfully loaded!"),
					false
				);
				modLoadedMessageSent = true;
			}
		});
		
		LOGGER.info("Alias mod initialized!");
	}

	// Ensure config folder and aliases.json exist, create with example if not
	private void ensureConfigExists() {
		try {
			Path folder = CONFIG_PATH.getParent();
			if (!Files.exists(folder)) {
				Files.createDirectories(folder);
			}
			if (!Files.exists(CONFIG_PATH)) {
				List<String> lines = new ArrayList<>();
				lines.add("# CommandMaker Aliases Config");
				lines.add("# Each entry is an alias and its command target.");
				lines.add("# You can use variables like ${player}, ${x}, ${y}, ${z} in the command.");
				lines.add("# Example:");
				lines.add("#   teleport=tp ${player} 0 100 0");
				lines.add("#   greet=say Hello, ${player}!");
				lines.add("{");
				lines.add("}");
				Files.write(CONFIG_PATH, lines, StandardOpenOption.CREATE_NEW);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create config folder or file", e);
		}
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
					String command = substituteVariables(target, ctx);
					CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
					ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
					return cmdDispatcher.execute(parsed);
				})
				.then(net.minecraft.server.command.CommandManager.argument("args", StringArgumentType.greedyString())
					.executes(ctx -> {
						String args = StringArgumentType.getString(ctx, "args");
						String full = target + " " + args;
						String command = substituteVariables(full, ctx);
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
		} catch (Exception e) {
			LOGGER.error("Failed to load aliases config (server will continue without aliases)", e);
		}
	}

	private void saveAliases() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			List<String> lines = new ArrayList<>();
			lines.add("# CommandMaker Aliases Config");
			lines.add("# Each entry is an alias and its command target.");
			lines.add("# You can use variables like ${player}, ${x}, ${y}, ${z} in the command.");
			lines.add("# Example:");
			lines.add("#   teleport=tp ${player} 0 100 0");
			lines.add("#   greet=say Hello, ${player}!");
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
	}}