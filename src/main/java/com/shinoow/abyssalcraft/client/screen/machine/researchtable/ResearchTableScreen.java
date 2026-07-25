package com.shinoow.abyssalcraft.client.screen.machine.researchtable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.machine.researchtable.ResearchTableMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

/**
 * Research Table screen (owned by PC-8, Stage C2a).
 *
 * <p>Placeholder inventory screen so the block's GUI opens (the DoD's "interface opens"). The real
 * knowledge-browser UI is deferred to Stage S-B; for now it reuses a vanilla 176x166 background and
 * shows only the player inventory. {@code renderBackground} forks and is routed through
 * {@link ClientScreenCompat}.
 */
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/research_table.png");

    public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 238;
        titleLabelY = 4;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE6D5A7, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
