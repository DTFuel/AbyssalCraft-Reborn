package com.shinoow.abyssalcraft.client.screen.item;

import com.shinoow.abyssalcraft.content.menu.spellbook.SpellbookMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact spell-inscription surface matching the legacy seven-slot layout. */
public final class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {

    public SpellbookScreen(SpellbookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 238;
        titleLabelY = 6;
        inventoryLabelY = 144;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF18151C);
        graphics.fill(leftPos + 7, topPos + 17, leftPos + imageWidth - 7, topPos + 137, 0xFF292330);
        graphics.fill(leftPos + 7, topPos + 151, leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFF252129);
        int[][] slots = {{51, 73}, {51, 48}, {76, 69}, {65, 98}, {37, 98}, {26, 69}, {134, 73}};
        for (int index = 0; index < slots.length; index++) {
            int x = leftPos + slots[index][0] - 1;
            int y = topPos + slots[index][1] - 1;
            graphics.fill(x, y, x + 18, y + 18, index == 6 ? 0xFF846747 : 0xFF51465B);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171419);
        }
        graphics.fill(leftPos + 96, topPos + 80, leftPos + 124, topPos + 82, 0xFF9F7A49);
        graphics.fill(leftPos + 120, topPos + 76, leftPos + 124, topPos + 86, 0xFF9F7A49);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE6D5A7, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFC8BFCB, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}