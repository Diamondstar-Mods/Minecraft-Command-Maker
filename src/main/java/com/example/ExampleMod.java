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

	private static final Path CONFIG_PATH = Paths.get("config", MOD_ID + "_aliases.json");
	private static final Map<String, String> aliases = new HashMap<>();

	@Override
	public void onInitialize() {
		loadAliases();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAddCommand(dispatcher);
			registerAliases(dispatcher);
		});
		LOGGER.info("Alias mod initialized!");
	}

	private void registerAddCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			LiteralArgumentBuilder.<ServerCommandSource>literal("addcommand")
				.then(net.minecraft.server.command.CommandManager.literal("reload")
					.executes(ctx -> {
						loadAliases();
						registerAliases(dispatcher);
						ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Aliases reloaded."), false);
						return 1;
					})
				)
				.then(net.minecraft.server.command.CommandManager.literal("add")
					.then(net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
						.then(net.minecraft.server.command.CommandManager.argument("target", StringArgumentType.greedyString())
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
				.then(net.minecraft.server.command.CommandManager.literal("del")
					.then(net.minecraft.server.command.CommandManager.argument("alias", StringArgumentType.word())
						.executes(ctx -> {
							String alias = StringArgumentType.getString(ctx, "alias");
							if (aliases.remove(alias) != null) {
								saveAliases();
								// Remove the command node from dispatcher
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
					CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
					ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(target, source);
					return cmdDispatcher.execute(parsed);
				})
				.then(net.minecraft.server.command.CommandManager.argument("args", StringArgumentType.greedyString())
					.executes(ctx -> {
						String args = StringArgumentType.getString(ctx, "args");
						String full = target + " " + args;
						ServerCommandSource source = ctx.getSource();
						CommandDispatcher<ServerCommandSource> cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
						ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(full, source);
						return cmdDispatcher.execute(parsed);
					})
				)
		);
	}

	private void loadAliases() {
		aliases.clear();
		try {
			if (Files.exists(CONFIG_PATH)) {
				String json = Files.readString(CONFIG_PATH);
				JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					aliases.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load aliases config", e);
		}
	}

	private void saveAliases() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			JsonObject obj = new JsonObject();
			for (Map.Entry<String, String> entry : aliases.entrySet()) {
				obj.addProperty(entry.getKey(), entry.getValue());
			}
			Files.writeString(CONFIG_PATH, obj.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception e) {
			LOGGER.error("Failed to save aliases config", e);
		}
	}
}