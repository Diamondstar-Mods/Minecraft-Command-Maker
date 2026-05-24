package com.example.gui;

import com.example.FunctionManager;
import com.example.AliasManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.nio.file.*;
import java.util.*;

public class FunctionChestHandler extends ScreenHandler {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int CONTAINER_SIZE = ROWS * COLS; // 54

    private final SimpleInventory inventory;
    private final PlayerEntity player;
    private int currentTab = 0; // 0=Download, 1=My Functions, 2=Create
    private int currentPage = 0;
    private Map<String, String> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();

    public FunctionChestHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public FunctionChestHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerType<?> type) {
        super(type != null ? type : ModScreens.FUNCTION_CHEST, syncId);
        this.player = playerInventory.player;
        this.inventory = new SimpleInventory(CONTAINER_SIZE);

        // Container slots
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            int row = i / COLS;
            int col = i % COLS;
            this.addSlot(new LockedSlot(inventory, i, 8 + col * 18, 18 + row * 18));
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        // Load data async and refresh
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
        // Download tab
        setTabItem(0, currentTab == 0 ? Items.LIME_STAINED_GLASS_PANE : Items.GREEN_STAINED_GLASS_PANE,
            "§a§lDownload Functions", "§7Browse and download functions");

        // My Functions tab
        setTabItem(1, currentTab == 1 ? Items.LIGHT_BLUE_STAINED_GLASS_PANE : Items.BLUE_STAINED_GLASS_PANE,
            "§b§lMy Functions", "§7Manage local functions");

        // Create tab
        setTabItem(2, currentTab == 2 ? Items.YELLOW_STAINED_GLASS_PANE : Items.ORANGE_STAINED_GLASS_PANE,
            "§e§lCreate New", "§7Create from templates");

        // Spacers
        for (int i = 3; i < 8; i++) {
            inventory.setStack(i, makeItem(Items.BLACK_STAINED_GLASS_PANE, " ", (String[]) null));
        }

        // Refresh button
        setTabItem(8, Items.CLOCK, "§6§lRefresh", "§7Click to reload");
    }

    private void setTabItem(int slot, net.minecraft.item.Item item, String name, String... lore) {
        ItemStack stack = makeItem(item, name, lore);
        inventory.setStack(slot, stack);
    }

    private ItemStack makeItem(net.minecraft.item.Item item, String name, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(Text.literal(name));
        if (lore != null && lore.length > 0) {
            net.minecraft.nbt.NbtList loreList = new net.minecraft.nbt.NbtList();
            for (String line : lore) {
                loreList.add(net.minecraft.nbt.NbtString.of(
                    net.minecraft.text.Text.Serialization.toJsonString(Text.literal(line))));
            }
            net.minecraft.nbt.NbtCompound display = stack.getOrCreateSubNbt("display");
            display.put("Lore", loreList);
        }
        return stack;
    }

    private void drawContent() {
        int slotStart = COLS; // row 1
        int contentSlots = COLS * 4; // rows 1-4

        List<FunctionEntry> entries = getCurrentEntries();
        int startIdx = currentPage * contentSlots;

        for (int i = 0; i < contentSlots; i++) {
            int slotIdx = slotStart + i;
            int entryIdx = startIdx + i;
            if (entryIdx < entries.size()) {
                FunctionEntry entry = entries.get(entryIdx);
                inventory.setStack(slotIdx, entry.toItemStack());
            }
        }
    }

    private List<FunctionEntry> getCurrentEntries() {
        List<FunctionEntry> entries = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, String> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        entries.add(new FunctionEntry(e.getKey(), e.getValue(), EntryType.DOWNLOAD));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    String desc = manifest.getOrDefault(name, "");
                    entries.add(new FunctionEntry(name, desc, EntryType.LOCAL));
                }
            }
            case 2 -> {
                entries.add(new FunctionEntry("Empty Function", "Blank function file", EntryType.CREATE_EMPTY));
                entries.add(new FunctionEntry("Command Template", "Common command examples", EntryType.CREATE_CMD));
                entries.add(new FunctionEntry("Mob Spawner Template", "Mob spawning and effects", EntryType.CREATE_MOB));
                entries.add(new FunctionEntry("Building Template", "fill/setblock commands", EntryType.CREATE_BUILD));
            }
        }
        return entries;
    }

    private void drawNavigation() {
        int total = getCurrentEntries().size();
        int contentSlots = COLS * 4;
        int maxPage = Math.max(0, (total - 1) / contentSlots);

        // Page indicator
        ItemStack pageInfo = makeItem(Items.PAPER,
            "§6Page " + (currentPage + 1) + " / " + (maxPage + 1),
            "§7" + total + " items");
        inventory.setStack(49, pageInfo);

        // Prev page
        if (currentPage > 0) {
            inventory.setStack(45, makeItem(Items.ARROW, "§a§l← Previous Page", null));
        }
        // Next page
        if (currentPage < maxPage) {
            inventory.setStack(53, makeItem(Items.ARROW, "§a§lNext Page →", null));
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType != SlotActionType.PICKUP) return;

        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;

        Slot slot = this.slots.get(slotIndex);
        if (!(slot instanceof LockedSlot)) return; // player inventory slots handled normally

        if (slotIndex == 0) { currentTab = 0; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 1) { currentTab = 1; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 2) { currentTab = 2; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 8) { resetAndRefresh(); return; }
        if (slotIndex == 45 && currentPage > 0) { currentPage--; refreshDisplay(); return; }
        if (slotIndex == 53) { currentPage++; refreshDisplay(); return; }

        if (slotIndex >= COLS && slotIndex < COLS * 5) {
            handleContentClick(slotIndex);
        }
    }

    private void resetAndRefresh() {
        manifest = FunctionManager.fetchFunctionManifest();
        localFunctions = FunctionManager.listLocalFunctions();
        refreshDisplay();
    }

    private void handleContentClick(int slotIndex) {
        int contentSlots = COLS * 4;
        int entryIdx = currentPage * contentSlots + (slotIndex - COLS);
        List<FunctionEntry> entries = getCurrentEntries();
        if (entryIdx >= entries.size()) return;

        FunctionEntry entry = entries.get(entryIdx);
        String name = entry.name;

        switch (entry.type) {
            case DOWNLOAD -> {
                FunctionManager.downloadFunction(name, player.getCommandSource());
                player.sendMessage(Text.literal("§a⬇ Downloading §f" + name + "§a..."), false);
                scheduleRefresh();
            }
            case LOCAL -> {
                if (button == 1) {
                    // Right-click: delete
                    try {
                        Path file = AliasManager.getFunctionsPath().resolve(name + ".mcfunction");
                        Files.deleteIfExists(file);
                        player.sendMessage(Text.literal("§c🗑 Deleted §f" + name), false);
                    } catch (Exception e) {
                        player.sendMessage(Text.literal("§c✖ Error: §7" + e.getMessage()), false);
                    }
                    scheduleRefresh();
                } else {
                    // Left-click: run
                    if (player.getServer() != null) {
                        player.getServer().getCommandManager().executeWithPrefix(
                            player.getCommandSource(), "/cmd function " + name);
                    }
                    player.sendMessage(Text.literal("§6▶ Running §f" + name + "§6..."), false);
                }
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
                    String content = getTemplateContent(entry.type, newName);
                    Files.writeString(file, content);
                    player.sendMessage(Text.literal("§a✔ Created §f" + newName + " §7| Edit: config/CommandMaker/Functions/" + newName + ".mcfunction"), false);
                } catch (Exception e) {
                    player.sendMessage(Text.literal("§c✖ Error: §7" + e.getMessage()), false);
                }
                scheduleRefresh();
            }
        }
    }

    private void scheduleRefresh() {
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            localFunctions = FunctionManager.listLocalFunctions();
            manifest = FunctionManager.fetchFunctionManifest();
            if (currentTab == 0) currentTab = 1; // switch to My Functions after creating/downloading
            currentPage = 0;
            refreshDisplay();
        }).start();
    }

    private static String getTemplateContent(EntryType type, String name) {
        String header = "# " + name + "\n# Created with Command Maker\n\n";
        return switch (type) {
            case CREATE_CMD -> header + "# Command Examples Template\n"
                + "# give @a minecraft:diamond 5\n"
                + "# time set day\n"
                + "# say Hello everyone!\n";
            case CREATE_MOB -> header + "# Mob Spawning Template\n"
                + "# summon minecraft:wither ~ ~5 ~\n"
                + "# give @a minecraft:diamond_sword 1\n"
                + "# give @a minecraft:golden_apple 5\n"
                + "# effect give @a minecraft:strength 120 1\n";
            case CREATE_BUILD -> header + "# Building Template\n"
                + "# fill ~-10 ~-1 ~-10 ~10 ~-1 ~10 minecraft:grass_block\n"
                + "# fill ~-10 ~0 ~-10 ~10 ~4 ~-10 minecraft:stone_bricks\n"
                + "# setblock ~0 ~1 ~0 minecraft:glowstone\n";
            default -> header + "# Empty Function\n"
                + "# Add your Minecraft commands below\n\n"
                + "# say Hello, world!\n";
        };
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    // --- Inner classes ---

    private enum EntryType {
        DOWNLOAD, LOCAL, CREATE_EMPTY, CREATE_CMD, CREATE_MOB, CREATE_BUILD
    }

    private static class FunctionEntry {
        final String name;
        final String description;
        final EntryType type;

        FunctionEntry(String name, String description, EntryType type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }

        ItemStack toItemStack() {
            net.minecraft.item.Item item = switch (type) {
                case DOWNLOAD -> Items.PAPER;
                case LOCAL -> Items.BOOK;
                default -> Items.WRITABLE_BOOK;
            };
            String prefix = switch (type) {
                case DOWNLOAD -> "§a";
                case LOCAL -> "§b";
                default -> "§e";
            };
            ItemStack stack = new ItemStack(item);
            stack.setCustomName(Text.literal(prefix + name));
            if (description != null && !description.isEmpty()) {
                net.minecraft.nbt.NbtList loreList = new net.minecraft.nbt.NbtList();
                loreList.add(net.minecraft.nbt.NbtString.of(
                    net.minecraft.text.Text.Serialization.toJsonString(Text.literal("§7" + description))));
                String action = switch (type) {
                    case DOWNLOAD -> "§eClick to download";
                    case LOCAL -> "§aLeft-click to run  §cRight-click to delete";
                    default -> "§eClick to create";
                };
                loreList.add(net.minecraft.nbt.NbtString.of(
                    net.minecraft.text.Text.Serialization.toJsonString(Text.literal(""))));
                loreList.add(net.minecraft.nbt.NbtString.of(
                    net.minecraft.text.Text.Serialization.toJsonString(Text.literal(action))));
                net.minecraft.nbt.NbtCompound display = stack.getOrCreateSubNbt("display");
                display.put("Lore", loreList);
            }
            return stack;
        }
    }

    private static class LockedSlot extends Slot {
        public LockedSlot(SimpleInventory inventory, int index, int x, int y) {
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
