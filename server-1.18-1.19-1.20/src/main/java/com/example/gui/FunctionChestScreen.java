package com.example.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FunctionChestScreen extends HandledScreen<FunctionChestHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    public FunctionChestScreen(FunctionChestHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw chest background using fills (avoids API compatibility issues)
        int texW = this.backgroundWidth;
        int texH = this.backgroundHeight;
        context.fill(x - 3, y - 3, x + texW + 3, y + texH + 3, 0xFF000000);
        context.fill(x, y, x + texW, y + texH, 0xFFC6C6C6);

        // Draw slot backgrounds
        int slotSize = 18;
        for (int i = 0; i < FunctionChestHandler.ROWS; i++) {
            for (int j = 0; j < FunctionChestHandler.COLS; j++) {
                int sx = x + 8 + j * slotSize;
                int sy = y + 18 + i * slotSize;
                context.fill(sx, sy, sx + slotSize, sy + slotSize, 0xFF373737);
                context.fill(sx + 1, sy + 1, sx + slotSize - 1, sy + slotSize - 1, 0xFF8B8B8B);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, Text.literal("§6§lCommand Maker - Functions"), 8, 6, 0x404040, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
    }
}
