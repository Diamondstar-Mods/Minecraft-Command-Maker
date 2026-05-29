package com.example.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class FunctionChestScreen extends HandledScreen<FunctionChestHandler> {
    private static final int TEXTURE_W = 176;
    private static final int TEXTURE_H = 222;

    public FunctionChestScreen(FunctionChestHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        int texW = this.backgroundWidth;
        int texH = this.backgroundHeight;
        fill(matrices, x - 3, y - 3, x + texW + 3, y + texH + 3, 0xFF000000);
        fill(matrices, x, y, x + texW, y + texH, 0xFFC6C6C6);

        int slotSize = 18;
        for (int i = 0; i < FunctionChestHandler.ROWS; i++) {
            for (int j = 0; j < FunctionChestHandler.COLS; j++) {
                int sx = x + 8 + j * slotSize;
                int sy = y + 18 + i * slotSize;
                fill(matrices, sx, sy, sx + slotSize, sy + slotSize, 0xFF373737);
                fill(matrices, sx + 1, sy + 1, sx + slotSize - 1, sy + slotSize - 1, 0xFF8B8B8B);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, "§6§lCommand Maker - Functions", 8, 6, 0x404040);
        this.textRenderer.draw(matrices, this.playerInventoryTitle.asOrderedText(), 8, this.backgroundHeight - 94, 0x404040);
    }
}
