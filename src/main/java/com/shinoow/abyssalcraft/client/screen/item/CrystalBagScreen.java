package com.shinoow.abyssalcraft.client.screen.item;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.ACRef;

public final class CrystalBagScreen extends AbstractContainerScreen<CrystalBagMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/crystalbag.png");

    public CrystalBagScreen(CrystalBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 118 + menu.rows() * 18;
        inventoryLabelY = imageHeight - 98;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int rowsHeight = menu.rows() * 18;
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, rowsHeight + 17);
        graphics.blit(TEXTURE, leftPos, topPos + rowsHeight + 17, 0, 160, imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}