package com.example.gui;

import com.example.AliasManager;
import com.example.FunctionManager;
import com.example.ManifestEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class FunctionGui implements InventoryHolder, Listener {
    private static final int ROWS = 6;
    private static final int COLS = 9;
    private static final int CONTAINER_SIZE = ROWS * COLS;

    private Inventory inventory;
    private final Player player;
    private int currentTab = 0;
    private int currentPage = 0;
    private Map<String, ManifestEntry> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();
    private String pendingDeleteAlias = null;

    public FunctionGui(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, CONTAINER_SIZE, "§8Command Maker - Functions");
        loadAndRefresh();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void loadAndRefresh() {
        new Thread(() -> {
            manifest = FunctionManager.fetchFunctionManifest();
            localFunctions = FunctionManager.listLocalFunctions();
            Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager().getPlugin("CommandMaker"),
                this::refreshDisplay
            );
        }).start();
    }

    private void refreshDisplay() {
        if (inventory == null) return;
        inventory.clear();
        drawTabs();
        drawContent();
        drawNavigation();
    }

    private void drawTabs() {
        inventory.setItem(0, makeItem(currentTab == 0 ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE,
            "§a§lDownload", "Browse and install from the online library"));
        inventory.setItem(1, makeItem(currentTab == 1 ? Material.LIGHT_BLUE_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE,
            "§b§lMy Functions", "Your installed functions — left-click to run"));
        inventory.setItem(2, makeItem(currentTab == 2 ? Material.YELLOW_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE,
            "§e§lCreate New", "Create a new function from a template"));
        inventory.setItem(3, makeItem(currentTab == 3 ? Material.RED_STAINED_GLASS_PANE : Material.PINK_STAINED_GLASS_PANE,
            "§c§lDelete Aliases", "Remove command aliases — requires confirmation"));
        for (int i = 4; i < 7; i++) {
            inventory.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        if (currentTab == 3 && pendingDeleteAlias != null) {
            inventory.setItem(7, makeItem(Material.REDSTONE_BLOCK, "§c§l⚠ Confirm Delete",
                "§7Click to permanently delete: §c/" + pendingDeleteAlias,
                "§7This action cannot be undone!"));
        } else if (currentTab == 3) {
            inventory.setItem(7, makeItem(Material.BARRIER, "§7Confirm Delete", "§7Select an alias first, then confirm here"));
        } else {
            inventory.setItem(7, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        inventory.setItem(8, makeItem(Material.CLOCK, "§6§lRefresh", "Reload the function library and local files"));
    }

    private void drawContent() {
        int slotStart = COLS;
        int contentSlots = COLS * 4;
        List<FunctionEntry> entries = getCurrentEntries();
        int startIdx = currentPage * contentSlots;

        for (int i = 0; i < contentSlots; i++) {
            int slotIdx = slotStart + i;
            int entryIdx = startIdx + i;
            if (entryIdx < entries.size()) {
                FunctionEntry entry = entries.get(entryIdx);
                ItemStack stack;
                if (currentTab == 3 && pendingDeleteAlias != null && entry.name.equals(pendingDeleteAlias)) {
                    stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName("§c§l⚠ DELETE: /" + entry.name);
                    meta.setLore(Arrays.asList(
                        "§7Command: " + AliasManager.getAliases().get(entry.name),
                        "§c§lClick slot 8 (redstone block) to confirm deletion",
                        "§cThis action cannot be undone!"
                    ));
                    stack.setItemMeta(meta);
                } else {
                    stack = entry.toItemStack();
                }
                inventory.setItem(slotIdx, stack);
            }
        }
    }

    private List<FunctionEntry> getCurrentEntries() {
        List<FunctionEntry> entries = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, ManifestEntry> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        entries.add(new FunctionEntry(e.getKey(), EntryType.DOWNLOAD, e.getValue().description));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    ManifestEntry me = manifest.get(name);
                    String desc = me != null ? me.description : "Local function — left-click to run";
                    entries.add(new FunctionEntry(name, EntryType.LOCAL, desc));
                }
            }
            case 2 -> {
                entries.add(new FunctionEntry("Empty Function", EntryType.CREATE_EMPTY, "Create a blank .mcfunction file to write yourself"));
                entries.add(new FunctionEntry("Command Template", EntryType.CREATE_CMD, "Pre-filled with common command examples to customize"));
                entries.add(new FunctionEntry("Mob Spawner", EntryType.CREATE_MOB, "Template with mob summoning and effect commands"));
                entries.add(new FunctionEntry("Building", EntryType.CREATE_BUILD, "Template with fill/setblock building commands"));
            }
            case 3 -> {
                Map<String, String> aliases = AliasManager.getAliases();
                List<String> sorted = new ArrayList<>(aliases.keySet());
                Collections.sort(sorted);
                for (String name : sorted) {
                    entries.add(new FunctionEntry(name, EntryType.DELETE_ALIAS, aliases.get(name)));
                }
            }
        }
        return entries;
    }

    private void drawNavigation() {
        int total = getCurrentEntries().size();
        int contentSlots = COLS * 4;
        int maxPage = Math.max(0, (total - 1) / contentSlots);
        inventory.setItem(49, makeItem(Material.PAPER, "§6Page " + (currentPage + 1) + " / " + (maxPage + 1)));
        if (currentPage > 0) inventory.setItem(45, makeItem(Material.ARROW, "§a§l← Previous"));
        if (currentPage < maxPage) inventory.setItem(53, makeItem(Material.ARROW, "§a§lNext →"));
    }

    private ItemStack makeItem(Material material, String name, String... loreLines) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null && loreLines.length > 0) {
                meta.setLore(Arrays.asList(loreLines));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof FunctionGui gui)) return;
        if (event.getWhoClicked() != gui.player) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= CONTAINER_SIZE) return;

        if (slot == 0) { gui.currentTab = 0; gui.currentPage = 0; gui.pendingDeleteAlias = null; gui.resetAndRefresh(); return; }
        if (slot == 1) { gui.currentTab = 1; gui.currentPage = 0; gui.pendingDeleteAlias = null; gui.resetAndRefresh(); return; }
        if (slot == 2) { gui.currentTab = 2; gui.currentPage = 0; gui.pendingDeleteAlias = null; gui.resetAndRefresh(); return; }
        if (slot == 3) { gui.currentTab = 3; gui.currentPage = 0; gui.pendingDeleteAlias = null; gui.refreshDisplay(); return; }

        if (slot == 7 && gui.currentTab == 3) {
            if (gui.pendingDeleteAlias != null) {
                String deleted = gui.pendingDeleteAlias;
                AliasManager.removeAlias(deleted);
                gui.pendingDeleteAlias = null;
                gui.refreshDisplay();
                player.sendMessage("§c✖ Deleted alias §f/" + deleted + "§c. Run §f/cmd reload§c to update commands.");
            }
            return;
        }

        if (slot == 8) { gui.pendingDeleteAlias = null; gui.resetAndRefresh(); return; }
        if (slot == 45 && gui.currentPage > 0) { gui.currentPage--; gui.refreshDisplay(); return; }
        if (slot == 53) { gui.currentPage++; gui.refreshDisplay(); return; }

        if (slot >= COLS && slot < COLS * 5) {
            gui.handleContentClick(slot);
        }
    }

    private void resetAndRefresh() {
        manifest = FunctionManager.fetchFunctionManifest();
        localFunctions = FunctionManager.listLocalFunctions();
        refreshDisplay();
    }

    private void handleContentClick(int slot) {
        int contentSlots = COLS * 4;
        int entryIdx = currentPage * contentSlots + (slot - COLS);
        List<FunctionEntry> entries = getCurrentEntries();
        if (entryIdx >= entries.size()) return;

        FunctionEntry entry = entries.get(entryIdx);

        switch (entry.type) {
            case DOWNLOAD -> {
                FunctionManager.downloadFunction(entry.name, player);
                scheduleRefresh();
            }
            case LOCAL -> {
                player.closeInventory();
                Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("CommandMaker"),
                    () -> Bukkit.dispatchCommand(player, "cmd function " + entry.name)
                );
            }
            case CREATE_EMPTY, CREATE_CMD, CREATE_MOB, CREATE_BUILD -> {
                String prefix = switch (entry.type) {
                    case CREATE_CMD -> "cmd_";
                    case CREATE_MOB -> "mob_";
                    case CREATE_BUILD -> "build_";
                    default -> "empty_";
                };
                String newName = prefix + System.currentTimeMillis() % 100000;
                try {
                    File file = new File(AliasManager.getFunctionsDir(), newName + ".mcfunction");
                    Files.createDirectories(file.getParentFile().toPath());
                    Files.writeString(file.toPath(), "# " + newName + "\n# Created with Command Maker\n");
                } catch (Exception ignored) {}
                scheduleRefresh();
            }
            case DELETE_ALIAS -> {
                if (entry.name.equals(pendingDeleteAlias)) {
                    pendingDeleteAlias = null;
                } else {
                    pendingDeleteAlias = entry.name;
                }
                refreshDisplay();
            }
        }
    }

    private void scheduleRefresh() {
        Bukkit.getScheduler().runTaskLater(
            Bukkit.getPluginManager().getPlugin("CommandMaker"),
            () -> {
                localFunctions = FunctionManager.listLocalFunctions();
                manifest = FunctionManager.fetchFunctionManifest();
                if (currentTab == 0) currentTab = 1;
                currentPage = 0;
                refreshDisplay();
            },
            20L
        );
    }

    private enum EntryType {
        DOWNLOAD, LOCAL, CREATE_EMPTY, CREATE_CMD, CREATE_MOB, CREATE_BUILD, DELETE_ALIAS
    }

    private static class FunctionEntry {
        final String name;
        final EntryType type;
        final String description;

        FunctionEntry(String name, EntryType type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        ItemStack toItemStack() {
            Material material = switch (type) {
                case DOWNLOAD -> Material.PAPER;
                case LOCAL -> Material.BOOK;
                case DELETE_ALIAS -> Material.NAME_TAG;
                default -> Material.WRITABLE_BOOK;
            };
            String prefix = switch (type) {
                case DOWNLOAD -> "§a";
                case LOCAL -> "§b";
                case DELETE_ALIAS -> "§c";
                default -> "§e";
            };
            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(prefix + name);
                List<String> lore = new ArrayList<>();
                if (description != null && !description.isEmpty()) {
                    if (type == EntryType.DELETE_ALIAS) {
                        lore.add("§7Command: " + description);
                        lore.add("");
                        lore.add("§c§l⚠ Click to select for deletion");
                        lore.add("§7Then click the redstone block (slot 8) to confirm");
                    } else {
                        lore.add("§7" + description);
                        if (type == EntryType.DOWNLOAD) {
                            lore.add("");
                            lore.add("§aClick to download & install");
                        } else if (type == EntryType.LOCAL) {
                            lore.add("");
                            lore.add("§eLeft-click: Run function");
                        }
                    }
                }
                meta.setLore(lore);
                stack.setItemMeta(meta);
            }
            return stack;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Cleanup handled by GC
    }
}
