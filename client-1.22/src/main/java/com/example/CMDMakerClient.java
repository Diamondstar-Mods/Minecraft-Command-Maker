package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CMDMakerClient implements ClientModInitializer {
    public static final String MOD_ID = "cmdmakerclient";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        AliasManager.ensureConfigExists();
        AliasManager.loadAliases();
        SyntaxManager.loadSyntaxDefinitions();
        UpdateChecker.check();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCmdCommand(dispatcher);
            registerAddCommand(dispatcher);
            registerAliases(dispatcher);
            registerSetCmdVariable(dispatcher);
            registerSetCmdVar(dispatcher);
            registerDeleteAliasMenu(dispatcher);
            registerSyntaxCommand(dispatcher);
            registerCmdCommandCompat(dispatcher);
        });

        LOGGER.info("CMDMaker Client mod initialized! (v4.0.0-rc.1)");
    }

    // ---- /cmd (consolidated) ----

    private void registerCmdCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("cmd")
                // add
                .then(ClientCommands.literal("add")
                    .then(ClientCommands.argument("alias", StringArgumentType.word())
                        .then(ClientCommands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendFeedback(Component.literal("§cAlias '§f" + alias + "§c' already exists. Use /cmd del " + alias + " first."));
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(Component.literal("§6Added alias: §f/" + alias + " §7-> §f" + command));
                                return 1;
                            })
                        )
                    )
                )
                // del
                .then(ClientCommands.literal("del")
                    .then(ClientCommands.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(Component.literal("§6[" + alias + "] was deleted"));
                            } else {
                                ctx.getSource().sendFeedback(Component.literal("§cAlias '§f" + alias + "§c' not found."));
                            }
                            return 1;
                        })
                    )
                )
                // reload
                .then(ClientCommands.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        SyntaxManager.loadSyntaxDefinitions();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendFeedback(Component.literal("§6Reloaded aliases, functions, and syntax definitions."));
                        return 1;
                    })
                )
                // function
                .then(ClientCommands.literal("function")
                    .then(ClientCommands.argument("functionName", StringArgumentType.word())
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            FunctionManager.executeFunction(functionName, Minecraft.getInstance());
                            return 1;
                        })
                    )
                )
                // function create
                .then(ClientCommands.literal("function")
                    .then(ClientCommands.literal("create")
                        .then(ClientCommands.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                if (name.contains("..") || name.contains("/") || name.contains("\\")) {
                                    ctx.getSource().sendFeedback(Component.literal("§c✖ Invalid function name — don't use §f.. / \\ §cin names"));
                                    return 0;
                                }
                                try {
                                    Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                                    if (Files.exists(file)) {
                                        ctx.getSource().sendFeedback(Component.literal("§c✖ Function §f" + name + "§c already exists"));
                                        return 0;
                                    }
                                    Files.createDirectories(file.getParent());
                                    String content = "# " + name + "\n# Created with Command Maker\n\n# Add your Minecraft commands below\n# Lines starting with # are comments\n";
                                    Files.writeString(file, content);
                                    ctx.getSource().sendFeedback(Component.literal("§a✔ Created §f" + name + " §7| Edit: config/CommandMaker/Functions/" + name + ".mcfunction"));
                                    return 1;
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(Component.literal("§c✖ Error: §7" + e.getMessage()));
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // function delete
                .then(ClientCommands.literal("function")
                    .then(ClientCommands.literal("delete")
                        .then(ClientCommands.argument("name", StringArgumentType.word())
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
                                        ctx.getSource().sendFeedback(Component.literal("§c🗑 Deleted §f" + name));
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFeedback(Component.literal("§c✖ Function §f" + name + "§c not found"));
                                        return 0;
                                    }
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(Component.literal("§c✖ Error: §7" + e.getMessage()));
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // list
                .then(ClientCommands.literal("list")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Component.literal("§6Aliases:"));
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendFeedback(Component.literal("  §f/" + entry.getKey() + " §7-> §f" + entry.getValue()));
                        }
                        source.sendFeedback(Component.literal("§6Functions:"));
                        for (String name : FunctionManager.listLocalFunctions()) {
                            source.sendFeedback(Component.literal("  §f" + name));
                        }
                        return 1;
                    })
                )
                // syntax
                .then(ClientCommands.literal("syntax")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        if (syntaxes.isEmpty()) {
                            source.sendFeedback(Component.literal("§cNo custom syntaxes defined."));
                            return 0;
                        }
                        source.sendFeedback(Component.literal("§6Available Custom Syntaxes:"));
                        for (String name : syntaxes.keySet()) {
                            CommandSyntax syntax = syntaxes.get(name);
                            String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                            source.sendFeedback(Component.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc));
                        }
                        return 1;
                    })
                )
                // setvar
                .then(ClientCommands.literal("setvar")
                    .then(ClientCommands.argument("key", StringArgumentType.word())
                        .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                String value = StringArgumentType.getString(ctx, "value");
                                VariableManager.setVariable(Minecraft.getInstance(), key, value);
                                ctx.getSource().sendFeedback(Component.literal("§6Set variable ${" + key + "} = " + value));
                                return 1;
                            })
                        )
                    )
                )
                // donate
                .then(ClientCommands.literal("donate")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Component.literal("§6§l❤️ Support us on Patreon! ❤️"));
                        source.sendFeedback(Component.literal("§b§nhttps://commandmakerwiki.lucasgeitgey.com/donate.html"));
                        source.sendFeedback(Component.literal("§7Click the link above to open in your browser!"));
                        return 1;
                    })
                )
                // wiki
                .then(ClientCommands.literal("wiki")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Component.literal("§6§l📚 Command Maker Wiki 📚"));
                        source.sendFeedback(Component.literal("§b§nhttps://commandmakerwiki.lucasgeitgey.com"));
                        source.sendFeedback(Component.literal("§7Click the link above to open the wiki in your browser!"));
                        return 1;
                    })
                )
                // help
                .then(ClientCommands.literal("help")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        String updateMsg = UpdateChecker.getUpdateMessage();
                        if (updateMsg != null) {
                            source.sendFeedback(Component.literal(updateMsg));
                        }
                        source.sendFeedback(Component.literal("§6§l⚡ Command Maker Help ⚡"));
                        source.sendFeedback(Component.literal("§e/cmd add <alias> <command> §7— Create a new command alias"));
                        source.sendFeedback(Component.literal("§e/cmd del <alias> §7— Delete an alias"));
                        source.sendFeedback(Component.literal("§e/cmd reload §7— Reload aliases and syntax definitions from config files"));
                        source.sendFeedback(Component.literal("§e/cmd list §7— List all aliases and functions"));
                        source.sendFeedback(Component.literal("§e/cmd function <name> §7— Run a .mcfunction file"));
                        source.sendFeedback(Component.literal("§e/cmd function create <name> §7— Create a new .mcfunction file"));
                        source.sendFeedback(Component.literal("§e/cmd function delete <name> §7— Delete a .mcfunction file"));
                        source.sendFeedback(Component.literal("§e/cmd gui §7— Open the Function Manager GUI"));
                        source.sendFeedback(Component.literal("§e/cmd setvar <name> <value> §7— Set a custom variable"));
                        source.sendFeedback(Component.literal("§e/cmd downloadfunction <name> §7— Download a function from the online library"));
                        source.sendFeedback(Component.literal("§e/cmd listdownloadablefunctions §7— List all downloadable functions"));
                        source.sendFeedback(Component.literal("§e/cmd syntax §7— List custom syntax patterns"));
                        source.sendFeedback(Component.literal("§e/cmd wiki §7— Open the wiki in your browser"));
                        source.sendFeedback(Component.literal("§e/cmd help §7— Show this help message"));
                        source.sendFeedback(Component.literal("§7Use §e/cmd help §7anytime to see this list!"));
                        return 1;
                    })
                )
                // downloadfunction
                .then(ClientCommands.literal("downloadfunction")
                    .then(ClientCommands.argument("function", StringArgumentType.word())
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
                            FunctionManager.downloadFunction(function, Minecraft.getInstance());
                            return 1;
                        })
                    )
                )
                // listdownloadablefunctions
                .then(ClientCommands.literal("listdownloadablefunctions")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(Component.literal("§6⌛ Fetching function library..."));
                        new Thread(() -> {
                            try {
                                Map<String, ManifestEntry> manifest = FunctionManager.fetchFunctionManifest();
                                if (manifest.isEmpty()) {
                                    ctx.getSource().sendFeedback(Component.literal("§c✖ No downloadable functions available"));
                                } else {
                                    ctx.getSource().sendFeedback(Component.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                                    ctx.getSource().sendFeedback(Component.literal("§6§l📦 Downloadable Functions §7(§f" + manifest.size() + "§7 available)"));
                                    ctx.getSource().sendFeedback(Component.literal("§7Use §e/cmd downloadfunction <name> §7or open the GUI with §e/cmd functions"));
                                    ctx.getSource().sendFeedback(Component.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                                    for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                                        String name = entry.getKey();
                                        String desc = entry.getValue().description;
                                        if (desc != null && !desc.isEmpty()) {
                                            ctx.getSource().sendFeedback(Component.literal("§a  ◆ §f" + name + " §8▶ §7" + desc));
                                        } else {
                                            ctx.getSource().sendFeedback(Component.literal("§a  ◆ §f" + name));
                                        }
                                    }
                                    ctx.getSource().sendFeedback(Component.literal("§7─────────────────────────────"));
                                }
                            } catch (Exception e) {
                                ctx.getSource().sendFeedback(Component.literal("§cFailed to fetch function list: " + e.getMessage()));
                            }
                        }).start();
                        return 1;
                    })
                )
        );
    }

    // ---- /addcommand ----

    private void registerAddCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("addcommand")
                .then(ClientCommands.literal("add")
                    .then(ClientCommands.argument("alias", StringArgumentType.word())
                        .then(ClientCommands.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(Component.literal("§6Alias §f/" + alias + " §7-> §f" + command + "§6 added."));
                                return 1;
                            })
                        )
                    )
                )
                .then(ClientCommands.literal("del")
                    .then(ClientCommands.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(Component.literal("§6Alias §f/" + alias + "§6 removed."));
                            } else {
                                ctx.getSource().sendFeedback(Component.literal("§cAlias §f/" + alias + "§c not found."));
                            }
                            return 1;
                        })
                    )
                )
                .then(ClientCommands.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        try {
                            if (!Files.exists(AliasManager.FUNCTIONS_PATH)) {
                                Files.createDirectories(AliasManager.FUNCTIONS_PATH);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Failed to verify functions directory", e);
                        }
                        ctx.getSource().sendFeedback(Component.literal("§6Aliases and functions reloaded."));
                        return 1;
                    })
                )
        );
    }

    // ---- /setcmdvariable ----

    private void registerSetCmdVariable(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("setcmdvariable")
                .then(ClientCommands.argument("variable", StringArgumentType.word())
                    .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String var = StringArgumentType.getString(ctx, "variable");
                            String value = StringArgumentType.getString(ctx, "value");
                            VariableManager.setVariable(Minecraft.getInstance(), var, value);
                            ctx.getSource().sendFeedback(Component.literal("§6Variable $" + var + " set to: " + value));
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /setcmdvar (shorthand) ----

    private void registerSetCmdVar(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("setcmdvar")
                .then(ClientCommands.argument("name", StringArgumentType.word())
                    .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            String value = StringArgumentType.getString(ctx, "value");
                            VariableManager.setVariable(Minecraft.getInstance(), name, value);
                            ctx.getSource().sendFeedback(Component.literal("§6Variable $" + name + " set to: " + value));
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /deletealias / /delaliasmenu ----

    private void registerDeleteAliasMenu(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("deletealias")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(Component.literal("§cNo aliases to delete."));
                        return 0;
                    }
                    source.sendFeedback(Component.literal("§6Delete Aliases Menu:"));
                    int index = 1;
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        source.sendFeedback(Component.literal("  §f[" + index + "] §6/" + alias + " §7-> §f" + cmd));
                        index++;
                    }
                    source.sendFeedback(Component.literal("§7Use: §f/cmd del <alias>§7 to delete"));
                    return 1;
                })
                .then(ClientCommands.argument("alias", StringArgumentType.word())
                    .executes(ctx -> {
                        String alias = StringArgumentType.getString(ctx, "alias");
                        FabricClientCommandSource source = ctx.getSource();
                        if (AliasManager.removeAlias(alias)) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            source.sendFeedback(Component.literal("§6[" + alias + "] was deleted"));
                        } else {
                            source.sendFeedback(Component.literal("§cAlias '§f" + alias + "§c' not found."));
                        }
                        return 1;
                    })
                )
        );
        // Also register /delaliasmenu as alias for /deletealias
        dispatcher.register(
            ClientCommands.literal("delaliasmenu")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(Component.literal("§cNo aliases to delete."));
                        return 0;
                    }
                    source.sendFeedback(Component.literal("§6Delete Aliases Menu:"));
                    int index = 1;
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        source.sendFeedback(Component.literal("  §f[" + index + "] §6/" + alias + " §7-> §f" + cmd));
                        index++;
                    }
                    source.sendFeedback(Component.literal("§7Use: §f/cmd del <alias>§7 to delete"));
                    return 1;
                })
        );
    }

    // ---- /syntax ----

    private void registerSyntaxCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("syntax")
                .then(ClientCommands.literal("list")
                    .executes(ctx -> {
                        StringBuilder sb = new StringBuilder("§6Available Syntaxes:\n");
                        for (Map.Entry<String, CommandSyntax> entry : SyntaxManager.getAllSyntaxes().entrySet()) {
                            sb.append("  §f").append(entry.getKey()).append("§7: §e")
                              .append(entry.getValue().getPattern()).append("\n");
                        }
                        ctx.getSource().sendFeedback(Component.literal(sb.toString()));
                        return 1;
                    })
                )
                .then(ClientCommands.literal("help")
                    .then(ClientCommands.argument("syntaxName", StringArgumentType.word())
                        .executes(ctx -> {
                            String syntaxName = StringArgumentType.getString(ctx, "syntaxName");
                            CommandSyntax syntax = SyntaxManager.getSyntax(syntaxName);
                            if (syntax != null) {
                                ctx.getSource().sendFeedback(Component.literal(syntaxName + ": " + syntax.getPattern() + "\n" + syntax.getDescription()));
                            } else {
                                ctx.getSource().sendFeedback(Component.literal("§cSyntax '" + syntaxName + "' not found."));
                            }
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /cmdcommand (compatibility) ----

    private void registerCmdCommandCompat(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommands.literal("cmdcommand")
                .then(ClientCommands.argument("command", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String command = StringArgumentType.getString(ctx, "command");
                        executeClientCommand(command);
                        return 1;
                    })
                )
        );
    }

    /**
     * Executes a command for real instead of typing it into chat.
     *
     * Client-side commands (fabric client commands, including aliases that
     * target other aliases) run locally through the client dispatcher.
     * Server commands are mirrored into the client dispatcher only for
     * completion — their local nodes are no-ops that return 0 — so anything
     * that resolves to a no-op locally is sent to the server for actual
     * execution.
     */
    public static void executeClientCommand(String command) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        String cleaned = command.startsWith("/") ? command.substring(1) : command;
        CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommands.getActiveDispatcher();
        int result = 0;
        if (dispatcher != null) {
            try {
                FabricClientCommandSource source =
                    (FabricClientCommandSource) client.getConnection().getSuggestionsProvider();
                result = dispatcher.execute(cleaned, source);
            } catch (CommandSyntaxException e) {
                // Unknown in the client tree — let the server try it.
                client.getConnection().sendCommand(cleaned);
                return;
            } catch (ClassCastException e) {
                // Fabric client command API not applied — fall back to server.
                client.getConnection().sendCommand(cleaned);
                return;
            }
        }
        if (result == 0) {
            // Mirrored server command (no-op locally) — run it on the server.
            client.getConnection().sendCommand(cleaned);
        }
    }

    // ---- Alias registration ----

    private void registerAliases(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private void registerAlias(CommandDispatcher<FabricClientCommandSource> dispatcher, String alias, String target) {
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
        dispatcher.register(
            ClientCommands.literal(alias)
                .executes(ctx -> executeAlias(target, ctx))
                .then(ClientCommands.argument("args", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String args = StringArgumentType.getString(ctx, "args");
                        String full = alias + " " + args;
                        SyntaxManager.SyntaxMatch match = SyntaxManager.matchInput(full);
                        String command;
                        if (match != null) {
                            String substituted = match.syntax.substituteParameters(target, match.parameters);
                            command = VariableManager.substituteVariables(substituted, Minecraft.getInstance());
                        } else {
                            command = target + " " + args;
                            command = VariableManager.substituteVariables(command, Minecraft.getInstance());
                        }
                        executeClientCommand(command);
                        return 1;
                    })
                )
        );
    }

    private int executeAlias(String target, CommandContext<FabricClientCommandSource> ctx) {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return 0;

            // Handle function calls
            if (target.startsWith("function:")) {
                String functionName = target.substring(9);
                FunctionManager.executeFunction(functionName, client);
                return 1;
            }

            // Replace variables
            String command = VariableManager.substituteVariables(target, client);

            // Execute the command instead of typing it into chat
            executeClientCommand(command);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to execute alias: {}", target, e);
            ctx.getSource().sendError(Component.literal("Failed to execute alias: " + e.getMessage()));
            return 0;
        }
    }
}
