package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.gui.FunctionChestHandler;

@Mod("nekkycommandmaker")
public class CommandMaker {
    public static final String MOD_ID = "nekkycommandmaker";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("cmdmaker");

    public CommandMaker(IEventBus modEventBus) {
        AliasManager.ensureConfigExists();
        AliasManager.loadAliases();
        SyntaxManager.loadSyntaxDefinitions();
        PermissionManager.initialize();
        UpdateChecker.check();
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        LOGGER.info("Nek's Command Maker (NeoForge) initialized!");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        registerCmdCommand(dispatcher);
        registerAddCommand(dispatcher);
        registerAliases(dispatcher);
        registerSetCmdVariable(dispatcher);
        registerDeleteAliasMenu(dispatcher);
        registerSyntaxCommand(dispatcher);
        registerPermissionCommand(dispatcher);
    }

    private void registerCmdCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cmd")
                .then(Commands.literal("add")
                    .requires(source -> PermissionManager.canManageAliases(source))
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .then(Commands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§cAlias §f/" + alias + "§c already exists!"), false);
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Added alias §f/" + alias + " §a→ §7" + command), false);
                                return 1;
                            })
                        ))
                )
                .then(Commands.literal("del")
                    .then(Commands.argument("alias", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String key : AliasManager.getAliases().keySet()) {
                                builder.suggest(key);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (!AliasManager.hasAlias(alias)) {
                                ctx.getSource().sendSuccess(() -> Component.literal("§cAlias §f/" + alias + "§c not found!"), false);
                                return 0;
                            }
                            AliasManager.removeAlias(alias);
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            ctx.getSource().sendSuccess(() -> Component.literal("§c✖ Deleted alias §f/" + alias), false);
                            return 1;
                        })
                    )
                )
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
                        ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Reloaded aliases, functions, and syntax definitions."), false);
                        return 1;
                    })
                )
                .then(Commands.literal("function")
                    .then(Commands.argument("functionName", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : FunctionManager.listLocalFunctions()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            return FunctionManager.executeFunction(functionName, ctx);
                        })
                    )
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
                .then(Commands.literal("list")
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        CommandSourceStack source = ctx.getSource();
                        source.sendSuccess(() -> Component.literal("§6Aliases:"), false);
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendSuccess(() -> Component.literal("  §e/" + entry.getKey() + " §7→ " + entry.getValue()), false);
                        }
                        source.sendSuccess(() -> Component.literal("§6Functions:"), false);
                        for (String name : FunctionManager.listLocalFunctions()) {
                            source.sendSuccess(() -> Component.literal("  §b" + name), false);
                        }
                        return 1;
                    })
                )
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
                        source.sendSuccess(() -> Component.literal("§e/cmd help §7— Show this help message"), false);
                        source.sendSuccess(() -> Component.literal("§7Use §e/cmd help §7anytime to see this list!"), false);
                        return 1;
                    })
                )
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
                .then(Commands.literal("listdownloadablefunctions")
                    .executes(ctx -> {
                        FunctionManager.listDownloadableFunctions(ctx.getSource());
                        return 1;
                    })
                )
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
                .then(Commands.literal("syntax")
                    .executes(ctx -> {
                        UpdateChecker.sendUpdateMessage(ctx.getSource());
                        CommandSourceStack source = ctx.getSource();
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        if (syntaxes.isEmpty()) {
                            source.sendSuccess(() -> Component.literal("§cNo custom syntaxes defined."), false);
                            return 0;
                        }
                        source.sendSuccess(() -> Component.literal("§6§lCustom Syntax Patterns:"), false);
                        for (CommandSyntax syntax : syntaxes.values()) {
                            String desc = syntax.getDescription();
                            source.sendSuccess(() -> Component.literal("§e  " + syntax.getPattern() + " §7— " + (desc != null ? desc : "No description")), false);
                        }
                        return 1;
                    })
                )
        );
    }

    private void registerAddCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("addcommand")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("alias", StringArgumentType.word())
                    .then(Commands.argument("command", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            String command = StringArgumentType.getString(ctx, "command");
                            if (AliasManager.hasAlias(alias)) {
                                ctx.getSource().sendSuccess(() -> Component.literal("§cAlias already exists!"), false);
                                return 0;
                            }
                            AliasManager.addAlias(alias, command);
                            registerAlias(dispatcher, alias, command);
                            ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Added alias §f/" + alias), false);
                            return 1;
                        })
                    ))
        );
    }

    private void registerSetCmdVariable(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("setcmdvariable")
                .then(Commands.argument("key", StringArgumentType.word())
                    .then(Commands.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String key = StringArgumentType.getString(ctx, "key");
                            String value = StringArgumentType.getString(ctx, "value");
                            return VariableManager.setVariable(ctx.getSource(), key, value);
                        })
                    ))
        );
    }

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
                    source.sendSuccess(() -> Component.literal("§6§lAliases:"), false);
                    int i = 1;
                    for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                        final int idx = i++;
                        source.sendSuccess(() -> Component.literal("§e" + idx + ". §f/" + entry.getKey() + " §7→ " + entry.getValue()), false);
                    }
                    source.sendSuccess(() -> Component.literal("§7Use §e/cmd del <alias> §7to delete, or open §e/cmd gui §7for the GUI"), false);
                    return 1;
                })
        );
    }

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
                    source.sendSuccess(() -> Component.literal("§6§lCustom Syntax Patterns:"), false);
                    for (CommandSyntax syntax : syntaxes.values()) {
                        String desc = syntax.getDescription();
                        source.sendSuccess(() -> Component.literal("§e  " + syntax.getPattern() + " §7— " + (desc != null ? desc : "No description")), false);
                    }
                    return 1;
                })
        );
    }

    private void registerPermissionCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cmmakerperm")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        PermissionManager.initialize();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Permissions reloaded."), false);
                        return 1;
                    })
                )
        );
    }

    private void registerAliases(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher, String alias, String command) {
        if (command.startsWith("function:")) {
            String funcName = command.substring("function:".length());
            dispatcher.register(
                Commands.literal(alias)
                    .requires(source -> PermissionManager.canUseAlias(source, alias))
                    .executes(ctx -> FunctionManager.executeFunction(funcName, ctx))
            );
        } else {
            dispatcher.register(
                Commands.literal(alias)
                    .requires(source -> PermissionManager.canUseAlias(source, alias))
                    .executes(ctx -> {
                        String substituted = VariableManager.substituteVariables(command, ctx);
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        String input = ctx.getInput();
                        for (CommandSyntax syntax : syntaxes.values()) {
                            Map<String, String> params = syntax.extractParameters(input);
                            if (params != null) {
                                substituted = syntax.substituteParameters(substituted, params);
                                break;
                            }
                        }
                        ctx.getSource().getServer().getCommands().performPrefixedCommand(
                            ctx.getSource(), substituted);
                        return 1;
                    })
            );
        }
    }
}
