package com.shinoow.abyssalcraft.client.screen.machine.rendingpedestal;

import com.shinoow.abyssalcraft.content.machine.rendingpedestal.RendingPedestalMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class RendingPedestalScreen extends AbstractContainerScreen<RendingPedestalMenu> {

    private static final ResourceLocation TEXTURE =
        ACRef.id("textures/gui/container/rending_pedestal.png");

    public RendingPedestalScreen(RendingPedestalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        String pe = menu.potentialEnergy() + "/" + menu.maxPotentialEnergy() + " PE";
        graphics.drawString(font, pe, imageWidth / 2 - font.width(pe) / 2, 20, 0x404040, false);
        drawLedger(graphics, RendingEnergyType.ABYSSAL, "A", 55, 29);
        drawLedger(graphics, RendingEnergyType.DREAD, "D", 108, 29);
        drawLedger(graphics, RendingEnergyType.OMOTHOL, "O", 55, 37);
        drawLedger(graphics, RendingEnergyType.SHADOW, "S", 108, 37);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private void drawLedger(GuiGraphics graphics, RendingEnergyType type, String prefix, int x, int y) {
        graphics.drawString(font, prefix + ": " + menu.rendingEnergy(type) + "/" + type.threshold(),
            x, y, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}