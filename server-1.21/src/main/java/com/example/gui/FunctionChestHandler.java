package com.example.gui;

import com.example.FunctionManager;
import com.example.AliasManager;
import com.example.ManifestEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.nio.file.*;
import java.util.*;

public class FunctionChestHandler extends ScreenHandler {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int CONTAINER_SIZE = ROWS * COLS;

    private final SimpleInventory inventory;
    private final PlayerEntity player;
    private int currentTab = 0;
    private int currentPage = 0;
    private Map<String, ManifestEntry> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();
    private String pendingDeleteAlias = null;

    public FunctionChestHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        this.player = playerInventory.player;
        this.inventory = new SimpleInventory(CONTAINER_SIZE);

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
        inventory.clear();
        drawTabs();
        drawContent();
        drawNavigation();
    }

    private void drawTabs() {
        inventory.setStack(0, makeItem(currentTab == 0 ? Items.LIME_STAINED_GLASS_PANE : Items.GREEN_STAINED_GLASS_PANE, "§a§lDownload", "Browse and install from the online library"));
        inventory.setStack(1, makeItem(currentTab == 1 ? Items.LIGHT_BLUE_STAINED_GLASS_PANE : Items.BLUE_STAINED_GLASS_PANE, "§b§lMy Functions", "Your installed functions — left-click to run"));
        inventory.setStack(2, makeItem(currentTab == 2 ? Items.YELLOW_STAINED_GLASS_PANE : Items.ORANGE_STAINED_GLASS_PANE, "§e§lCreate New", "Create a new function from a template"));
        inventory.setStack(3, makeItem(currentTab == 3 ? Items.RED_STAINED_GLASS_PANE : Items.PINK_STAINED_GLASS_PANE, "§c§lDelete Aliases", "Remove command aliases — requires confirmation"));
        for (int i = 4; i < 7; i++) {
            inventory.setStack(i, makeItem(Items.BLACK_STAINED_GLASS_PANE, " ", (String[]) null));
        }
        // Slot 7: Confirm delete button (visible in delete aliases tab)
        if (currentTab == 3 && pendingDeleteAlias != null) {
            inventory.setStack(7, makeItem(Items.REDSTONE_BLOCK, "§c§l⚠ Confirm Delete",
                "§7Click to permanently delete: §c/" + pendingDeleteAlias,
                "§7This action cannot be undone!"));
        } else if (currentTab == 3) {
            inventory.setStack(7, makeItem(Items.BARRIER, "§7Confirm Delete", "§7Select an alias first, then confirm here"));
        } else {
            inventory.setStack(7, makeItem(Items.BLACK_STAINED_GLASS_PANE, " ", (String[]) null));
        }
        inventory.setStack(8, makeItem(Items.CLOCK, "§6§lRefresh", "Runs /cmd reload — reloads all aliases,", "functions, and config files from disk."));
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
                // Highlight pending delete alias
                if (currentTab == 3 && pendingDeleteAlias != null && entry.name.equals(pendingDeleteAlias)) {
                    ItemStack highlighted = new ItemStack(Items.RED_STAINED_GLASS_PANE);
                    highlighted.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§c§l⚠ DELETE: /" + entry.name));
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.literal("§7Command: " + AliasManager.getAliases().get(entry.name)));
                    lore.add(Text.literal("§c§lClick slot 7 (redstone block) to confirm deletion"));
                    lore.add(Text.literal("§cThis action cannot be undone!"));
                    highlighted.set(DataComponentTypes.LORE, new LoreComponent(lore));
                    inventory.setStack(slotIdx, highlighted);
                } else {
                    inventory.setStack(slotIdx, stack);
                }
            }
        }
    }

    private List<FunctionEntry> getCurrentEntries() {
        List<FunctionEntry> entries = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, ManifestEntry> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        ManifestEntry me = e.getValue();
                        entries.add(new FunctionEntry(e.getKey(), EntryType.DOWNLOAD, me.description, me.icon));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    ManifestEntry me = manifest.get(name);
                    String desc = me != null ? me.description : "Local function — left-click to run";
                    String icon = me != null ? me.icon : null;
                    entries.add(new FunctionEntry(name, EntryType.LOCAL, desc, icon));
                }
            }
            case 2 -> {
                entries.add(new FunctionEntry("Empty Function", EntryType.CREATE_EMPTY, "Create a blank .mcfunction file to write yourself", null));
                entries.add(new FunctionEntry("Command Template", EntryType.CREATE_CMD, "Pre-filled with common command examples to customize", null));
                entries.add(new FunctionEntry("Mob Spawner", EntryType.CREATE_MOB, "Template with mob summoning and effect commands", null));
                entries.add(new FunctionEntry("Building", EntryType.CREATE_BUILD, "Template with fill/setblock building commands", null));
            }
            case 3 -> {
                Map<String, String> aliases = AliasManager.getAliases();
                List<String> sorted = new ArrayList<>(aliases.keySet());
                Collections.sort(sorted);
                for (String name : sorted) {
                    entries.add(new FunctionEntry(name, EntryType.DELETE_ALIAS, aliases.get(name), null));
                }
            }
        }
        return entries;
    }

    private void drawNavigation() {
        int total = getCurrentEntries().size();
        int contentSlots = COLS * 4;
        int maxPage = Math.max(0, (total - 1) / contentSlots);
        inventory.setStack(49, makeItem(Items.PAPER, "§6Page " + (currentPage + 1) + " / " + (maxPage + 1), (String[]) null));
        if (currentPage > 0) inventory.setStack(45, makeItem(Items.ARROW, "§a§l← Previous", (String[]) null));
        if (currentPage < maxPage) inventory.setStack(53, makeItem(Items.ARROW, "§a§lNext →", (String[]) null));
    }

    private ItemStack makeItem(net.minecraft.item.Item item, String name, String... loreLines) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        if (loreLines != null && loreLines.length > 0 && loreLines[0] != null) {
            List<Text> lore = new ArrayList<>();
            for (String line : loreLines) {
                if (line != null) {
                    lore.add(Text.literal(line));
                }
            }
            if (!lore.isEmpty()) {
                stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
            }
        }
        return stack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType != SlotActionType.PICKUP) return;
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        if (!(this.slots.get(slotIndex) instanceof LockedSlot)) return;

        // Tab switching — clear pending delete
        if (slotIndex == 0) { currentTab = 0; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 1) { currentTab = 1; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 2) { currentTab = 2; currentPage = 0; pendingDeleteAlias = null; resetAndRefresh(); return; }
        if (slotIndex == 3) { currentTab = 3; currentPage = 0; pendingDeleteAlias = null; refreshDisplay(); return; }

        if (slotIndex == 7 && currentTab == 3) {
            // Confirm delete button
            if (pendingDeleteAlias != null) {
                String deleted = pendingDeleteAlias;
                AliasManager.removeAlias(deleted);
                pendingDeleteAlias = null;
                refreshDisplay();
                // Send feedback to the player
                if (player instanceof ServerPlayerEntity sp) {
                    sp.sendMessage(Text.literal("§c✖ Deleted alias §f/" + deleted + "§c. Run §f/cmd reload§c to update commands."), false);
                }
            }
            return;
        }

        if (slotIndex == 8) {
            pendingDeleteAlias = null;
            if (player instanceof ServerPlayerEntity sp) {
                var source = sp.getCommandSource();
                var dispatcher = source.getServer().getCommandManager().getDispatcher();
                try {
                    var parsed = dispatcher.parse("/cmd reload", source);
                    dispatcher.execute(parsed);
                } catch (Exception ignored) {}
                sp.sendMessage(Text.literal("§a✔ Reloaded aliases, functions, and configs."), false);
            }
            resetAndRefresh();
            return;
        }
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

        if (player instanceof ServerPlayerEntity sp) {
            switch (entry.type) {
                case DOWNLOAD -> {
                    FunctionManager.downloadFunction(entry.name, sp.getCommandSource());
                    scheduleRefresh();
                }
                case LOCAL -> {
                        net.minecraft.server.command.ServerCommandSource source = sp.getCommandSource();
                        var dispatcher = source.getServer().getCommandManager().getDispatcher();
                        try {
                            var parsed = dispatcher.parse("/cmd function " + entry.name, source);
                            dispatcher.execute(parsed);
                        } catch (Exception ignored) {}
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
                        // Clicking again deselects
                        pendingDeleteAlias = null;
                    } else {
                        // Select for deletion — confirm via slot 7
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
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private enum EntryType {
        DOWNLOAD, LOCAL, CREATE_EMPTY, CREATE_CMD, CREATE_MOB, CREATE_BUILD, DELETE_ALIAS
    }

    private static class FunctionEntry {
        final String name;
        final EntryType type;
        final String description;
        final String iconId; // e.g. "minecraft:diamond_shovel", may be null

        FunctionEntry(String name, EntryType type, String description) {
            this(name, type, description, null);
        }

        FunctionEntry(String name, EntryType type, String description, String iconId) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.iconId = iconId;
        }

        private net.minecraft.item.Item resolveIcon() {
            if (iconId != null && !iconId.isEmpty()) {
                // Parse "minecraft:diamond_shovel"
                String id = iconId.contains(":") ? iconId.substring(iconId.indexOf(':') + 1) : iconId;
                net.minecraft.item.Item resolved = net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of("minecraft", id));
                if (resolved != Items.AIR) {
                    // Verify icon file exists in CDN icons folder
                    Path iconFile = Paths.get("docs", "cdn", "icons", id + ".png");
                    if (Files.exists(iconFile)) {
                        return resolved;
                    }
                    // Icon file missing from CDN — skip this icon, fall back to default
                }
            }
            // Fallback to type-based defaults
            return switch (type) {
                case DOWNLOAD -> Items.PAPER;
                case LOCAL -> Items.BOOK;
                case DELETE_ALIAS -> Items.NAME_TAG;
                default -> Items.WRITABLE_BOOK;
            };
        }

        ItemStack toItemStack() {
            net.minecraft.item.Item item = resolveIcon();
            String prefix = switch (type) {
                case DOWNLOAD -> "§a";
                case LOCAL -> "§b";
                case DELETE_ALIAS -> "§c";
                default -> "§e";
            };
            ItemStack stack = new ItemStack(item);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(prefix + name));

            // Add description as tooltip lore — shows on hover in-game
            if (description != null && !description.isEmpty()) {
                List<Text> lore = new ArrayList<>();
                // Word-wrap the description into ~40-char lines for readability
                String desc = description;
                if (type == EntryType.DELETE_ALIAS) {
                    lore.add(Text.literal("§7Command: " + desc));
                    lore.add(Text.literal(""));
                    lore.add(Text.literal("§c§l⚠ Click to select for deletion"));
                    lore.add(Text.literal("§7Then click the redstone block (slot 8) to confirm"));
                } else {
                    lore.add(Text.literal("§7" + desc));
                    if (type == EntryType.DOWNLOAD) {
                        lore.add(Text.literal(""));
                        lore.add(Text.literal("§aClick to download & install"));
                    } else if (type == EntryType.LOCAL) {
                        lore.add(Text.literal(""));
                        lore.add(Text.literal("§eLeft-click: Run function"));
                    }
                }
                stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
            }
            return stack;
        }
    }

    private static class LockedSlot extends Slot {
        LockedSlot(SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
