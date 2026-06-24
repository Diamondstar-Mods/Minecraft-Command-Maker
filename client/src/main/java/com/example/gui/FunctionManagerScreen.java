package com.example.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import com.example.*;
import com.google.gson.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

public class FunctionManagerScreen extends Screen {
    private static final int ROWS = 6;
    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int TEXTURE_W = 176;
    private static final int TEXTURE_H = 222;

    private final Screen previousScreen;
    private int currentTab = 0; // 0=Download, 1=My Functions, 2=Create, 3=Delete Aliases
    private int currentPage = 0;
    private Map<String, ManifestEntry> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();
    private boolean loading = true;
    private String statusMessage = "";
    private String pendingDeleteAlias = null;

    private int guiLeft, guiTop;

    public FunctionManagerScreen(Screen previousScreen) {
        super(Text.literal("Command Maker - Functions"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - TEXTURE_W) / 2;
        this.guiTop = (this.height - TEXTURE_H) / 2;
        loadData();
    }

    private void loadData() {
        loading = true;
        statusMessage = "§7Loading...";
        new Thread(() -> {
            try {
                manifest = FunctionManager.fetchFunctionManifest();
                statusMessage = "§aLoaded " + manifest.size() + " functions";
            } catch (Exception e) {
                statusMessage = "§cFailed to load online functions";
            }

            localFunctions = FunctionManager.listLocalFunctions();
            loading = false;
        }).start();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw chest-like background
        context.fill(guiLeft - 2, guiTop - 2, guiLeft + TEXTURE_W + 2, guiTop + TEXTURE_H + 2, 0xFF000000);
        context.fill(guiLeft, guiTop, guiLeft + TEXTURE_W, guiTop + TEXTURE_H, 0xFFC6C6C6);

        // Title bar
        context.fill(guiLeft, guiTop, guiLeft + TEXTURE_W, guiTop + 16, 0xFF404040);
        context.drawText(this.textRenderer, Text.literal("§6§lCommand Maker - Functions"), guiLeft + 8, guiTop + 4, 0xFFFFFF, false);

        if (loading) {
            context.drawText(this.textRenderer, Text.literal(statusMessage), guiLeft + 8, guiTop + 30, 0xFFFFFF, false);
            return;
        }

        drawTabs(context, mouseX, mouseY);
        drawFunctionSlots(context, mouseX, mouseY);
        drawNavigation(context, mouseX, mouseY);
    }

    private void drawTabs(DrawContext context, int mouseX, int mouseY) {
        int tabY = guiTop + 18;
        String[] labels = {"Download", "My Functions", "Create", "Del Aliases"};
        int[] colors = {0xFF3D8B3D, 0xFF3D3D8B, 0xFF8B8B3D, 0xFF8B3D3D};

        for (int i = 0; i < 4; i++) {
            int x = guiLeft + 4 + i * 43;
            boolean active = currentTab == i;
            int tabColor = active ? 0xFF3D3D3D : 0xFF5A5A5A;
            int textColor = active ? colors[i] : 0xAAAAAA;

            context.fill(x, tabY, x + 40, tabY + 14, tabColor);
            context.fill(x, tabY, x + 40, tabY + 1, active ? colors[i] : 0xFF3D3D3D);
            context.drawText(this.textRenderer, Text.literal(labels[i]), x + 2, tabY + 3, textColor, false);
        }

        // Confirm delete button (visible in delete aliases tab)
        if (currentTab == 3 && pendingDeleteAlias != null) {
            int btnX = guiLeft + TEXTURE_W - 40;
            context.fill(btnX, tabY, btnX + 36, tabY + 14, 0xFF8B0000);
            context.fill(btnX + 1, tabY + 1, btnX + 35, tabY + 13, 0xFFFF4444);
            context.drawText(this.textRenderer, Text.literal("§f✖ DEL"), btnX + 3, tabY + 3, 0xFFFFFF, false);
        } else if (currentTab == 3) {
            int btnX = guiLeft + TEXTURE_W - 40;
            context.fill(btnX, tabY, btnX + 36, tabY + 14, 0xFF555555);
            context.drawText(this.textRenderer, Text.literal("§7Confirm"), btnX + 1, tabY + 3, 0xAAAAAA, false);
        }
    }

    private void drawFunctionSlots(DrawContext context, int mouseX, int mouseY) {
        int startX = guiLeft + 8;
        int startY = guiTop + 36;
        int slotAreaRows = ROWS - 2;

        List<SlotData> slots = getCurrentSlots();
        int startIdx = currentPage * slotAreaRows * COLUMNS;

        for (int row = 0; row < slotAreaRows; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int idx = startIdx + row * COLUMNS + col;
                int x = startX + col * SLOT_SIZE;
                int y = startY + row * SLOT_SIZE;

                if (idx < slots.size()) {
                    SlotData slot = slots.get(idx);
                    // Highlight pending delete
                    boolean highlighted = currentTab == 3 && pendingDeleteAlias != null && slot.name.equals(pendingDeleteAlias);
                    drawSlot(context, x, y, slot, mouseX, mouseY, highlighted);
                } else {
                    context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF8B8B8B);
                    context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF666666);
                }
            }
        }

        int infoY = startY + slotAreaRows * SLOT_SIZE + 4;
        context.drawText(this.textRenderer,
            Text.literal("Page " + (currentPage + 1) + " - " + slots.size() + " items"),
            guiLeft + 8, infoY, 0x808080, false);
    }

    private List<SlotData> getCurrentSlots() {
        List<SlotData> slots = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, ManifestEntry> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        ManifestEntry me = e.getValue();
                        slots.add(new SlotData(e.getKey(), me.description, SlotAction.DOWNLOAD, me.icon));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    ManifestEntry me = manifest.get(name);
                    String desc = me != null ? me.description : "Local function — left-click to run";
                    slots.add(new SlotData(name, desc, SlotAction.RUN_DELETE, null));
                }
            }
            case 2 -> {
                slots.add(new SlotData("Empty Function", "Create a blank .mcfunction file to write yourself", SlotAction.CREATE_EMPTY, null));
                slots.add(new SlotData("Command Template", "Pre-filled with common command examples to customize", SlotAction.CREATE_COMMAND, null));
                slots.add(new SlotData("Mob Spawner", "Template with mob summoning and effect commands", SlotAction.CREATE_MOB, null));
                slots.add(new SlotData("Building", "Template with fill/setblock building commands", SlotAction.CREATE_BUILD, null));
            }
            case 3 -> {
                Map<String, String> aliases = AliasManager.getAliases();
                List<String> sorted = new ArrayList<>(aliases.keySet());
                Collections.sort(sorted);
                for (String name : sorted) {
                    slots.add(new SlotData(name, aliases.get(name), SlotAction.DELETE_ALIAS, null));
                }
            }
        }
        return slots;
    }

    private void drawSlot(DrawContext context, int x, int y, SlotData slot, int mouseX, int mouseY, boolean highlighted) {
        boolean hovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;

        int bgColor;
        if (highlighted) {
            bgColor = 0xFFFF4444;
        } else if (hovered) {
            bgColor = 0xFFFFFFFF;
        } else {
            bgColor = 0xFF8B8B8B;
        }

        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, highlighted ? 0xFF8B0000 : 0xFF373737);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, bgColor);

        // Item color indicator based on action
        int iconColor = switch (slot.action) {
            case DOWNLOAD -> 0xFF3D8B3D;
            case RUN_DELETE -> 0xFF3D3D8B;
            case CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD -> 0xFF8B8B3D;
            case DELETE_ALIAS -> highlighted ? 0xFFFF0000 : 0xFF8B3D3D;
            default -> 0xFF666666;
        };

        if (!highlighted) {
            context.fill(x + 3, y + 3, x + SLOT_SIZE - 3, y + SLOT_SIZE - 3, iconColor);
        }

        // First letter of slot name as icon
        String letter = slot.name.substring(0, 1).toUpperCase();
        context.drawText(this.textRenderer, Text.literal("§f" + letter), x + 6, y + 4, 0xFFFFFF, false);

        // Tooltip on hover
        if (hovered && slot.description != null && !slot.description.isEmpty()) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.literal("§e" + slot.name));
            if (slot.action == SlotAction.DELETE_ALIAS) {
                tooltip.add(Text.literal("§7Command: " + slot.description));
                tooltip.add(Text.literal(""));
                if (highlighted) {
                    tooltip.add(Text.literal("§c§l⚠ Click CONFIRM to delete!"));
                } else {
                    tooltip.add(Text.literal("§cClick to select for deletion"));
                    tooltip.add(Text.literal("§7Then click the §c✖ DEL §7button to confirm"));
                }
            } else {
                tooltip.add(Text.literal("§7" + slot.description));
            }
            String actionHint = switch (slot.action) {
                case DOWNLOAD -> "§aClick to download";
                case RUN_DELETE -> "§aLeft-click to run  §cRight-click to delete";
                case CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD -> "§eClick to create";
                case DELETE_ALIAS -> highlighted ? "§c§lClick CONFIRM button to delete!" : "§cClick to select for deletion";
                default -> "";
            };
            if (!actionHint.isEmpty()) {
                tooltip.add(Text.literal(actionHint));
            }
            context.drawTooltip(this.textRenderer, tooltip, mouseX + 5, mouseY + 5);
        }
    }

    private void drawNavigation(DrawContext context, int mouseX, int mouseY) {
        int navY = guiTop + TEXTURE_H - 28;
        int totalSlots = getCurrentSlots().size();
        int slotRows = ROWS - 2;
        int maxPage = Math.max(0, (totalSlots - 1) / (slotRows * COLUMNS));

        String pageInfo = "Page " + (currentPage + 1) + " / " + (maxPage + 1);
        int infoWidth = this.textRenderer.getWidth(pageInfo);
        context.drawText(this.textRenderer, Text.literal(pageInfo), guiLeft + (TEXTURE_W - infoWidth) / 2, navY + 4, 0xFFFFFF, false);

        if (currentPage > 0) {
            int px = guiLeft + 8;
            context.fill(px, navY, px + 20, navY + 16, 0xFF3D8B3D);
            context.fill(px + 1, navY + 1, px + 19, navY + 15, 0xFF55FF55);
            context.drawText(this.textRenderer, Text.literal("§0<"), px + 7, navY + 3, 0xFFFFFF, false);
        }

        if (currentPage < maxPage) {
            int nx = guiLeft + TEXTURE_W - 28;
            context.fill(nx, navY, nx + 20, navY + 16, 0xFF3D8B3D);
            context.fill(nx + 1, navY + 1, nx + 19, navY + 15, 0xFF55FF55);
            context.drawText(this.textRenderer, Text.literal("§0>"), nx + 7, navY + 3, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (loading) return false;

        // Tab clicks
        int tabY = guiTop + 18;
        for (int i = 0; i < 4; i++) {
            int tx = guiLeft + 4 + i * 43;
            if (mouseX >= tx && mouseX < tx + 40 && mouseY >= tabY && mouseY < tabY + 14) {
                currentTab = i;
                currentPage = 0;
                pendingDeleteAlias = null;
                return true;
            }
        }

        // Confirm delete button
        if (currentTab == 3 && pendingDeleteAlias != null) {
            int btnX = guiLeft + TEXTURE_W - 40;
            if (mouseX >= btnX && mouseX < btnX + 36 && mouseY >= tabY && mouseY < tabY + 14) {
                AliasManager.removeAlias(pendingDeleteAlias);
                pendingDeleteAlias = null;
                loadData();
                currentPage = 0;
                return true;
            }
        }

        // Navigation
        int totalSlots = getCurrentSlots().size();
        int slotRows = ROWS - 2;
        int maxPage = Math.max(0, (totalSlots - 1) / (slotRows * COLUMNS));
        int navY = guiTop + TEXTURE_H - 28;

        if (currentPage > 0) {
            int px = guiLeft + 8;
            if (mouseX >= px && mouseX < px + 20 && mouseY >= navY && mouseY < navY + 16) {
                currentPage--;
                return true;
            }
        }
        if (currentPage < maxPage) {
            int nx = guiLeft + TEXTURE_W - 28;
            if (mouseX >= nx && mouseX < nx + 20 && mouseY >= navY && mouseY < navY + 16) {
                currentPage++;
                return true;
            }
        }

        // Function slot clicks
        int startX = guiLeft + 8;
        int startY = guiTop + 36;
        List<SlotData> slots = getCurrentSlots();
        int startIdx = currentPage * slotRows * COLUMNS;

        for (int row = 0; row < slotRows; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int idx = startIdx + row * COLUMNS + col;
                if (idx >= slots.size()) break;

                int x = startX + col * SLOT_SIZE;
                int y = startY + row * SLOT_SIZE;

                if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                    handleSlotClick(slots.get(idx), button);
                    return true;
                }
            }
        }

        return false;
    }

    private void handleSlotClick(SlotData slot, int button) {
        if (this.client == null || this.client.player == null) return;

        switch (slot.action) {
            case DOWNLOAD -> {
                this.client.player.networkHandler.sendChatMessage("/cmd downloadfunction " + slot.name);
                statusMessage = "§aDownloading: " + slot.name;
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    loadData();
                }).start();
            }
            case RUN_DELETE -> {
                if (button == 1) {
                    this.client.player.networkHandler.sendChatMessage("/cmd function delete " + slot.name);
                    localFunctions.remove(slot.name);
                    statusMessage = "§cDeleted: " + slot.name;
                } else {
                    this.client.player.networkHandler.sendChatMessage("/cmd function " + slot.name);
                    statusMessage = "§6Running: " + slot.name;
                }
            }
            case CREATE_EMPTY -> {
                this.client.player.networkHandler.sendChatMessage("/cmd function create empty_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating empty function...";
                scheduleRefresh();
            }
            case CREATE_COMMAND -> {
                this.client.player.networkHandler.sendChatMessage("/cmd function create cmd_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating command template...";
                scheduleRefresh();
            }
            case CREATE_MOB -> {
                this.client.player.networkHandler.sendChatMessage("/cmd function create mob_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating mob template...";
                scheduleRefresh();
            }
            case CREATE_BUILD -> {
                this.client.player.networkHandler.sendChatMessage("/cmd function create build_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating building template...";
                scheduleRefresh();
            }
            case DELETE_ALIAS -> {
                if (slot.name.equals(pendingDeleteAlias)) {
                    pendingDeleteAlias = null;
                } else {
                    pendingDeleteAlias = slot.name;
                }
            }
        }
    }

    private void scheduleRefresh() {
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            loadData();
            currentTab = 1;
            currentPage = 0;
        }).start();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0 && currentPage > 0) {
            currentPage--;
        } else if (verticalAmount > 0) {
            int totalSlots = getCurrentSlots().size();
            int slotRows = ROWS - 2;
            int maxPage = Math.max(0, (totalSlots - 1) / (slotRows * COLUMNS));
            if (currentPage < maxPage) currentPage++;
        }
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(previousScreen);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private enum SlotAction {
        NONE, DOWNLOAD, RUN_DELETE, CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD, DELETE_ALIAS
    }

    private static class SlotData {
        final String name;
        final String description;
        final SlotAction action;
        final String iconId;

        SlotData(String name, String description, SlotAction action, String iconId) {
            this.name = name;
            this.description = description;
            this.action = action;
            this.iconId = iconId;
        }
    }
}
