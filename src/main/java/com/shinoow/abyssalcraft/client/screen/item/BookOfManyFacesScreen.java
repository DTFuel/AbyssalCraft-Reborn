package com.shinoow.abyssalcraft.client.screen.item;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.material.CrystalClusterBlocks;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.menu.facebook.BookOfManyFacesMenu;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ClientScreenCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class BookOfManyFacesScreen extends AbstractContainerScreen<BookOfManyFacesMenu> {

    private static final ResourceLocation TEXTURE = ACRef.id("textures/gui/face_book.png");
    private int page;
    private Button previous;
    private Button next;

    public BookOfManyFacesScreen(BookOfManyFacesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 160;
    }

    @Override
    protected void init() {
        super.init();
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> turn(-1))
            .bounds(leftPos + 20, topPos + 134, 20, 18).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> turn(1))
            .bounds(leftPos + 132, topPos + 134, 20, 18).build());
        updateButtons();
    }

    private void turn(int direction) {
        page = Math.max(0, Math.min(menu.pageCount() - 1, page + direction));
        updateButtons();
    }

    private void updateButtons() {
        if (previous != null) previous.visible = page > 0;
        if (next != null) next.visible = page + 1 < menu.pageCount();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.abyssalcraft.facebook.name"), 20, 16, 0, false);
        Component crystalHeader = Component.translatable("gui.abyssalcraft.facebook.crystal_size");
        graphics.drawString(font, crystalHeader, imageWidth - 22 - font.width(crystalHeader), 16, 0, false);
        List<BookOfManyFacesMenu.FaceEntry> entries = menu.page(page);
        for (int index = 0; index < entries.size(); index++) {
            BookOfManyFacesMenu.FaceEntry entry = entries.get(index);
            int y = 28 + index * 20;
            List<FormattedCharSequence> lines = font.split(Component.literal(entry.name()), 90);
            graphics.drawString(font, lines.get(0), 20, y, 0, false);
            if (lines.size() > 1) graphics.drawString(font, lines.get(1), 20, y + 9, 0, false);
            graphics.renderItem(crystalForSize(entry.crystalSize()), 115, y - 2);
        }
    }

    private static ItemStack crystalForSize(int size) {
        return switch (size) {
            case 1 -> new ItemStack(MaterialItems.CRYSTALS.get(0).get());
            case 2 -> new ItemStack(CrystalClusterBlocks.CLUSTERS.get(0).get());
            default -> new ItemStack(MaterialItems.CRYSTAL_SHARDS.get(0).get());
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientScreenCompat.background(this, graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}