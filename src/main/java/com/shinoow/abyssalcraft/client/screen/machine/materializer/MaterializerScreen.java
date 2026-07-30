package com.shinoow.abyssalcraft.client.screen.machine.materializer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.machine.materializer.MaterializerMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * Materializer screen (owned by PP-3).
 *
 * <p>Uses the legacy Materializer background; modern page buttons remain live above it.
 */
public class MaterializerScreen extends AbstractContainerScreen<MaterializerMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/materializer.png");

    public MaterializerScreen(MaterializerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(0))
            .bounds(leftPos + 132, topPos + 57, 16, 16).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
            .bounds(leftPos + 151, topPos + 57, 16, 16).build());
    }

    private void changePage(int direction) {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, direction);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.drawString(font, (menu.page() + 1) + "/" + (menu.maxPage() + 1),
            leftPos + 104, topPos + 61, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
