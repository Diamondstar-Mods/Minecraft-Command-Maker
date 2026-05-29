package com.example.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliasDeleteScreen extends Screen {
	private static final int COLUMNS = 9;
	private static final int SLOT_SIZE = 18;
	private static final int PADDING = 10;

	private final Map<String, String> aliases;
	private final Screen previousScreen;
	private List<String> aliasNames;
	private int scrollOffset = 0;
	private final int visibleRows = 5;

	public AliasDeleteScreen(Map<String, String> aliases, Screen previousScreen) {
		super(new LiteralText("Delete Aliases"));
		this.aliases = new HashMap<>(aliases);
		this.previousScreen = previousScreen;
		this.aliasNames = new ArrayList<>(aliases.keySet());
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		fill(matrices, 0, 0, this.width, this.height, 0xFF8B8B8B);

		int titleY = PADDING;
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, titleY, 0xFFFFFF);

		int startX = PADDING;
		int startY = PADDING + 20;
		int endX = this.width - PADDING;
		int endY = startY + (SLOT_SIZE * visibleRows);

		fill(matrices, startX - 2, startY - 2, endX + 2, endY + 2, 0xFF000000);
		fill(matrices, startX, startY, endX, endY, 0xFF3F3F3F);

		int slotIndex = scrollOffset * COLUMNS;

		for (int row = 0; row < visibleRows; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				if (slotIndex >= aliasNames.size()) break;

				int x = startX + col * SLOT_SIZE;
				int y = startY + row * SLOT_SIZE;

				String aliasName = aliasNames.get(slotIndex);
				String command = aliases.get(aliasName);

				drawStoneBlock(matrices, x, y, aliasName, command, mouseX, mouseY);
				slotIndex++;
			}
		}

		int instructY = endY + PADDING;
		this.textRenderer.drawWithShadow(matrices, new LiteralText("Click a block to delete the alias"), startX, instructY, 0xFFFFFF);
		this.textRenderer.drawWithShadow(matrices, new LiteralText("Press ESC to go back"), startX, instructY + 12, 0xFFAAAAAA);
	}

	private void drawStoneBlock(MatrixStack matrices, int x, int y, String alias, String command, int mouseX, int mouseY) {
		boolean isHovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;

		int bgColor = isHovered ? 0xFF5F5F5F : 0xFF4F4F4F;
		fill(matrices, x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
		fill(matrices, x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, 0xFF8B8B8B);

		fill(matrices, x, y, x + SLOT_SIZE, y + 1, 0xFFFFFFFF);
		fill(matrices, x, y, x + 1, y + SLOT_SIZE, 0xFFFFFFFF);
		fill(matrices, x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF333333);
		fill(matrices, x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF333333);

		if (isHovered) {
			List<Text> tooltip = new ArrayList<>();
			tooltip.add(new LiteralText("§6/" + alias));
			tooltip.add(new LiteralText("§7Command: " + command));
			this.renderTooltip(matrices, tooltip, mouseX + 5, mouseY + 5);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int totalSlots = aliasNames.size();
		int maxScroll = Math.max(0, (totalSlots + COLUMNS - 1) / COLUMNS - visibleRows);

		scrollOffset -= (int) amount;
		if (scrollOffset < 0) scrollOffset = 0;
		if (scrollOffset > maxScroll) scrollOffset = maxScroll;

		return true;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return false;

		int startX = PADDING;
		int startY = PADDING + 20;

		int slotIndex = scrollOffset * COLUMNS;
		for (int row = 0; row < visibleRows; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				if (slotIndex >= aliasNames.size()) break;

				int x = startX + col * SLOT_SIZE;
				int y = startY + row * SLOT_SIZE;

				if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
					String aliasToDelete = aliasNames.get(slotIndex);
					deleteAlias(aliasToDelete);
					return true;
				}
				slotIndex++;
			}
		}

		return false;
	}

	private void deleteAlias(String alias) {
		if (this.client != null && this.client.player != null) {
			this.client.player.sendChatMessage("/cmd del " + alias);
		}

		aliases.remove(alias);
		aliasNames.remove(alias);
	}

	@Override
	public void onClose() {
		if (this.client != null) {
			this.client.setScreen(previousScreen);
		}
	}
}
