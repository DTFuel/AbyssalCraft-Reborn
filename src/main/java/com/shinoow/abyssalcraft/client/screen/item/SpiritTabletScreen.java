package com.shinoow.abyssalcraft.client.screen.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

public final class SpiritTabletScreen extends AbstractContainerScreen<SpiritTabletMenu> {

    private Button subtypeButton;
    private Button componentsButton;

    public SpiritTabletScreen(SpiritTabletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        subtypeButton = addRenderableWidget(Button.builder(Component.empty(), button -> toggle(0))
            .bounds(leftPos + 122, topPos + 36, 40, 20).build());
        componentsButton = addRenderableWidget(Button.builder(Component.empty(), button -> toggle(1))
            .bounds(leftPos + 122, topPos + 57, 40, 20).build());
        updateLabels();
    }

    private void toggle(int id) {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void updateLabels() {
        if (subtypeButton != null) {
            subtypeButton.setMessage(Component.literal(Boolean.toString(menu.ignoreSubtypes())));
        }
        if (componentsButton != null) {
            componentsButton.setMessage(Component.literal(Boolean.toString(menu.matchComponents())));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateLabels();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF171B20);
        graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7, topPos + 80, 0xFF303840);
        graphics.fill(leftPos + 7, topPos + 82, leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFF303840);
        for (int slot = 0; slot < 5; slot++) {
            int x = leftPos + 43 + slot * 18;
            int y = topPos + 16;
            graphics.fill(x, y, x + 18, y + 18, 0xFF15191D);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF4C5863);
            graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF252C33);
        }
        graphics.drawString(font, Component.translatable("gui.abyssalcraft.spirit_tablet.subtypes"),
            leftPos + 8, topPos + 42, 0xFFE0E0E0, false);
        graphics.drawString(font, Component.translatable("gui.abyssalcraft.spirit_tablet.components"),
            leftPos + 8, topPos + 63, 0xFFE0E0E0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE0E0E0, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFE0E0E0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}