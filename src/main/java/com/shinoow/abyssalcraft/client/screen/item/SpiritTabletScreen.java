package com.shinoow.abyssalcraft.client.screen.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletMenu;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;
import com.shinoow.abyssalcraft.platform.ACRef;

public final class SpiritTabletScreen extends AbstractContainerScreen<SpiritTabletMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/spirit_tablet.png");

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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.drawString(font, Component.translatable("gui.abyssalcraft.spirit_tablet.subtypes"),
            leftPos + 8, topPos + 42, 0xFF404040, false);
        graphics.drawString(font, Component.translatable("gui.abyssalcraft.spirit_tablet.components"),
            leftPos + 8, topPos + 63, 0xFF404040, false);
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