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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;

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
    private Map<String, String> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();

    public FunctionChestHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public FunctionChestHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerType<?> type) {
        super(type != null ? type : ModScreens.FUNCTION_CHEST, syncId);
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
        inventory.setStack(0, makeItem(currentTab == 0 ? Items.LIME_STAINED_GLASS_PANE : Items.GREEN_STAINED_GLASS_PANE, "§a§lDownload"));
        inventory.setStack(1, makeItem(currentTab == 1 ? Items.LIGHT_BLUE_STAINED_GLASS_PANE : Items.BLUE_STAINED_GLASS_PANE, "§b§lMy Functions"));
        inventory.setStack(2, makeItem(currentTab == 2 ? Items.YELLOW_STAINED_GLASS_PANE : Items.ORANGE_STAINED_GLASS_PANE, "§e§lCreate New"));
        for (int i = 3; i < 8; i++) {
            inventory.setStack(i, makeItem(Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        inventory.setStack(8, makeItem(Items.CLOCK, "§6§lRefresh"));
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
                inventory.setStack(slotIdx, entries.get(entryIdx).toItemStack());
            }
        }
    }

    private List<FunctionEntry> getCurrentEntries() {
        List<FunctionEntry> entries = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, String> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        entries.add(new FunctionEntry(e.getKey(), EntryType.DOWNLOAD));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    entries.add(new FunctionEntry(name, EntryType.LOCAL));
                }
            }
            case 2 -> {
                entries.add(new FunctionEntry("Empty Function", EntryType.CREATE_EMPTY));
                entries.add(new FunctionEntry("Command Template", EntryType.CREATE_CMD));
                entries.add(new FunctionEntry("Mob Spawner", EntryType.CREATE_MOB));
                entries.add(new FunctionEntry("Building", EntryType.CREATE_BUILD));
            }
        }
        return entries;
    }

    private void drawNavigation() {
        int total = getCurrentEntries().size();
        int contentSlots = COLS * 4;
        int maxPage = Math.max(0, (total - 1) / contentSlots);
        inventory.setStack(49, makeItem(Items.PAPER, "§6Page " + (currentPage + 1) + " / " + (maxPage + 1)));
        if (currentPage > 0) inventory.setStack(45, makeItem(Items.ARROW, "§a§l← Previous"));
        if (currentPage < maxPage) inventory.setStack(53, makeItem(Items.ARROW, "§a§lNext →"));
    }

    private ItemStack makeItem(net.minecraft.item.Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(new LiteralText(name));
        return stack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType != SlotActionType.PICKUP) return;
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        if (!(this.slots.get(slotIndex) instanceof LockedSlot)) return;

        if (slotIndex == 0) { currentTab = 0; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 1) { currentTab = 1; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 2) { currentTab = 2; currentPage = 0; resetAndRefresh(); return; }
        if (slotIndex == 8) { resetAndRefresh(); return; }
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
                    if (button == 1) {
                        try {
                            Files.deleteIfExists(AliasManager.getFunctionsPath().resolve(entry.name + ".mcfunction"));
                        } catch (Exception ignored) {}
                        scheduleRefresh();
                    } else {
                        net.minecraft.server.command.ServerCommandSource source = sp.getCommandSource();
                        source.getServer().getCommandManager().execute(
                            source, "/cmd function " + entry.name);
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
                        Files.writeString(file, "# " + newName + "\n# Created with Command Maker\n");
                    } catch (Exception ignored) {}
                    scheduleRefresh();
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
        DOWNLOAD, LOCAL, CREATE_EMPTY, CREATE_CMD, CREATE_MOB, CREATE_BUILD
    }

    private static class FunctionEntry {
        final String name;
        final EntryType type;

        FunctionEntry(String name, EntryType type) {
            this.name = name;
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
            stack.setCustomName(new LiteralText(prefix + name));
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
