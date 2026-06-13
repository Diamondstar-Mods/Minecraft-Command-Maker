package com.example;

import com.example.gui.FunctionGui;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

public class CommandMakerPlugin extends JavaPlugin {

    private CommandMap commandMap;
    private final Map<String, AliasCommand> registeredAliasCommands = new HashMap<>();

    @Override
    public void onEnable() {
        AliasManager.init(this);
        SyntaxManager.init(this);
        PermissionManager.init(this);
        UpdateChecker.check(this);

        getCommand("cmd").setExecutor(new CmdExecutor());
        getCommand("cmd").setTabCompleter(new CmdTabCompleter());
        getCommand("addcommand").setExecutor(new AddCommandExecutor());
        getCommand("setcmdvariable").setExecutor(new SetCmdVariableExecutor());
        getCommand("syntax").setExecutor(new SyntaxExecutor());
        getCommand("deletealias").setExecutor(new DeleteAliasExecutor());
        getCommand("cmmakerperm").setExecutor(new CmMakerPermExecutor());

        getServer().getPluginManager().registerEvents(new FunctionGui(null) {
            @Override public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {}
        }, this);

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            getLogger().severe("Failed to get CommandMap: " + e.getMessage());
        }

        registerAliasCommands();

        getLogger().info("Nek's Command Maker (Paper) initialized!");
    }

    @Override
    public void onDisable() {
        unregisterAliasCommands();
        getLogger().info("Command Maker disabled.");
    }

    void registerAliasCommands() {
        Map<String, String> aliases = AliasManager.getAliases();
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            registerAliasCommand(entry.getKey(), entry.getValue());
        }
    }

    void registerAliasCommand(String name, String target) {
        if (commandMap == null) return;
        if (registeredAliasCommands.containsKey(name)) return;

        AliasCommand cmd = new AliasCommand(name, target);
        commandMap.register("cmdmaker", cmd);
        registeredAliasCommands.put(name, cmd);
    }

    void unregisterAliasCommands() {
        if (commandMap == null) return;
        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.values().removeIf(cmd -> cmd instanceof AliasCommand);
        } catch (Exception e) {
            getLogger().warning("Failed to unregister alias commands: " + e.getMessage());
        }
        registeredAliasCommands.clear();
    }

    void reloadAll() {
        unregisterAliasCommands();
        AliasManager.loadAliases();
        SyntaxManager.loadSyntaxDefinitions();
        registerAliasCommands();
    }

    // ── /cmd executor ──

    private class CmdExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            UpdateChecker.sendUpdateMessage(sender);

            if (args.length == 0) {
                sender.sendMessage("§6⚡ Command Maker §7— Use §e/cmd help §7for a list of commands.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (!PermissionManager.canManageAliases(sender)) {
                        sender.sendMessage("§cYou don't have permission to manage aliases.");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage("§cUsage: /cmd add <alias> <command>");
                        return true;
                    }
                    String alias = args[1];
                    String command = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (AliasManager.hasAlias(alias)) {
                        sender.sendMessage("§cAlias §f/" + alias + "§c already exists!");
                        return true;
                    }
                    AliasManager.addAlias(alias, command);
                    registerAliasCommand(alias, command);
                    sender.sendMessage("§a✔ Added alias §f/" + alias + " §a→ §7" + command);
                }
                case "del" -> {
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /cmd del <alias>");
                        return true;
                    }
                    String alias = args[1];
                    if (!AliasManager.hasAlias(alias)) {
                        sender.sendMessage("§cAlias §f/" + alias + "§c not found!");
                        return true;
                    }
                    AliasManager.removeAlias(alias);
                    if (registeredAliasCommands.containsKey(alias)) {
                        try {
                            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                            knownCommandsField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                            knownCommands.remove(alias);
                            knownCommands.remove("cmdmaker:" + alias);
                        } catch (Exception ignored) {}
                        registeredAliasCommands.remove(alias);
                    }
                    sender.sendMessage("§c✖ Deleted alias §f/" + alias);
                }
                case "reload" -> {
                    if (!PermissionManager.canManageAliases(sender)) {
                        sender.sendMessage("§cYou don't have permission to manage aliases.");
                        return true;
                    }
                    reloadAll();
                    sender.sendMessage("§a✔ Reloaded aliases, functions, and syntax definitions.");
                }
                case "list" -> {
                    sender.sendMessage("§6Aliases:");
                    for (Map.Entry<String, String> entry : AliasManager.getAliases().entrySet()) {
                        sender.sendMessage("  §e/" + entry.getKey() + " §7→ " + entry.getValue());
                    }
                    sender.sendMessage("§6Functions:");
                    for (String name : FunctionManager.listLocalFunctions()) {
                        sender.sendMessage("  §b" + name);
                    }
                }
                case "gui" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§cThis command can only be used by a player.");
                        return true;
                    }
                    player.openInventory(new FunctionGui(player).getInventory());
                }
                case "help" -> {
                    sender.sendMessage("§6§l⚡ Command Maker Help ⚡");
                    sender.sendMessage("§e/cmd add <alias> <command> §7— Create a new command alias");
                    sender.sendMessage("§e/cmd del <alias> §7— Delete an alias");
                    sender.sendMessage("§e/cmd reload §7— Reload aliases and syntax definitions from config files");
                    sender.sendMessage("§e/cmd list §7— List all aliases and functions");
                    sender.sendMessage("§e/cmd function <name> §7— Run a .mcfunction file");
                    sender.sendMessage("§e/cmd function create <name> §7— Create a new .mcfunction file");
                    sender.sendMessage("§e/cmd function delete <name> §7— Delete a .mcfunction file");
                    sender.sendMessage("§e/cmd gui §7— Open the Function Manager GUI");
                    sender.sendMessage("§e/cmd downloadfunction <name> §7— Download a function from the online library");
                    sender.sendMessage("§e/cmd listdownloadablefunctions §7— List all downloadable functions");
                    sender.sendMessage("§e/cmd syntax §7— List custom syntax patterns");
                    sender.sendMessage("§e/cmd help §7— Show this help message");
                    sender.sendMessage("§7Use §e/cmd help §7anytime to see this list!");
                }
                case "function" -> {
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /cmd function <name|create|delete> [args]");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("create")) {
                        if (args.length < 3) {
                            sender.sendMessage("§cUsage: /cmd function create <name>");
                            return true;
                        }
                        String name = args[2];
                        try {
                            File file = new File(AliasManager.getFunctionsDir(), name + ".mcfunction");
                            Files.createDirectories(file.getParentFile().toPath());
                            if (!Files.exists(file.toPath())) {
                                Files.writeString(file.toPath(), "# " + name + "\n# Created with Command Maker\n");
                                sender.sendMessage("§a✔ Created function §f" + name);
                            } else {
                                sender.sendMessage("§cFunction §f" + name + "§c already exists.");
                            }
                        } catch (Exception e) {
                            sender.sendMessage("§cFailed to create function: " + e.getMessage());
                        }
                    } else if (args[1].equalsIgnoreCase("delete")) {
                        if (args.length < 3) {
                            sender.sendMessage("§cUsage: /cmd function delete <name>");
                            return true;
                        }
                        String name = args[2];
                        File file = new File(AliasManager.getFunctionsDir(), name + ".mcfunction");
                        if (file.exists()) {
                            file.delete();
                            sender.sendMessage("§c✖ Deleted function §f" + name);
                        } else {
                            sender.sendMessage("§cFunction §f" + name + "§c not found.");
                        }
                    } else {
                        String name = args[1];
                        FunctionManager.executeFunction(name, sender);
                    }
                }
                case "downloadfunction" -> {
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /cmd downloadfunction <name>");
                        return true;
                    }
                    FunctionManager.downloadFunction(args[1], sender);
                }
                case "listdownloadablefunctions" -> FunctionManager.listDownloadableFunctions(sender);
                case "setvar" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§cOnly players can set variables.");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage("§cUsage: /cmd setvar <key> <value>");
                        return true;
                    }
                    String key = args[1];
                    String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    VariableManager.setVariable(player, key, value);
                    sender.sendMessage("§aSet variable §f${" + key + "} §a= §f" + value);
                }
                case "syntax" -> {
                    Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
                    if (syntaxes.isEmpty()) {
                        sender.sendMessage("§cNo custom syntaxes defined.");
                    } else {
                        sender.sendMessage("§6§lCustom Syntax Patterns:");
                        for (CommandSyntax syntax : syntaxes.values()) {
                            String desc = syntax.getDescription();
                            sender.sendMessage("§e  " + syntax.getPattern() + " §7— " + (desc != null ? desc : "No description"));
                        }
                    }
                }
                default -> sender.sendMessage("§cUnknown subcommand. Use §e/cmd help §cfor a list.");
            }
            return true;
        }
    }

    // ── Tab completer for /cmd ──

    private class CmdTabCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                List<String> subs = new ArrayList<>(Arrays.asList(
                    "add", "del", "reload", "list", "gui", "help",
                    "function", "downloadfunction", "listdownloadablefunctions",
                    "setvar", "syntax"
                ));
                subs.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
                return subs;
            }
            if (args.length == 2) {
                switch (args[0].toLowerCase()) {
                    case "del" -> {
                        List<String> names = new ArrayList<>(AliasManager.getAliases().keySet());
                        names.removeIf(n -> !n.startsWith(args[1].toLowerCase()));
                        return names;
                    }
                    case "function" -> {
                        List<String> subs = new ArrayList<>(Arrays.asList("create", "delete"));
                        subs.addAll(FunctionManager.listLocalFunctions());
                        subs.removeIf(s -> !s.startsWith(args[1].toLowerCase()));
                        return subs;
                    }
                    case "downloadfunction" -> {
                        List<String> names = FunctionManager.fetchDownloadableFunctionNames();
                        names.removeIf(n -> !n.startsWith(args[1].toLowerCase()));
                        return names;
                    }
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("function") && args[1].equalsIgnoreCase("delete")) {
                List<String> names = FunctionManager.listLocalFunctions();
                names.removeIf(n -> !n.startsWith(args[2].toLowerCase()));
                return names;
            }
            return List.of();
        }
    }

    // ── /addcommand executor ──

    private class AddCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!sender.isOp()) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /addcommand <alias> <command>");
                return true;
            }
            String alias = args[0];
            String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (AliasManager.hasAlias(alias)) {
                sender.sendMessage("§cAlias already exists!");
                return true;
            }
            AliasManager.addAlias(alias, command);
            registerAliasCommand(alias, command);
            sender.sendMessage("§a✔ Added alias §f/" + alias);
            return true;
        }
    }

    // ── /setcmdvariable executor ──

    private class SetCmdVariableExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can set variables.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /setcmdvariable <key> <value>");
                return true;
            }
            String key = args[0];
            String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            VariableManager.setVariable(player, key, value);
            sender.sendMessage("§aSet variable §f${" + key + "} §a= §f" + value);
            return true;
        }
    }

    // ── /syntax executor ──

    private class SyntaxExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            UpdateChecker.sendUpdateMessage(sender);
            Map<String, CommandSyntax> syntaxes = SyntaxManager.getAllSyntaxes();
            if (syntaxes.isEmpty()) {
                sender.sendMessage("§cNo custom syntaxes defined.");
            } else {
                sender.sendMessage("§6§lCustom Syntax Patterns:");
                for (CommandSyntax syntax : syntaxes.values()) {
                    String desc = syntax.getDescription();
                    sender.sendMessage("§e  " + syntax.getPattern() + " §7— " + (desc != null ? desc : "No description"));
                }
            }
            return true;
        }
    }

    // ── /deletealias executor ──

    private class DeleteAliasExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            UpdateChecker.sendUpdateMessage(sender);
            Map<String, String> snapshot = AliasManager.getAliases();
            if (snapshot.isEmpty()) {
                sender.sendMessage("§cNo aliases to delete.");
                return true;
            }
            sender.sendMessage("§6§lAliases:");
            int i = 1;
            for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                sender.sendMessage("§e" + i + ". §f/" + entry.getKey() + " §7→ " + entry.getValue());
                i++;
            }
            sender.sendMessage("§7Use §e/cmd del <alias> §7to delete, or open §e/cmd gui §7for the GUI");
            return true;
        }
    }

    // ── /cmmakerperm executor ──

    private class CmMakerPermExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!sender.isOp() && !sender.hasPermission("cmdmaker.admin")) {
                sender.sendMessage("§cYou don't have permission to manage Command Maker permissions.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /cmmakerperm reload");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                PermissionManager.init(CommandMakerPlugin.this);
                sender.sendMessage("§a✔ Permissions reloaded.");
            } else {
                sender.sendMessage("§cUsage: /cmmakerperm reload");
            }
            return true;
        }
    }

    // ── Dynamic alias command ──

    private static class AliasCommand extends Command {
        private final String target;

        AliasCommand(String name, String target) {
            super(name);
            this.target = target;
            this.description = "Command Maker alias";
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            Player player = sender instanceof Player ? (Player) sender : null;
            String substituted = VariableManager.substituteVariables(target, player);

            String fullInput = "/" + label;
            if (args.length > 0) fullInput += " " + String.join(" ", args);

            SyntaxManager.SyntaxMatch match = SyntaxManager.matchInput(fullInput);
            if (match != null) {
                substituted = match.syntax.substituteParameters(substituted, match.parameters);
            }

            Bukkit.dispatchCommand(sender, substituted);
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return List.of();
        }
    }
}
