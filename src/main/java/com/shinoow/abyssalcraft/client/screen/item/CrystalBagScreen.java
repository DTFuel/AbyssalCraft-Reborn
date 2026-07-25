package com.shinoow.abyssalcraft.client.screen.item;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

public final class CrystalBagScreen extends AbstractContainerScreen<CrystalBagMenu> {

    public CrystalBagScreen(CrystalBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 18 + menu.rows() * 18 + 14 + 76;
        inventoryLabelY = 18 + menu.rows() * 18 + 4;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20252B);
        graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7,
            topPos + 18 + menu.rows() * 18, 0xFF303840);
        graphics.fill(leftPos + 7, topPos + inventoryLabelY + 10,
            leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFF303840);
        for (int slot = 0; slot < menu.bagSlots(); slot++) {
            int x = leftPos + 7 + slot % 9 * 18;
            int y = topPos + 17 + slot / 9 * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xFF171B20);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF46515C);
            graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF252C33);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}