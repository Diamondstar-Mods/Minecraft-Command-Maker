package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.dimdev.riftloader.listener.InitializationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * RiftLoader entry point for Command Maker.
 * Implements InitializationListener — onInitialization() is called before MC starts.
 * Command registration happens via mixin on the first server tick.
 */
public class CommandMakerRift implements InitializationListener {
    public static final String MOD_ID = "cmdmaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean commandsRegistered = false;

    @Override
    public void onInitialization() {
        AliasManager.ensureConfigExists();
        AliasManager.loadAliases();
        SyntaxManager.loadSyntaxDefinitions();

        LOGGER.info("Command Maker (RiftLoader) pre-initialized!");
    }

    /**
     * Called from MixinMinecraftServer on first tick after startup.
     */
    @SuppressWarnings("unchecked")
    public static void registerCommands(MinecraftServer server) {
        if (commandsRegistered) return;
        commandsRegistered = true;

        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();

        registerCmdCommand(dispatcher);
        registerAddCommand(dispatcher);
        registerSetCmdVariable(dispatcher);
        registerSyntaxCommand(dispatcher);
        registerDeleteAliasCommand(dispatcher);
        registerAliases(dispatcher);

        LOGGER.info("Command Maker (RiftLoader) commands registered!");
    }

    // ---- /cmd ----

    private static void registerCmdCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("cmd")
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cAlias '§f" + alias + "§c' already exists. Use /cmd del " + alias + " first."), false);
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(() -> Text.literal("§6Added alias: §f/" + alias + " §7-> §f" + command), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("del")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(() -> Text.literal("§6[" + alias + "] was deleted"), false);
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("§cAlias '§f" + alias + "§c' not found."), false);
                            }
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        SyntaxManager.loadSyntaxDefinitions();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendFeedback(() -> Text.literal("§6Reloaded aliases, functions, and syntax definitions."), false);
                        return 1;
                    })
                )
                .then(CommandManager.literal("function")
                    .then(CommandManager.argument("functionName", StringArgumentType.word())
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            return FunctionManager.executeFunction(functionName, ctx);
                        })
                    )
                )
                .then(CommandManager.literal("list")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        source.sendFeedback(() -> Text.literal("Aliases:"), false);
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendFeedback(() -> Text.literal("  /" + entry.getKey() + " -> " + entry.getValue()), false);
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("help")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        source.sendFeedback(() -> Text.literal("§6§l⚡ Command Maker Help ⚡"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd add <alias> <command> §7— Create a new command alias"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd del <alias> §7— Delete an alias"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd reload §7— Reload aliases and syntax definitions"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd list §7— List all aliases and functions"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd function <name> §7— Run a .mcfunction file"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd downloadfunction <name> §7— Download from online library"), false);
                        source.sendFeedback(() -> Text.literal("§e/cmd help §7— Show this help message"), false);
                        return 1;
                    })
                )
                .then(CommandManager.literal("downloadfunction")
                    .then(CommandManager.argument("function", StringArgumentType.word())
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            Map<String, ManifestEntry> manifest = FunctionManager.fetchFunctionManifest();
                            for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                                builder.suggest(entry.getKey());
                            }
                            return builder.build();
                        }))
                        .executes(ctx -> {
                            String function = StringArgumentType.getString(ctx, "function");
                            FunctionManager.downloadFunction(function, ctx.getSource());
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /addcommand ----

    private static void registerAddCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("addcommand")
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(() -> Text.literal("Alias /" + alias + " -> " + command + " added."), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("del")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(() -> Text.literal("Alias /" + alias + " removed."), false);
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("Alias /" + alias + " not found."), false);
                            }
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendFeedback(() -> Text.literal("Aliases reloaded."), false);
                        return 1;
                    })
                )
        );
    }

    // ---- /setcmdvariable ----

    private static void registerSetCmdVariable(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("setcmdvariable")
                .then(CommandManager.argument("variable", StringArgumentType.word())
                    .then(CommandManager.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String var = StringArgumentType.getString(ctx, "variable");
                            String value = StringArgumentType.getString(ctx, "value");
                            return VariableManager.setVariable(ctx.getSource(), var, value);
                        })
                    )
                )
        );
    }

    // ---- /syntax ----

    private static void registerSyntaxCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("syntax")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                    if (syntaxes.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("§cNo custom syntaxes defined."), false);
                        return 0;
                    }
                    source.sendFeedback(() -> Text.literal("§6Available Custom Syntaxes:"), false);
                    for (String name : syntaxes.keySet()) {
                        CommandSyntax syntax = syntaxes.get(name);
                        String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                        source.sendFeedback(() -> Text.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
                    }
                    return 1;
                })
        );
    }

    // ---- /deletealias ----

    private static void registerDeleteAliasCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("deletealias")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("§cNo aliases to delete."), false);
                        return 0;
                    }
                    source.sendFeedback(() -> Text.literal("§6Delete Aliases Menu:"), false);
                    int index = 1;
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        source.sendFeedback(() -> Text.literal("  §f[" + index + "] §6/" + alias + " §7-> §f" + cmd), false);
                        index++;
                    }
                    source.sendFeedback(() -> Text.literal("§7Use: §f/cmd del <alias>§7 to delete"), false);
                    return 1;
                })
                .then(CommandManager.argument("alias", StringArgumentType.word())
                    .executes(ctx -> {
                        String alias = StringArgumentType.getString(ctx, "alias");
                        if (AliasManager.removeAlias(alias)) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            ctx.getSource().sendFeedback(() -> Text.literal("§6[" + alias + "] was deleted"), false);
                        } else {
                            ctx.getSource().sendFeedback(() -> Text.literal("§cAlias '§f" + alias + "§c' not found."), false);
                        }
                        return 1;
                    })
                )
        );
    }

    // ---- Alias registration ----

    private static void registerAliases(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private static void registerAlias(CommandDispatcher<ServerCommandSource> dispatcher, String alias, String target) {
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
        dispatcher.register(
            CommandManager.literal(alias)
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    if (target.startsWith("function:")) {
                        return FunctionManager.executeFunction(target.substring("function:".length()), ctx);
                    }
                    String command = VariableManager.substituteVariables(target, ctx);
                    var cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
                    com.mojang.brigadier.ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
                    return cmdDispatcher.execute(parsed);
                })
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        if (target.startsWith("function:")) {
                            return FunctionManager.executeFunction(target.substring("function:".length()), ctx);
                        }
                        String args = StringArgumentType.getString(ctx, "args");
                        String full = alias + " " + args;
                        SyntaxManager.SyntaxMatch match = SyntaxManager.matchInput(full);
                        String command;
                        if (match != null) {
                            String substituted = match.syntax.substituteParameters(target, match.parameters);
                            command = VariableManager.substituteVariables(substituted, ctx);
                        } else {
                            command = target + " " + args;
                            command = VariableManager.substituteVariables(command, ctx);
                        }
                        var cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
                        com.mojang.brigadier.ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
                        return cmdDispatcher.execute(parsed);
                    })
                )
        );
    }
}
