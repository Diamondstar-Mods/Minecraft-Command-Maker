package com.example.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FunctionChestScreen extends AbstractContainerScreen<FunctionChestHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public FunctionChestScreen(FunctionChestHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 222);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw chest background using fills (avoids API compatibility issues)
        int texW = this.imageWidth;
        int texH = this.imageHeight;
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractRenderStateWithTooltipAndSubtitles(context, mouseX, mouseY, delta);
    }

    
}
