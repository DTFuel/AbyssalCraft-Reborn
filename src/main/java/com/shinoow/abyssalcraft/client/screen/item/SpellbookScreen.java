package com.shinoow.abyssalcraft.client.screen.item;

import com.shinoow.abyssalcraft.content.menu.spellbook.SpellbookMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.shinoow.abyssalcraft.platform.ACRef;

/** Compact spell-inscription surface matching the legacy seven-slot layout. */
public final class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/spellcraft_test.png");

    public SpellbookScreen(SpellbookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 238;
        titleLabelY = 6;
        inventoryLabelY = 144;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}