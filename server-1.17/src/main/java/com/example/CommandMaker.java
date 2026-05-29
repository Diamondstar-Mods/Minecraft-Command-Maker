package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.ParseResults;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.gui.ModScreens;
import com.example.gui.FunctionChestHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.inventory.SimpleInventory;

public class CommandMaker implements ModInitializer {
    public static final String MOD_ID = "cmdmaker";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AliasManager.ensureConfigExists();
        AliasManager.loadAliases();
        SyntaxManager.loadSyntaxDefinitions();
        PermissionManager.initialize();
        ModScreens.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCmdCommand(dispatcher);
            registerAddCommand(dispatcher);
            registerAliases(dispatcher);
            registerSetCmdVariable(dispatcher);
            registerDeleteAliasMenu(dispatcher);
            registerSyntaxCommand(dispatcher);
            registerPermissionCommand(dispatcher);
        });

        LOGGER.info("Alias mod initialized!");
    }

    // ---- /cmd (consolidated) ----

    private void registerCmdCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("cmd")
                // add
                .then(CommandManager.literal("add")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendFeedback(new LiteralText("§cAlias '§f" + alias + "§c' already exists. Use /cmd del " + alias + " first."), false);
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(new LiteralText("§6Added alias: §f/" + alias + " §7-> §f" + command), false);
                                return 1;
                            })
                        )
                    )
                )
                // del
                .then(CommandManager.literal("del")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(new LiteralText("§6[" + alias + "] was deleted"), false);
                            } else {
                                ctx.getSource().sendFeedback(new LiteralText("§cAlias '§f" + alias + "§c' not found."), false);
                            }
                            return 1;
                        })
                    )
                )
                // reload
                .then(CommandManager.literal("reload")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        SyntaxManager.loadSyntaxDefinitions();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendFeedback(new LiteralText("§6Reloaded aliases, functions, and syntax definitions."), false);
                        return 1;
                    })
                )
                // function
                .then(CommandManager.literal("function")
                    .then(CommandManager.argument("functionName", StringArgumentType.word())
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            return FunctionManager.executeFunction(functionName, ctx);
                        })
                    )
                )
                // list
                .then(CommandManager.literal("list")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        source.sendFeedback(new LiteralText("Aliases:"), false);
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendFeedback(new LiteralText("  /" + entry.getKey() + " -> " + entry.getValue()), false);
                        }
                        source.sendFeedback(new LiteralText("Functions:"), false);
                        for (String name : FunctionManager.listLocalFunctions()) {
                            source.sendFeedback(new LiteralText("  " + name), false);
                        }
                        return 1;
                    })
                )
                // gui
                .then(CommandManager.literal("gui")
                    .executes(ctx -> {
                        PlayerEntity player = ctx.getSource().getPlayer();
                        if (player != null) {
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                (syncId, inv, p) -> new FunctionChestHandler(syncId, inv, ModScreens.FUNCTION_CHEST),
                                new LiteralText("Command Maker - Functions")
                            ));
                        } else {
                            ctx.getSource().sendFeedback(new LiteralText("§cThis command can only be used by a player"), false);
                        }
                        return 1;
                    })
                )
                // functions - open function manager GUI
                .then(CommandManager.literal("functions")
                    .executes(ctx -> {
                        PlayerEntity player = ctx.getSource().getPlayer();
                        if (player != null) {
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                (syncId, inv, p) -> new FunctionChestHandler(syncId, inv, ModScreens.FUNCTION_CHEST),
                                new LiteralText("Command Maker - Functions")
                            ));
                        } else {
                            ctx.getSource().sendFeedback(new LiteralText("§cThis command can only be used by a player"), false);
                        }
                        return 1;
                    })
                )
                // function subcommands
                .then(CommandManager.literal("function")
                    .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                if (name.contains("..") || name.contains("/") || name.contains("\\")) {
                                    ctx.getSource().sendFeedback(new LiteralText("§c✖ Invalid function name — don't use §f.. / \\ §cin names"), false);
                                    return 0;
                                }
                                try {
                                    Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                                    if (Files.exists(file)) {
                                        ctx.getSource().sendFeedback(new LiteralText("§c✖ Function §f" + name + "§c already exists"), false);
                                        return 0;
                                    }
                                    Files.createDirectories(file.getParent());
                                    String content = "# " + name + "\n# Created with Command Maker\n\n# Add your Minecraft commands below\n# Lines starting with # are comments\n";
                                    Files.writeString(file, content);
                                    ctx.getSource().sendFeedback(new LiteralText("§a✔ Created §f" + name + " §7| Edit: config/CommandMaker/Functions/" + name + ".mcfunction"), false);
                                    return 1;
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(new LiteralText("§c✖ Error: §7" + e.getMessage()), false);
                                    return 0;
                                }
                            })
                        )
                    )
                    .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (String name : FunctionManager.listLocalFunctions()) {
                                    builder.suggest(name);
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                try {
                                    Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                                    if (Files.deleteIfExists(file)) {
                                        ctx.getSource().sendFeedback(new LiteralText("§c🗑 Deleted §f" + name), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFeedback(new LiteralText("§c✖ Function §f" + name + "§c not found"), false);
                                        return 0;
                                    }
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(new LiteralText("§c✖ Error: §7" + e.getMessage()), false);
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // syntax
                .then(CommandManager.literal("syntax")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        if (syntaxes.isEmpty()) {
                            source.sendFeedback(new LiteralText("§cNo custom syntaxes defined."), false);
                            return 0;
                        }
                        source.sendFeedback(new LiteralText("§6Available Custom Syntaxes:"), false);
                        for (String name : syntaxes.keySet()) {
                            CommandSyntax syntax = syntaxes.get(name);
                            String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                            source.sendFeedback(new LiteralText("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
                        }
                        return 1;
                    })
                )
                // setvar
                .then(CommandManager.literal("setvar")
                    .then(CommandManager.argument("key", StringArgumentType.word())
                        .then(CommandManager.argument("value", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                String value = StringArgumentType.getString(ctx, "value");
                                return VariableManager.setVariable(ctx.getSource(), key, value);
                            })
                        )
                    )
                )
                // donate
                .then(CommandManager.literal("donate")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        source.sendFeedback(new LiteralText("§6§l❤️ Support us on Patreon! ❤️"), false);
                        String tellrawCmd = "tellraw " + source.getName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\"}}";
                        var cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
                        var parsed = cmdDispatcher.parse(tellrawCmd, source);
                        cmdDispatcher.execute(parsed);
                        source.sendFeedback(new LiteralText("§7Click the link above to open in your browser!"), false);
                        return 1;
                    })
                )
                // wiki
                .then(CommandManager.literal("wiki")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        source.sendFeedback(new LiteralText("§6§l📚 Command Maker Wiki 📚"), false);
                        String tellrawCmd = "tellraw " + source.getName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com\"}}";
                        var cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
                        var parsed = cmdDispatcher.parse(tellrawCmd, source);
                        cmdDispatcher.execute(parsed);
                        source.sendFeedback(new LiteralText("§7Click the link above to open the wiki in your browser!"), false);
                        return 1;
                    })
                )
                // downloadfunction
                .then(CommandManager.literal("downloadfunction")
                    .then(CommandManager.argument("function", StringArgumentType.word())
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            Map<String, String> manifest = FunctionManager.fetchFunctionManifest();
                            for (Map.Entry<String, String> entry : manifest.entrySet()) {
                                String name = entry.getKey();
                                String desc = entry.getValue();
                                if (desc != null && !desc.isEmpty()) {
                                    builder.suggest(name, new LiteralText("§7" + desc));
                                } else {
                                    builder.suggest(name);
                                }
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
                // listdownloadablefunctions
                .then(CommandManager.literal("listdownloadablefunctions")
                    .executes(ctx -> {
                        FunctionManager.listDownloadableFunctions(ctx.getSource());
                        return 1;
                    })
                )
        );
    }

    // ---- /addcommand ----

    private void registerAddCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("addcommand")
                .requires(source -> PermissionManager.canUseAddCommand(source))
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                if (!PermissionManager.canManageAliases(ctx.getSource())) {
                                    ctx.getSource().sendFeedback(new LiteralText("You don't have permission to add aliases."), false);
                                    return 0;
                                }
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(new LiteralText("Alias /" + alias + " -> " + command + " added."), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("del")
                    .then(CommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            if (!PermissionManager.canManageAliases(ctx.getSource())) {
                                ctx.getSource().sendFeedback(new LiteralText("You don't have permission to delete aliases."), false);
                                return 0;
                            }
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(new LiteralText("Alias /" + alias + " removed."), false);
                            } else {
                                ctx.getSource().sendFeedback(new LiteralText("Alias /" + alias + " not found."), false);
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
                        ctx.getSource().sendFeedback(new LiteralText("Aliases reloaded."), false);
                        return 1;
                    })
                )
        );
    }

    // ---- /setcmdvariable ----

    private void registerSetCmdVariable(CommandDispatcher<ServerCommandSource> dispatcher) {
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

    // ---- Alias registration as Brigadier commands ----

    private void registerAliases(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private void registerAlias(CommandDispatcher<ServerCommandSource> dispatcher, String alias, String target) {
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
        dispatcher.register(
            CommandManager.literal(alias)
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    if (!PermissionManager.canUseAlias(source, alias)) {
                        source.sendFeedback(new LiteralText("You don't have permission to use this alias."), false);
                        return 0;
                    }
                    if (target.startsWith("function:")) {
                        return FunctionManager.executeFunction(target.substring("function:".length()), ctx);
                    }
                    String command = VariableManager.substituteVariables(target, ctx);
                    var cmdDispatcher = source.getServer().getCommandManager().getDispatcher();
                    ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
                    return cmdDispatcher.execute(parsed);
                })
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        if (!PermissionManager.canUseAlias(source, alias)) {
                            source.sendFeedback(new LiteralText("You don't have permission to use this alias."), false);
                            return 0;
                        }
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
                        ParseResults<ServerCommandSource> parsed = cmdDispatcher.parse(command, source);
                        return cmdDispatcher.execute(parsed);
                    })
                )
        );
    }

    // ---- /deletealias ----

    private void registerDeleteAliasMenu(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("deletealias")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(new LiteralText("§cNo aliases to delete."), false);
                        return 0;
                    }
                    source.sendFeedback(new LiteralText("§6Delete Aliases Menu:"), false);
                    final int[] index = {1};
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        int idx = index[0];
                        source.sendFeedback(new LiteralText("  §f[" + idx + "] §6/" + alias + " §7-> §f" + cmd), false);
                        index[0]++;
                    }
                    source.sendFeedback(new LiteralText("§7Use: §f/cmd del <alias>§7 to delete"), false);
                    return 1;
                })
                .then(CommandManager.argument("alias", StringArgumentType.word())
                    .executes(ctx -> {
                        String alias = StringArgumentType.getString(ctx, "alias");
                        ServerCommandSource source = ctx.getSource();
                        if (AliasManager.removeAlias(alias)) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            source.sendFeedback(new LiteralText("§6[" + alias + "] was deleted"), false);
                        } else {
                            source.sendFeedback(new LiteralText("§cAlias '§f" + alias + "§c' not found."), false);
                        }
                        return 1;
                    })
                )
        );
    }

    // ---- /syntax ----

    private void registerSyntaxCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("syntax")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                    if (syntaxes.isEmpty()) {
                        source.sendFeedback(new LiteralText("§cNo custom syntaxes defined."), false);
                        return 0;
                    }
                    source.sendFeedback(new LiteralText("§6Available Custom Syntaxes:"), false);
                    for (String name : syntaxes.keySet()) {
                        CommandSyntax syntax = syntaxes.get(name);
                        String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                        source.sendFeedback(new LiteralText("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
                    }
                    return 1;
                })
        );
    }

    // ---- /cmmakerperm ----

    private void registerPermissionCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("cmmakerperm")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("grant")
                    .then(CommandManager.argument("player", StringArgumentType.word())
                        .then(CommandManager.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String playerName = StringArgumentType.getString(ctx, "player");
                                ctx.getSource().getServer().getPlayerManager().getPlayer(playerName);
                                ctx.getSource().sendFeedback(new LiteralText("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("revoke")
                    .then(CommandManager.argument("player", StringArgumentType.word())
                        .then(CommandManager.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(new LiteralText("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("check")
                    .then(CommandManager.argument("player", StringArgumentType.word())
                        .then(CommandManager.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(new LiteralText("§cNote: For full permission checking, use LuckPerms"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("reload")
                    .executes(ctx -> {
                        PermissionManager.initialize();
                        ctx.getSource().sendFeedback(new LiteralText("§aPermission config reloaded!"), false);
                        return 1;
                    })
                )
        );
    }
}
