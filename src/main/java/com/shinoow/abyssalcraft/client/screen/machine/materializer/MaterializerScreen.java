package com.shinoow.abyssalcraft.client.screen.machine.materializer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.machine.materializer.MaterializerMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

/**
 * Materializer screen (owned by PP-3).
 *
 * <p>Reuses the vanilla furnace GUI texture -- the PP-1 {@link MachineMenu} slot layout
 * (input 56,17 / fuel 56,53 / output 116,35 / arrow 79,34) matches the furnace, so no bespoke texture
 * is shipped for the pilot. {@code blit}/{@code renderBg}/{@code renderTooltip} are vanilla-shared;
 * only {@code renderBackground} forks and is routed through {@link ClientScreenCompat}.
 */
public class MaterializerScreen extends AbstractContainerScreen<MaterializerMenu> {

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
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20252B);
        graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7, topPos + 76, 0xFF303840);
        graphics.fill(leftPos + 7, topPos + 82, leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFF303840);
        graphics.drawString(font, (menu.page() + 1) + "/" + (menu.maxPage() + 1), leftPos + 104, topPos + 61, 0xFFE0E0E0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
