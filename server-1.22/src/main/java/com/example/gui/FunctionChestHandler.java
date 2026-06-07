package com.example.gui;

import com.example.FunctionManager;
import com.example.AliasManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.nio.file.*;
import java.util.*;

public class FunctionChestHandler extends AbstractContainerMenu {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int CONTAINER_SIZE = ROWS * COLS;

    private final SimpleContainer inventory;
    private final Player player;
    private int currentTab = 0;
    private int currentPage = 0;
    private Map<String, com.example.ManifestEntry> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();
    private String pendingDeleteAlias = null;

    public FunctionChestHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public FunctionChestHandler(int syncId, Inventory playerInventory, MenuType<?> type) {
        super(type != null ? type : ModScreens.FUNCTION_CHEST, syncId);
        this.player = playerInventory.player;
        this.inventory = new SimpleContainer(CONTAINER_SIZE);

        for (int i = 0; i < CONTAINER_SIZE; i++) {
            int row = i / COLS;
            int col = i % COLS;
            this.addSlot(new LockedSlot(inventory, i, 8 + col * 18, 18 + row * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        loadAndRefresh();
    }

    private void loadAndRefresh() {
        new Thread(() -> {
            manifest = FunctionManager.fetchFunctionManifest();
            localFunctions = FunctionManager.listLocalFunctions();
            refreshDisplay();
        }).start();
    }

    private void refreshDisplay() {
        inventory.clearContent();
        drawTabs();
        drawContent();
        drawNavigation();
    }

    private void drawTabs() {
        inventory.setItem(0, makeItem(currentTab == 0 ? Items.LIME_STAINED_GLASS_PANE : Items.GREEN_STAINED_GLASS_PANE, "§a§lDownload", "Browse and install from the online library"));
        inventory.setItem(1, makeItem(currentTab == 1 ? Items.LIGHT_BLUE_STAINED_GLASS_PANE : Items.BLUE_STAINED_GLASS_PANE, "§b§lMy Functions", "Your installed functions — left-click to run"));
        inventory.setItem(2, makeItem(currentTab == 2 ? Items.YELLOW_STAINED_GLASS_PANE : Items.ORANGE_STAINED_GLASS_PANE, "§e§lCreate New", "Create a new function from a template"));
        inventory.setItem(3, makeItem(currentTab == 3 ? Items.RED_STAINED_GLASS_PANE : Items.PINK_STAINED_GLASS_PANE, "§c§lDelete Aliases", "Remove command aliases — requires confirmation"));
        for (int i = 4; i < 7; i++) {
            inventory.setItem(i, makeItem(Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        if (currentTab == 3 && pendingDeleteAlias != null) {
            inventory.setItem(7, makeItem(Items.REDSTONE_BLOCK, "§c§l⚠ Confirm Delete",
                "§7Click to permanently delete: §c/" + pendingDeleteAlias,
                "§7This action cannot be undone!"));
        } else if (currentTab == 3) {
            inventory.setItem(7, makeItem(Items.BARRIER, "§7Confirm Delete", "§7Select an alias first, then confirm here"));
        } else {
            inventory.setItem(7, makeItem(Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        inventory.setItem(8, makeItem(Items.CLOCK, "§6§lRefresh", "Reload the function library and local files"));
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
                ItemStack stack = entry.toItemStack();
                if (currentTab == 3 && pendingDeleteAlias != null && entry.name.equals(pendingDeleteAlias)) {
                    ItemStack highlighted = new ItemStack(Items.RED_STAINED_GLASS_PANE);
                    highlighted.set(DataComponents.CUSTOM_NAME, Component.literal("§c§l⚠ DELETE: /" + entry.name));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.literal("§7Command: " + AliasManager.getAliases().get(entry.name)));
                    lore.add(Component.literal("§c§lClick slot 8 (redstone block) to confirm deletion"));
                    lore.add(Component.literal("§cThis action cannot be undone!"));
                    highlighted.set(DataComponents.LORE, new ItemLore(lore));
                    inventory.setItem(slotIdx, highlighted);
                } else {
                    inventory.setItem(slotIdx, stack);
                }
            }
        }
    }

    private List<FunctionEntry> getCurrentEntries() {
        List<FunctionEntry> entries = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, com.example.ManifestEntry> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        entries.add(new FunctionEntry(e.getKey(), EntryType.DOWNLOAD, e.getValue().description));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    com.example.ManifestEntry me = manifest.get(name);
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
        inventory.setItem(49, makeItem(Items.PAPER, "§6Page " + (currentPage + 1) + " / " + (maxPage + 1)));
        if (currentPage > 0) inventory.setItem(45, makeItem(Items.ARROW, "§a§l← Previous"));
        if (currentPage < maxPage) inventory.setItem(53, makeItem(Items.ARROW, "§a§lNext →"));
    }

    private ItemStack makeItem(net.minecraft.world.item.Item item, String name, String... loreLines) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        if (loreLines != null && loreLines.length > 0 && loreLines[0] != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                if (line != null) {
                    lore.add(Component.literal(line));
                }
            }
            if (!lore.isEmpty()) {
                stack.set(DataComponents.LORE, new ItemLore(lore));
            }
        }
        return stack;
    }

    private ItemStack makeItem(net.minecraft.world.item.Item item, String name) {
        return makeItem(item, name, (String) null);
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (input != ContainerInput.PICKUP) return;
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        if (!(this.slots.get(slotIndex) instanceof LockedSlot)) return;

        if (slotIndex == 0) { currentTab = 0; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 1) { currentTab = 1; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 2) { currentTab = 2; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 3) { currentTab = 3; currentPage = 0; pendingDeleteAlias = null; refreshDisplay(); return; }

        if (slotIndex == 7 && currentTab == 3) {
            if (pendingDeleteAlias != null) {
                String deleted = pendingDeleteAlias;
                AliasManager.removeAlias(pendingDeleteAlias);
                pendingDeleteAlias = null;
                refreshDisplay();
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.literal("§c✖ Deleted alias §f/" + deleted + "§c. Run §f/cmd reload§c to update commands."));
                }
            }
            return;
        }

        if (slotIndex == 8) { pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 45 && currentPage > 0) { currentPage--; refreshDisplay(); return; }
        if (slotIndex == 53) { currentPage++; refreshDisplay(); return; }

        if (slotIndex >= COLS && slotIndex < COLS * 5) {
            handleContentClick(slotIndex, button);
        }
    }

    private void resetAndRefresh() {
        manifest = FunctionManager.fetchFunctionManifest();
        localFunctions = FunctionManager.listLocalFunctions();
        refreshDisplay();
    }

    private void handleContentClick(int slotIndex, int button) {
        int contentSlots = COLS * 4;
        int entryIdx = currentPage * contentSlots + (slotIndex - COLS);
        List<FunctionEntry> entries = getCurrentEntries();
        if (entryIdx >= entries.size()) return;

        FunctionEntry entry = entries.get(entryIdx);

        if (player instanceof ServerPlayer sp) {
            switch (entry.type) {
                case DOWNLOAD -> {
                    FunctionManager.downloadFunction(entry.name, sp.createCommandSourceStack());
                    scheduleRefresh();
                }
                case LOCAL -> {
                        net.minecraft.commands.CommandSourceStack source = sp.createCommandSourceStack();
                        source.getServer().getCommands().performPrefixedCommand(
                            source, "/cmd function " + entry.name);
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
                        Path file = AliasManager.getFunctionsPath().resolve(newName + ".mcfunction");
                        Files.createDirectories(file.getParent());
                        Files.writeString(file, "# " + newName + "\n# Created with Command Maker\n");
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
    }

    private void scheduleRefresh() {
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            localFunctions = FunctionManager.listLocalFunctions();
            manifest = FunctionManager.fetchFunctionManifest();
            if (currentTab == 0) currentTab = 1;
            currentPage = 0;
            refreshDisplay();
        }).start();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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
            net.minecraft.world.item.Item item = switch (type) {
                case DOWNLOAD -> Items.PAPER;
                case LOCAL -> Items.BOOK;
                case DELETE_ALIAS -> Items.NAME_TAG;
                default -> Items.WRITABLE_BOOK;
            };
            String prefix = switch (type) {
                case DOWNLOAD -> "§a";
                case LOCAL -> "§b";
                case DELETE_ALIAS -> "§c";
                default -> "§e";
            };
            ItemStack stack = new ItemStack(item);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(prefix + name));

            if (description != null && !description.isEmpty()) {
                List<Component> lore = new ArrayList<>();
                if (type == EntryType.DELETE_ALIAS) {
                    lore.add(Component.literal("§7Command: " + description));
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("§c§l⚠ Click to select for deletion"));
                    lore.add(Component.literal("§7Then click the redstone block (slot 8) to confirm"));
                } else {
                    lore.add(Component.literal("§7" + description));
                    if (type == EntryType.DOWNLOAD) {
                        lore.add(Component.literal(""));
                        lore.add(Component.literal("§aClick to download & install"));
                    } else if (type == EntryType.LOCAL) {
                        lore.add(Component.literal(""));
                        lore.add(Component.literal("§eLeft-click: Run function"));
                    }
                }
                stack.set(DataComponents.LORE, new ItemLore(lore));
            }
            return stack;
        }
    }

    private static class LockedSlot extends Slot {
        LockedSlot(SimpleContainer inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
