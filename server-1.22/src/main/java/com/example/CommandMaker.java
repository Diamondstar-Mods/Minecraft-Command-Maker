package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.ParseResults;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.gui.ModScreens;
import com.example.gui.FunctionChestHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.SimpleContainer;

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
        UpdateChecker.check();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
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

    private void registerCmdCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cmd")
                // add
                .then(Commands.literal("add")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§cAlias '§f" + alias + "§c' already exists. Use /cmd del " + alias + " first."), false);
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendSuccess(() -> Component.literal("§6Added alias: §f/" + alias + " §7-> §f" + command), false);
                                return 1;
                            })
                        )
                    )
                )
                // del
                .then(Commands.literal("del")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendSuccess(() -> Component.literal("§6[" + alias + "] was deleted"), false);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("§cAlias '§f" + alias + "§c' not found."), false);
                            }
                            return 1;
                        })
                    )
                )
                // reload
                .then(Commands.literal("reload")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        AliasManager.loadAliases();
                        SyntaxManager.loadSyntaxDefinitions();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendSuccess(() -> Component.literal("§6Reloaded aliases, functions, and syntax definitions."), false);
                        return 1;
                    })
                )
                // function
                .then(Commands.literal("function")
                    .then(Commands.argument("functionName", StringArgumentType.word())
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            return FunctionManager.executeFunction(functionName, ctx);
                        })
                    )
                )
                // list
                .then(Commands.literal("list")
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        CommandSourceStack source = ctx.getSource();
                        source.sendSuccess(() -> Component.literal("Aliases:"), false);
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendSuccess(() -> Component.literal("  /" + entry.getKey() + " -> " + entry.getValue()), false);
                        }
                        source.sendSuccess(() -> Component.literal("Functions:"), false);
                        for (String name : FunctionManager.listLocalFunctions()) {
                            source.sendSuccess(() -> Component.literal("  " + name), false);
                        }
                        return 1;
                    })
                )
                // gui
                .then(Commands.literal("gui")
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        Player player = ctx.getSource().getPlayer();
                        if (player != null) {
                            player.openMenu(new SimpleMenuProvider(
                                (syncId, inv, p) -> new FunctionChestHandler(syncId, inv),
                                Component.literal("Command Maker - Functions")
                            ));
                        } else {
                            ctx.getSource().sendSuccess(() -> Component.literal("§cThis command can only be used by a player"), false);
                        }
                        return 1;
                    })
                )
                // functions - open function manager GUI
                .then(Commands.literal("functions")
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayer();
                        if (player != null) {
                            player.openMenu(new SimpleMenuProvider(
                                (syncId, inv, p) -> new FunctionChestHandler(syncId, inv),
                                Component.literal("Command Maker - Functions")
                            ));
                        } else {
                            ctx.getSource().sendSuccess(() -> Component.literal("§cThis command can only be used by a player"), false);
                        }
                        return 1;
                    })
                )
                // function subcommands
                .then(Commands.literal("function")
                    .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                if (name.contains("..") || name.contains("/") || name.contains("\\")) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Invalid function name — don't use §f.. / \\ §cin names"), false);
                                    return 0;
                                }
                                try {
                                    Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                                    if (Files.exists(file)) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Function §f" + name + "§c already exists"), false);
                                        return 0;
                                    }
                                    Files.createDirectories(file.getParent());
                                    String content = "# " + name + "\n# Created with Command Maker\n\n# Add your Minecraft commands below\n# Lines starting with # are comments\n";
                                    Files.writeString(file, content);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Created §f" + name + " §7| Edit: config/CommandMaker/Functions/" + name + ".mcfunction"), false);
                                    return 1;
                                } catch (Exception e) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Error: §7" + e.getMessage()), false);
                                    return 0;
                                }
                            })
                        )
                    )
                    .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
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
                                        ctx.getSource().sendSuccess(() -> Component.literal("§c🗑 Deleted §f" + name), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Function §f" + name + "§c not found"), false);
                                        return 0;
                                    }
                                } catch (Exception e) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Error: §7" + e.getMessage()), false);
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // syntax
                .then(Commands.literal("syntax")
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        if (syntaxes.isEmpty()) {
                            source.sendSuccess(() -> Component.literal("§cNo custom syntaxes defined."), false);
                            return 0;
                        }
                        source.sendSuccess(() -> Component.literal("§6Available Custom Syntaxes:"), false);
                        for (String name : syntaxes.keySet()) {
                            CommandSyntax syntax = syntaxes.get(name);
                            String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                            source.sendSuccess(() -> Component.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
                        }
                        return 1;
                    })
                )
                // setvar
                .then(Commands.literal("setvar")
                    .then(Commands.argument("key", StringArgumentType.word())
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                String value = StringArgumentType.getString(ctx, "value");
                                return VariableManager.setVariable(ctx.getSource(), key, value);
                            })
                        )
                    )
                )
                // donate
                .then(Commands.literal("donate")
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        source.sendSuccess(() -> Component.literal("§6§l❤️ Support us on Patreon! ❤️"), false);
                        String tellrawCmd = "tellraw " + source.getTextName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com/donate.html\"}}";
                        var cmdDispatcher = source.getServer().getCommands().getDispatcher();
                        var parsed = cmdDispatcher.parse(tellrawCmd, source);
                        cmdDispatcher.execute(parsed);
                        source.sendSuccess(() -> Component.literal("§7Click the link above to open in your browser!"), false);
                        return 1;
                    })
                )
                // wiki
                .then(Commands.literal("wiki")
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        source.sendSuccess(() -> Component.literal("§6§l📚 Command Maker Wiki 📚"), false);
                        String tellrawCmd = "tellraw " + source.getTextName() + " {\"text\":\"https://commandmakerwiki.lucasgeitgey.com\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://commandmakerwiki.lucasgeitgey.com\"}}";
                        var cmdDispatcher = source.getServer().getCommands().getDispatcher();
                        var parsed = cmdDispatcher.parse(tellrawCmd, source);
                        cmdDispatcher.execute(parsed);
                        source.sendSuccess(() -> Component.literal("§7Click the link above to open the wiki in your browser!"), false);
                        return 1;
                    })
                )
                // help
                .then(Commands.literal("help")
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        CommandSourceStack source = ctx.getSource();
                        source.sendSuccess(() -> Component.literal("§6§l⚡ Command Maker Help ⚡"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd add <alias> <command> §7— Create a new command alias"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd del <alias> §7— Delete an alias"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd reload §7— Reload aliases and syntax definitions from config files"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd list §7— List all aliases and functions"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd function <name> §7— Run a .mcfunction file"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd function create <name> §7— Create a new .mcfunction file"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd function delete <name> §7— Delete a .mcfunction file"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd gui §7— Open the Function Manager GUI"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd setvar <name> <value> §7— Set a custom variable"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd downloadfunction <name> §7— Download a function from the online library"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd listdownloadablefunctions §7— List all downloadable functions"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd syntax §7— List custom syntax patterns"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd wiki §7— Open the wiki in your browser"), false);
                        source.sendSuccess(() -> Component.literal("§e/cmd help §7— Show this help message"), false);
                        source.sendSuccess(() -> Component.literal("§7Use §e/cmd help §7anytime to see this list!"), false);
                        return 1;
                    })
                )
                // downloadfunction
                .then(Commands.literal("downloadfunction")
                    .then(Commands.argument("function", StringArgumentType.word())
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            Map<String, ManifestEntry> manifest = FunctionManager.fetchFunctionManifest();
                            for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                                String name = entry.getKey();
                                String desc = entry.getValue().description;
                                if (desc != null && !desc.isEmpty()) {
                                    builder.suggest(name, Component.literal("§7" + desc));
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
                .then(Commands.literal("listdownloadablefunctions")
                    .executes(ctx -> {
                        FunctionManager.listDownloadableFunctions(ctx.getSource());
                        return 1;
                    })
                )
        );
    }

    // ---- /addcommand ----

    private void registerAddCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("addcommand")
                .requires(source -> PermissionManager.canUseAddCommand(source))
                .then(Commands.literal("add")
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                if (!PermissionManager.canManageAliases(ctx.getSource())) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("You don't have permission to add aliases."), false);
                                    return 0;
                                }
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendSuccess(() -> Component.literal("Alias /" + alias + " -> " + command + " added."), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("del")
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            if (!PermissionManager.canManageAliases(ctx.getSource())) {
                                ctx.getSource().sendSuccess(() -> Component.literal("You don't have permission to delete aliases."), false);
                                return 0;
                            }
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendSuccess(() -> Component.literal("Alias /" + alias + " removed."), false);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("Alias /" + alias + " not found."), false);
                            }
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendSuccess(() -> Component.literal("Aliases reloaded."), false);
                        return 1;
                    })
                )
        );
    }

    // ---- /setcmdvariable ----

    private void registerSetCmdVariable(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("setcmdvariable")
                .then(Commands.argument("variable", StringArgumentType.word())
                    .then(Commands.argument("value", StringArgumentType.greedyString())
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

    private void registerAliases(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher, String alias, String target) {
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
        dispatcher.register(
            Commands.literal(alias)
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!PermissionManager.canUseAlias(source, alias)) {
                        source.sendSuccess(() -> Component.literal("You don't have permission to use this alias."), false);
                        return 0;
                    }
                    if (target.startsWith("function:")) {
                        return FunctionManager.executeFunction(target.substring("function:".length()), ctx);
                    }
                    String command = VariableManager.substituteVariables(target, ctx);
                    var cmdDispatcher = source.getServer().getCommands().getDispatcher();
                    ParseResults<CommandSourceStack> parsed = cmdDispatcher.parse(command, source);
                    return cmdDispatcher.execute(parsed);
                })
                .then(Commands.argument("args", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        if (!PermissionManager.canUseAlias(source, alias)) {
                            source.sendSuccess(() -> Component.literal("You don't have permission to use this alias."), false);
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
                        var cmdDispatcher = source.getServer().getCommands().getDispatcher();
                        ParseResults<CommandSourceStack> parsed = cmdDispatcher.parse(command, source);
                        return cmdDispatcher.execute(parsed);
                    })
                )
        );
    }

    // ---- /deletealias ----

    private void registerDeleteAliasMenu(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("deletealias")
                .executes(ctx -> {
                    UpdateChecker.sendUpdateMessage(ctx.getSource());
                    CommandSourceStack source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendSuccess(() -> Component.literal("§cNo aliases to delete."), false);
                        return 0;
                    }
                    source.sendSuccess(() -> Component.literal("§6Delete Aliases Menu:"), false);
                    final int[] index = {1};
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        int idx = index[0];
                        source.sendSuccess(() -> Component.literal("  §f[" + idx + "] §6/" + alias + " §7-> §f" + cmd), false);
                        index[0]++;
                    }
                    source.sendSuccess(() -> Component.literal("§7Use: §f/cmd del <alias>§7 to delete"), false);
                    return 1;
                })
                .then(Commands.argument("alias", StringArgumentType.word())
                    .executes(ctx -> {
                        String alias = StringArgumentType.getString(ctx, "alias");
                        CommandSourceStack source = ctx.getSource();
                        if (AliasManager.removeAlias(alias)) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            source.sendSuccess(() -> Component.literal("§6[" + alias + "] was deleted"), false);
                        } else {
                            source.sendSuccess(() -> Component.literal("§cAlias '§f" + alias + "§c' not found."), false);
                        }
                        return 1;
                    })
                )
        );
    }

    // ---- /syntax ----

    private void registerSyntaxCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("syntax")
                .executes(ctx -> {
                    UpdateChecker.sendUpdateMessage(ctx.getSource());
                    CommandSourceStack source = ctx.getSource();
                    Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                    if (syntaxes.isEmpty()) {
                        source.sendSuccess(() -> Component.literal("§cNo custom syntaxes defined."), false);
                        return 0;
                    }
                    source.sendSuccess(() -> Component.literal("§6Available Custom Syntaxes:"), false);
                    for (String name : syntaxes.keySet()) {
                        CommandSyntax syntax = syntaxes.get(name);
                        String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                        source.sendSuccess(() -> Component.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc), false);
                    }
                    return 1;
                })
        );
    }

    // ---- /cmmakerperm ----

    private void registerPermissionCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cmmakerperm")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                .then(Commands.literal("grant")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String playerName = StringArgumentType.getString(ctx, "player");
                                ctx.getSource().getServer().getPlayerList().getPlayer(playerName);
                                ctx.getSource().sendSuccess(() -> Component.literal("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("revoke")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ctx.getSource().sendSuccess(() -> Component.literal("§cNote: For full permission management, use LuckPerms or edit permissions.json directly"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("check")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("permission", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ctx.getSource().sendSuccess(() -> Component.literal("§cNote: For full permission checking, use LuckPerms"), false);
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        PermissionManager.initialize();
                        ctx.getSource().sendSuccess(() -> Component.literal("§aPermission config reloaded!"), false);
                        return 1;
                    })
                )
        );
    }
}
