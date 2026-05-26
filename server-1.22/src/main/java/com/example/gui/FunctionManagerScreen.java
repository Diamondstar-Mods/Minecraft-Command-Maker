package com.example.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    private int currentTab = 0; // 0=Download, 1=My Functions, 2=Create
    private int currentPage = 0;
    private Map<String, String> manifest = new LinkedHashMap<>();
    private List<String> localFunctions = new ArrayList<>();
    private boolean loading = true;
    private String statusMessage = "";

    private int guiLeft, guiTop;

    public FunctionManagerScreen(Screen previousScreen) {
        super(Component.literal("Command Maker - Functions"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void added() {
        super.init();
        this.guiLeft = (this.width - TEXTURE_W) / 2;
        this.guiTop = (this.height - TEXTURE_H) / 2;
        loadData();
    }

    private void loadData() {
        loading = true;
        statusMessage = "§7⌛ Loading function library...";
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://diamondstar-mods.github.io/Minecraft-Command-Maker/cdn/functions/functions.json"))
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonElement je = JsonParser.parseString(response.body());
                    if (je.isJsonObject()) {
                        manifest.clear();
                        for (Map.Entry<String, JsonElement> entry : je.getAsJsonObject().entrySet()) {
                            manifest.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
            } catch (Exception e) {
                statusMessage = "§cFailed to load online functions";
            }

            localFunctions.clear();
            try {
                Path functionsPath = Paths.get("config", "CommandMaker", "Functions");
                if (Files.exists(functionsPath)) {
                    Files.list(functionsPath)
                        .filter(p -> p.toString().endsWith(".mcfunction"))
                        .forEach(p -> {
                            String name = p.getFileName().toString().replace(".mcfunction", "");
                            localFunctions.add(name);
                        });
                }
            } catch (Exception e) {}

            loading = false;
        }).start();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractBackground(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);

        // Draw chest-like background (dark border + light interior)
        context.fill(guiLeft - 2, guiTop - 2, guiLeft + TEXTURE_W + 2, guiTop + TEXTURE_H + 2, 0xFF000000);
        context.fill(guiLeft, guiTop, guiLeft + TEXTURE_W, guiTop + TEXTURE_H, 0xFFC6C6C6);

        // Title bar
        context.fill(guiLeft, guiTop, guiLeft + TEXTURE_W, guiTop + 16, 0xFF404040);
        context.text(this.getFont(), Component.literal("§6§lCommand Maker - Functions"), guiLeft + 8, guiTop + 4, 0xFFFFFF, false);

        if (loading) {
            context.text(this.getFont(), Component.literal(statusMessage), guiLeft + 8, guiTop + 30, 0xFFFFFF, false);
            return;
        }

        drawTabs(context, mouseX, mouseY);
        drawFunctionSlots(context, mouseX, mouseY);
        drawNavigation(context, mouseX, mouseY);
    }

    private void drawTabs(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int tabY = guiTop + 18;
        String[] labels = {"Download", "My Functions", "Create"};

        for (int i = 0; i < 3; i++) {
            int x = guiLeft + 8 + i * 55;
            boolean active = currentTab == i;
            int tabColor = active ? 0xFF3D3D3D : 0xFF5A5A5A;
            int textColor = active ? 0xFFFF55 : 0xAAAAAA;

            context.fill(x, tabY, x + 50, tabY + 14, tabColor);
            context.fill(x, tabY, x + 50, tabY + 1, active ? 0xFF55FF55 : 0xFF3D3D3D); // green underline for active
            context.text(this.getFont(), Component.literal(labels[i]), x + 4, tabY + 3, textColor, false);
        }
    }

    private void drawFunctionSlots(GuiGraphicsExtractor context, int mouseX, int mouseY) {
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
                    drawSlot(context, x, y, slot, mouseX, mouseY);
                } else {
                    // Empty slot
                    context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF8B8B8B);
                    context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF666666);
                }
            }
        }

        int infoY = startY + slotAreaRows * SLOT_SIZE + 4;
        context.text(this.getFont(),
            Component.literal("Page " + (currentPage + 1) + " - " + slots.size() + " items"),
            guiLeft + 8, infoY, 0x808080, false);
    }

    private List<SlotData> getCurrentSlots() {
        List<SlotData> slots = new ArrayList<>();
        switch (currentTab) {
            case 0 -> {
                for (Map.Entry<String, String> e : manifest.entrySet()) {
                    if (!localFunctions.contains(e.getKey())) {
                        slots.add(new SlotData(e.getKey(), e.getValue(), SlotAction.DOWNLOAD));
                    }
                }
            }
            case 1 -> {
                List<String> sorted = new ArrayList<>(localFunctions);
                Collections.sort(sorted);
                for (String name : sorted) {
                    String desc = manifest.getOrDefault(name, "Local function");
                    slots.add(new SlotData(name, desc, SlotAction.RUN_DELETE));
                }
            }
            case 2 -> {
                slots.add(new SlotData("Empty Function", "Create a blank function file", SlotAction.CREATE_EMPTY));
                slots.add(new SlotData("Command Template", "Pre-filled with command examples", SlotAction.CREATE_COMMAND));
                slots.add(new SlotData("Mob Spawner Template", "Mob summoning and effect commands", SlotAction.CREATE_MOB));
                slots.add(new SlotData("Building Template", "fill/setblock building commands", SlotAction.CREATE_BUILD));
            }
        }
        return slots;
    }

    private void drawSlot(GuiGraphicsExtractor context, int x, int y, SlotData slot, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;

        // Slot background
        int bgColor = hovered ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, bgColor);

        // Item color indicator
        int iconColor = switch (slot.action) {
            case DOWNLOAD -> 0xFF3D8B3D;
            case RUN_DELETE -> 0xFF3D3D8B;
            case CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD -> 0xFF8B8B3D;
            default -> 0xFF666666;
        };
        context.fill(x + 3, y + 3, x + SLOT_SIZE - 3, y + SLOT_SIZE - 3, iconColor);

        // First letter of slot name as icon
        String letter = slot.name.substring(0, 1).toUpperCase();
        context.text(this.getFont(), Component.literal("§f" + letter), x + 6, y + 4, 0xFFFFFF, false);

        if (hovered && slot.description != null && !slot.description.isEmpty()) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§e" + slot.name));
            tooltip.add(Component.literal("§7" + slot.description));
            String actionHint = switch (slot.action) {
                case DOWNLOAD -> "§aClick to download";
                case RUN_DELETE -> "§aLeft-click to run  §cRight-click to delete";
                case CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD -> "§eClick to create";
                default -> "";
            };
            if (!actionHint.isEmpty()) {
                tooltip.add(Component.literal(actionHint));
            }
            // Tooltip system changed in MC 26.1

            // context.drawTooltip(this.getFont(), tooltip, mouseX + 5, mouseY + 5);
        }
    }

    private void drawNavigation(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int navY = guiTop + TEXTURE_H - 28;
        int totalSlots = getCurrentSlots().size();
        int slotRows = ROWS - 2;
        int maxPage = Math.max(0, (totalSlots - 1) / (slotRows * COLUMNS));

        // Page info
        String pageInfo = "Page " + (currentPage + 1) + " / " + (maxPage + 1);
        int infoWidth = this.getFont().width(pageInfo);
        context.text(this.getFont(), Component.literal(pageInfo), guiLeft + (TEXTURE_W - infoWidth) / 2, navY + 4, 0xFFFFFF, false);

        // Prev button
        if (currentPage > 0) {
            int px = guiLeft + 8;
            context.fill(px, navY, px + 20, navY + 16, 0xFF3D8B3D);
            context.fill(px + 1, navY + 1, px + 19, navY + 15, 0xFF55FF55);
            context.text(this.getFont(), Component.literal("§0<"), px + 7, navY + 3, 0xFFFFFF, false);
        }

        // Next button
        if (currentPage < maxPage) {
            int nx = guiLeft + TEXTURE_W - 28;
            context.fill(nx, navY, nx + 20, navY + 16, 0xFF3D8B3D);
            context.fill(nx + 1, navY + 1, nx + 19, navY + 15, 0xFF55FF55);
            context.text(this.getFont(), Component.literal("§0>"), nx + 7, navY + 3, 0xFFFFFF, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (loading) return false;

        // Tab clicks
        int tabY = guiTop + 18;
        for (int i = 0; i < 3; i++) {
            int tx = guiLeft + 8 + i * 55;
            if (mouseX >= tx && mouseX < tx + 50 && mouseY >= tabY && mouseY < tabY + 14) {
                currentTab = i;
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
        if (this.minecraft == null || this.minecraft.player == null) return;

        switch (slot.action) {
            case DOWNLOAD -> {
                this.minecraft.player.connection.sendCommand("/cmd downloadfunction " + slot.name);
                statusMessage = "§aDownloading: " + slot.name;
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    loadData();
                }).start();
            }
            case RUN_DELETE -> {
                if (button == 1) {
                    this.minecraft.player.connection.sendCommand("/cmd function delete " + slot.name);
                    localFunctions.remove(slot.name);
                    statusMessage = "§cDeleted: " + slot.name;
                } else {
                    this.minecraft.player.connection.sendCommand("/cmd function " + slot.name);
                    statusMessage = "§6Running: " + slot.name;
                }
            }
            case CREATE_EMPTY -> {
                this.minecraft.player.connection.sendCommand("/cmd function create empty_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating empty function...";
                scheduleRefresh();
            }
            case CREATE_COMMAND -> {
                this.minecraft.player.connection.sendCommand("/cmd function create cmd_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating command template...";
                scheduleRefresh();
            }
            case CREATE_MOB -> {
                this.minecraft.player.connection.sendCommand("/cmd function create mob_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating mob template...";
                scheduleRefresh();
            }
            case CREATE_BUILD -> {
                this.minecraft.player.connection.sendCommand("/cmd function create build_" + System.currentTimeMillis() % 100000);
                statusMessage = "§aCreating building template...";
                scheduleRefresh();
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
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(previousScreen);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private enum SlotAction {
        NONE, DOWNLOAD, RUN_DELETE, CREATE_EMPTY, CREATE_COMMAND, CREATE_MOB, CREATE_BUILD
    }

    private static class SlotData {
        final String name;
        final String description;
        final SlotAction action;

        SlotData(String name, String description, SlotAction action) {
            this.name = name;
            this.description = description;
            this.action = action;
        }
    }
}
