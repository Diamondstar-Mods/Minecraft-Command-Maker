package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.gui.FunctionManagerScreen;

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
            ClientCommandManager.literal("cmd")
                // add
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("alias", StringArgumentType.word())
                        .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                if (AliasManager.hasAlias(alias)) {
                                    ctx.getSource().sendFeedback(Text.literal("§cAlias '§f" + alias + "§c' already exists. Use /cmd del " + alias + " first."));
                                    return 0;
                                }
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(Text.literal("§6Added alias: §f/" + alias + " §7-> §f" + command));
                                return 1;
                            })
                        )
                    )
                )
                // del
                .then(ClientCommandManager.literal("del")
                    .then(ClientCommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(Text.literal("§6[" + alias + "] was deleted"));
                            } else {
                                ctx.getSource().sendFeedback(Text.literal("§cAlias '§f" + alias + "§c' not found."));
                            }
                            return 1;
                        })
                    )
                )
                // reload
                .then(ClientCommandManager.literal("reload")
                    .executes(ctx -> {
                        AliasManager.loadAliases();
                        SyntaxManager.loadSyntaxDefinitions();
                        for (String alias : AliasManager.getAliases().keySet()) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                        }
                        registerAliases(dispatcher);
                        ctx.getSource().sendFeedback(Text.literal("§6Reloaded aliases, functions, and syntax definitions."));
                        return 1;
                    })
                )
                // function
                .then(ClientCommandManager.literal("function")
                    .then(ClientCommandManager.argument("functionName", StringArgumentType.word())
                        .executes(ctx -> {
                            String functionName = StringArgumentType.getString(ctx, "functionName");
                            FunctionManager.executeFunction(functionName, MinecraftClient.getInstance());
                            return 1;
                        })
                    )
                )
                // function create
                .then(ClientCommandManager.literal("function")
                    .then(ClientCommandManager.literal("create")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                if (name.contains("..") || name.contains("/") || name.contains("\\")) {
                                    ctx.getSource().sendFeedback(Text.literal("§c✖ Invalid function name — don't use §f.. / \\ §cin names"));
                                    return 0;
                                }
                                try {
                                    Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                                    if (Files.exists(file)) {
                                        ctx.getSource().sendFeedback(Text.literal("§c✖ Function §f" + name + "§c already exists"));
                                        return 0;
                                    }
                                    Files.createDirectories(file.getParent());
                                    String content = "# " + name + "\n# Created with Command Maker\n\n# Add your Minecraft commands below\n# Lines starting with # are comments\n";
                                    Files.writeString(file, content);
                                    ctx.getSource().sendFeedback(Text.literal("§a✔ Created §f" + name + " §7| Edit: config/CommandMaker/Functions/" + name + ".mcfunction"));
                                    return 1;
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(Text.literal("§c✖ Error: §7" + e.getMessage()));
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // function delete
                .then(ClientCommandManager.literal("function")
                    .then(ClientCommandManager.literal("delete")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
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
                                        ctx.getSource().sendFeedback(Text.literal("§c🗑 Deleted §f" + name));
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFeedback(Text.literal("§c✖ Function §f" + name + "§c not found"));
                                        return 0;
                                    }
                                } catch (Exception e) {
                                    ctx.getSource().sendFeedback(Text.literal("§c✖ Error: §7" + e.getMessage()));
                                    return 0;
                                }
                            })
                        )
                    )
                )
                // list
                .then(ClientCommandManager.literal("list")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Text.literal("§6Aliases:"));
                        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                            source.sendFeedback(Text.literal("  §f/" + entry.getKey() + " §7-> §f" + entry.getValue()));
                        }
                        source.sendFeedback(Text.literal("§6Functions:"));
                        for (String name : FunctionManager.listLocalFunctions()) {
                            source.sendFeedback(Text.literal("  §f" + name));
                        }
                        return 1;
                    })
                )
                // gui
                .then(ClientCommandManager.literal("gui")
                    .executes(ctx -> {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().setScreen(new FunctionManagerScreen(null));
                        });
                        return 1;
                    })
                )
                // functions - open function manager GUI
                .then(ClientCommandManager.literal("functions")
                    .executes(ctx -> {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().setScreen(new FunctionManagerScreen(null));
                        });
                        return 1;
                    })
                )
                // syntax
                .then(ClientCommandManager.literal("syntax")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                        if (syntaxes.isEmpty()) {
                            source.sendFeedback(Text.literal("§cNo custom syntaxes defined."));
                            return 0;
                        }
                        source.sendFeedback(Text.literal("§6Available Custom Syntaxes:"));
                        for (String name : syntaxes.keySet()) {
                            CommandSyntax syntax = syntaxes.get(name);
                            String desc = syntax.getDescription().isEmpty() ? "" : " - " + syntax.getDescription();
                            source.sendFeedback(Text.literal("  §f" + name + "§7: §e" + syntax.getPattern() + desc));
                        }
                        return 1;
                    })
                )
                // setvar
                .then(ClientCommandManager.literal("setvar")
                    .then(ClientCommandManager.argument("key", StringArgumentType.word())
                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                String value = StringArgumentType.getString(ctx, "value");
                                VariableManager.setVariable(MinecraftClient.getInstance(), key, value);
                                ctx.getSource().sendFeedback(Text.literal("§6Set variable ${" + key + "} = " + value));
                                return 1;
                            })
                        )
                    )
                )
                // donate
                .then(ClientCommandManager.literal("donate")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Text.literal("§6§l❤️ Support us on Patreon! ❤️"));
                        source.sendFeedback(Text.literal("§b§nhttps://commandmakerwiki.lucasgeitgey.com/donate.html"));
                        source.sendFeedback(Text.literal("§7Click the link above to open in your browser!"));
                        return 1;
                    })
                )
                // wiki
                .then(ClientCommandManager.literal("wiki")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        source.sendFeedback(Text.literal("§6§l📚 Command Maker Wiki 📚"));
                        source.sendFeedback(Text.literal("§b§nhttps://commandmakerwiki.lucasgeitgey.com"));
                        source.sendFeedback(Text.literal("§7Click the link above to open the wiki in your browser!"));
                        return 1;
                    })
                )
                // help
                .then(ClientCommandManager.literal("help")
                    .executes(ctx -> {
                        FabricClientCommandSource source = ctx.getSource();
                        String updateMsg = UpdateChecker.getUpdateMessage();
                        if (updateMsg != null) {
                            source.sendFeedback(Text.literal(updateMsg));
                        }
                        source.sendFeedback(Text.literal("§6§l⚡ Command Maker Help ⚡"));
                        source.sendFeedback(Text.literal("§e/cmd add <alias> <command> §7— Create a new command alias"));
                        source.sendFeedback(Text.literal("§e/cmd del <alias> §7— Delete an alias"));
                        source.sendFeedback(Text.literal("§e/cmd reload §7— Reload aliases and syntax definitions from config files"));
                        source.sendFeedback(Text.literal("§e/cmd list §7— List all aliases and functions"));
                        source.sendFeedback(Text.literal("§e/cmd function <name> §7— Run a .mcfunction file"));
                        source.sendFeedback(Text.literal("§e/cmd function create <name> §7— Create a new .mcfunction file"));
                        source.sendFeedback(Text.literal("§e/cmd function delete <name> §7— Delete a .mcfunction file"));
                        source.sendFeedback(Text.literal("§e/cmd gui §7— Open the Function Manager GUI"));
                        source.sendFeedback(Text.literal("§e/cmd setvar <name> <value> §7— Set a custom variable"));
                        source.sendFeedback(Text.literal("§e/cmd downloadfunction <name> §7— Download a function from the online library"));
                        source.sendFeedback(Text.literal("§e/cmd listdownloadablefunctions §7— List all downloadable functions"));
                        source.sendFeedback(Text.literal("§e/cmd syntax §7— List custom syntax patterns"));
                        source.sendFeedback(Text.literal("§e/cmd wiki §7— Open the wiki in your browser"));
                        source.sendFeedback(Text.literal("§e/cmd help §7— Show this help message"));
                        source.sendFeedback(Text.literal("§7Use §e/cmd help §7anytime to see this list!"));
                        return 1;
                    })
                )
                // downloadfunction
                .then(ClientCommandManager.literal("downloadfunction")
                    .then(ClientCommandManager.argument("function", StringArgumentType.word())
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            Map<String, ManifestEntry> manifest = FunctionManager.fetchFunctionManifest();
                            for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                                String name = entry.getKey();
                                String desc = entry.getValue().description;
                                if (desc != null && !desc.isEmpty()) {
                                    builder.suggest(name, Text.literal("§7" + desc));
                                } else {
                                    builder.suggest(name);
                                }
                            }
                            return builder.build();
                        }))
                        .executes(ctx -> {
                            String function = StringArgumentType.getString(ctx, "function");
                            FunctionManager.downloadFunction(function, MinecraftClient.getInstance());
                            return 1;
                        })
                    )
                )
                // listdownloadablefunctions
                .then(ClientCommandManager.literal("listdownloadablefunctions")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(Text.literal("§6⌛ Fetching function library..."));
                        new Thread(() -> {
                            try {
                                Map<String, ManifestEntry> manifest = FunctionManager.fetchFunctionManifest();
                                if (manifest.isEmpty()) {
                                    ctx.getSource().sendFeedback(Text.literal("§c✖ No downloadable functions available"));
                                } else {
                                    ctx.getSource().sendFeedback(Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                                    ctx.getSource().sendFeedback(Text.literal("§6§l📦 Downloadable Functions §7(§f" + manifest.size() + "§7 available)"));
                                    ctx.getSource().sendFeedback(Text.literal("§7Use §e/cmd downloadfunction <name> §7or open the GUI with §e/cmd functions"));
                                    ctx.getSource().sendFeedback(Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                                    for (Map.Entry<String, ManifestEntry> entry : manifest.entrySet()) {
                                        String name = entry.getKey();
                                        String desc = entry.getValue().description;
                                        if (desc != null && !desc.isEmpty()) {
                                            ctx.getSource().sendFeedback(Text.literal("§a  ◆ §f" + name + " §8▶ §7" + desc));
                                        } else {
                                            ctx.getSource().sendFeedback(Text.literal("§a  ◆ §f" + name));
                                        }
                                    }
                                    ctx.getSource().sendFeedback(Text.literal("§7─────────────────────────────"));
                                }
                            } catch (Exception e) {
                                ctx.getSource().sendFeedback(Text.literal("§cFailed to fetch function list: " + e.getMessage()));
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
            ClientCommandManager.literal("addcommand")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("alias", StringArgumentType.word())
                        .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String alias = StringArgumentType.getString(ctx, "alias");
                                String command = StringArgumentType.getString(ctx, "command");
                                AliasManager.addAlias(alias, command);
                                registerAlias(dispatcher, alias, command);
                                ctx.getSource().sendFeedback(Text.literal("§6Alias §f/" + alias + " §7-> §f" + command + "§6 added."));
                                return 1;
                            })
                        )
                    )
                )
                .then(ClientCommandManager.literal("del")
                    .then(ClientCommandManager.argument("alias", StringArgumentType.word())
                        .executes(ctx -> {
                            String alias = StringArgumentType.getString(ctx, "alias");
                            if (AliasManager.removeAlias(alias)) {
                                dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                                ctx.getSource().sendFeedback(Text.literal("§6Alias §f/" + alias + "§6 removed."));
                            } else {
                                ctx.getSource().sendFeedback(Text.literal("§cAlias §f/" + alias + "§c not found."));
                            }
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("reload")
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
                        ctx.getSource().sendFeedback(Text.literal("§6Aliases and functions reloaded."));
                        return 1;
                    })
                )
        );
    }

    // ---- /setcmdvariable ----

    private void registerSetCmdVariable(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("setcmdvariable")
                .then(ClientCommandManager.argument("variable", StringArgumentType.word())
                    .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String var = StringArgumentType.getString(ctx, "variable");
                            String value = StringArgumentType.getString(ctx, "value");
                            VariableManager.setVariable(MinecraftClient.getInstance(), var, value);
                            ctx.getSource().sendFeedback(Text.literal("§6Variable $" + var + " set to: " + value));
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /setcmdvar (shorthand) ----

    private void registerSetCmdVar(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("setcmdvar")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            String value = StringArgumentType.getString(ctx, "value");
                            VariableManager.setVariable(MinecraftClient.getInstance(), name, value);
                            ctx.getSource().sendFeedback(Text.literal("§6Variable $" + name + " set to: " + value));
                            return 1;
                        })
                    )
                )
        );
    }

    // ---- /deletealias / /delaliasmenu ----

    private void registerDeleteAliasMenu(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("deletealias")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(Text.literal("§cNo aliases to delete."));
                        return 0;
                    }
                    source.sendFeedback(Text.literal("§6Delete Aliases Menu:"));
                    int index = 1;
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        source.sendFeedback(Text.literal("  §f[" + index + "] §6/" + alias + " §7-> §f" + cmd));
                        index++;
                    }
                    source.sendFeedback(Text.literal("§7Use: §f/cmd del <alias>§7 to delete"));
                    return 1;
                })
                .then(ClientCommandManager.argument("alias", StringArgumentType.word())
                    .executes(ctx -> {
                        String alias = StringArgumentType.getString(ctx, "alias");
                        FabricClientCommandSource source = ctx.getSource();
                        if (AliasManager.removeAlias(alias)) {
                            dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
                            source.sendFeedback(Text.literal("§6[" + alias + "] was deleted"));
                        } else {
                            source.sendFeedback(Text.literal("§cAlias '§f" + alias + "§c' not found."));
                        }
                        return 1;
                    })
                )
        );
        // Also register /delaliasmenu as alias for /deletealias
        dispatcher.register(
            ClientCommandManager.literal("delaliasmenu")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Map<String, String> snapshot = AliasManager.getAliases();
                    if (snapshot.isEmpty()) {
                        source.sendFeedback(Text.literal("§cNo aliases to delete."));
                        return 0;
                    }
                    source.sendFeedback(Text.literal("§6Delete Aliases Menu:"));
                    int index = 1;
                    for (String alias : snapshot.keySet()) {
                        String cmd = snapshot.get(alias);
                        source.sendFeedback(Text.literal("  §f[" + index + "] §6/" + alias + " §7-> §f" + cmd));
                        index++;
                    }
                    source.sendFeedback(Text.literal("§7Use: §f/cmd del <alias>§7 to delete"));
                    return 1;
                })
        );
    }

    // ---- /syntax ----

    private void registerSyntaxCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("syntax")
                .then(ClientCommandManager.literal("list")
                    .executes(ctx -> {
                        StringBuilder sb = new StringBuilder("§6Available Syntaxes:\n");
                        for (Map.Entry<String, CommandSyntax> entry : SyntaxManager.getAllSyntaxes().entrySet()) {
                            sb.append("  §f").append(entry.getKey()).append("§7: §e")
                              .append(entry.getValue().getPattern()).append("\n");
                        }
                        ctx.getSource().sendFeedback(Text.literal(sb.toString()));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("help")
                    .then(ClientCommandManager.argument("syntaxName", StringArgumentType.word())
                        .executes(ctx -> {
                            String syntaxName = StringArgumentType.getString(ctx, "syntaxName");
                            CommandSyntax syntax = SyntaxManager.getSyntax(syntaxName);
                            if (syntax != null) {
                                ctx.getSource().sendFeedback(Text.literal(syntaxName + ": " + syntax.getPattern() + "\n" + syntax.getDescription()));
                            } else {
                                ctx.getSource().sendFeedback(Text.literal("§cSyntax '" + syntaxName + "' not found."));
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
            ClientCommandManager.literal("cmdcommand")
                .then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
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

    // ---- Alias registration ----

    private void registerAliases(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
            registerAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }

    private void registerAlias(CommandDispatcher<FabricClientCommandSource> dispatcher, String alias, String target) {
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals(alias));
        dispatcher.register(
            ClientCommandManager.literal(alias)
                .executes(ctx -> executeAlias(target, ctx))
                .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String args = StringArgumentType.getString(ctx, "args");
                        String full = alias + " " + args;
                        SyntaxManager.SyntaxMatch match = SyntaxManager.matchInput(full);
                        String command;
                        if (match != null) {
                            String substituted = match.syntax.substituteParameters(target, match.parameters);
                            command = VariableManager.substituteVariables(substituted, MinecraftClient.getInstance());
                        } else {
                            command = target + " " + args;
                            command = VariableManager.substituteVariables(command, MinecraftClient.getInstance());
                        }
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            if (!command.startsWith("/")) command = "/" + command;
                            client.player.networkHandler.sendChatMessage(command);
                        }
                        return 1;
                    })
                )
        );
    }

    private int executeAlias(String target, CommandContext<FabricClientCommandSource> ctx) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return 0;

            // Handle function calls
            if (target.startsWith("function:")) {
                String functionName = target.substring(9);
                FunctionManager.executeFunction(functionName, client);
                return 1;
            }

            // Replace variables
            String command = VariableManager.substituteVariables(target, client);

            // Ensure the command starts with /
            if (!command.startsWith("/")) {
                command = "/" + command;
            }

            // Execute the command on the server
            final String finalCommand = command;
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
}
