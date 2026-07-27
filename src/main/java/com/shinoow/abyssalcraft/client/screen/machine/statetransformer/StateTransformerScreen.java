package com.shinoow.abyssalcraft.client.screen.machine.statetransformer;

import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformerBlockEntity;
import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformerMenu;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.UpdateModeMessage;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class StateTransformerScreen extends AbstractContainerScreen<StateTransformerMenu> {

    private static final ResourceLocation TEXTURE =
        ACRef.id("textures/gui/container/state_transformer.png");

    private Button modeButton;

    public StateTransformerScreen(StateTransformerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 238;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> switchMode())
            .bounds(leftPos + 2, topPos + 95, 40, 20).build());
    }

    private void switchMode() {
        if (!menu.processing()) {
            int nextMode = menu.mode() == StateTransformerBlockEntity.MODE_INSERT
                ? StateTransformerBlockEntity.MODE_EXTRACT : StateTransformerBlockEntity.MODE_INSERT;
            ACNetwork.sendToServer(new UpdateModeMessage(nextMode, 0));
        }
    }

    private Component modeLabel() {
        return Component.translatable("gui.abyssalcraft.state_transformer."
            + (menu.mode() == StateTransformerBlockEntity.MODE_INSERT ? "insert" : "extract"));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (modeButton != null) {
            modeButton.active = !menu.processing();
            modeButton.setMessage(modeLabel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progressWidth = Math.round(menu.progress() * 24.0F);
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 6, topPos + 43, 176, 14, progressWidth + 1, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}