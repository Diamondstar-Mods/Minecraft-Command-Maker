package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Path CONFIG_PATH = Paths.get("config", "CommandMaker", "aliases.json");
	private static final Map<String, String> aliases = new HashMap<>();
	// Store per-player custom variables: player UUID -> (varName -> value)
	private static final Map<UUID, Map<String, String>> playerVariables = new HashMap<>();
	@Override
	public void onInitialize() {
		ensureConfigExists();
		loadAliases();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
			registerSetCmdVariable(dispatcher);
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
			for (String line : lines) {
				if (line.trim().startsWith("#")) continue;
				jsonBuilder.append(line).append("\n");
			}
			String json = jsonBuilder.toString().trim();
			if (json.isEmpty() || json.equals("{}")) return;
			JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				aliases.put(entry.getKey(), entry.getValue().getAsString());
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load aliases config", e);
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
	}}