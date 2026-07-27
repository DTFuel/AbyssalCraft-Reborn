package com.shinoow.abyssalcraft.client.screen.energy;

import com.shinoow.abyssalcraft.content.block.energy.EnergyDepositionerMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class EnergyDepositionerScreen extends AbstractContainerScreen<EnergyDepositionerMenu> {

    private static final ResourceLocation TEXTURE =
        ACRef.id("textures/gui/container/energy_depositioner.png");

    public EnergyDepositionerScreen(EnergyDepositionerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progressWidth = Math.round(menu.progress() * 24.0F);
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 76, topPos + 38, 176, 14, progressWidth + 1, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        String energy = menu.potentialEnergy() + "/" + menu.maxPotentialEnergy() + " PE";
        graphics.drawString(font, energy, imageWidth / 2 - font.width(energy) / 2, 20, 0x404040, false);
        String state = Component.translatable("gui.abyssalcraft.energy_depositioner.state",
            menu.tolerance(), menu.collectorCount(), menu.activeAmplifier() == null
                ? Component.translatable("gui.abyssalcraft.none")
                : Component.literal(menu.activeAmplifier().name())).getString();
        graphics.drawString(font, state, imageWidth / 2 - font.width(state) / 2, 69, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}