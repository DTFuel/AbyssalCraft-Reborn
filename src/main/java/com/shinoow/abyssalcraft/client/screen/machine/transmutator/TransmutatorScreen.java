package com.shinoow.abyssalcraft.client.screen.machine.transmutator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.machine.transmutator.TransmutatorMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

/**
 * Transmutator screen (owned by PP-4).
 *
 * <p>Sibling of the Crystallizer / Materializer screens: reuses the vanilla furnace GUI texture -- the
 * PP-1 {@link MachineMenu} slot layout (input 56,17 / fuel 56,53 / output 116,35 / arrow 79,34) matches
 * the furnace, so no bespoke texture is shipped for the pilot. {@code blit}/{@code renderBg}/
 * {@code renderTooltip} are vanilla-shared; only {@code renderBackground} forks and is routed through
 * {@link ClientScreenCompat}.
 */
public class TransmutatorScreen extends AbstractContainerScreen<TransmutatorMenu> {

    private static final ResourceLocation TEXTURE = ACRef.vanilla("textures/gui/container/furnace.png");

    public TransmutatorScreen(TransmutatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int arrow = Math.round(menu.progress() * 24);
        if (arrow > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, arrow + 1, 16);
        }
        int flame = Math.round(menu.burn() * 13);
        if (flame > 0) {
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
