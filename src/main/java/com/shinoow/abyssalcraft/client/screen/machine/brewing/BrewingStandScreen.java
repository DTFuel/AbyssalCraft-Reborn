package com.shinoow.abyssalcraft.client.screen.machine.brewing;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.shinoow.abyssalcraft.content.machine.brewing.BrewingStandMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

/**
 * Sequential Brewing Stand screen (owned by PC-8, Stage C2a).
 *
 * <p>Reuses the vanilla brewing-stand GUI texture -- the ported slot layout matches vanilla for the
 * standard slots (the three sequential transfer-out slots sit to the right). The GUI opens (the DoD's
 * "interface opens"); the bubble/fuel progress overlay and a bespoke texture are deferred to PK.
 * {@code renderBackground} forks and is routed through {@link ClientScreenCompat}.
 */
public class BrewingStandScreen extends AbstractContainerScreen<BrewingStandMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/container/sequential_brewing_stand.png");
    private static final int[] BUBBLE_LENGTHS = {29, 24, 20, 16, 11, 6, 0};

    public BrewingStandScreen(BrewingStandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int fuelWidth = Math.min(18, Math.max(0, (18 * menu.fuel() + 19) / 20));
        if (fuelWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 60, topPos + 44, 176, 29, fuelWidth, 4);
        }
        int remaining = menu.brewTime();
        if (remaining > 0) {
            int progressHeight = (int) (28.0F * menu.brewProgress());
            if (progressHeight > 0) {
                graphics.blit(TEXTURE, leftPos + 97, topPos + 16, 176, 0, 9, progressHeight);
            }
            int bubbleHeight = BUBBLE_LENGTHS[remaining / 2 % BUBBLE_LENGTHS.length];
            if (bubbleHeight > 0) {
                graphics.blit(TEXTURE, leftPos + 63, topPos + 43 - bubbleHeight,
                    185, 29 - bubbleHeight, 12, bubbleHeight);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
