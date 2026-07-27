package com.shinoow.abyssalcraft.integration.jei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;

/**
 * JEI category for Creation rituals (RR-JEI-AUTO / TP.5b / T8.1b).
 * Shows Necronomicon rituals that create scroll items from offerings only (no center item consumed).
 *
 * <p>Recipe data comes from {@link RitualManifestCatalog}, the immutable authoritative source.
 * Displays center altar, 8 pedestal offerings, and result scroll. Only CREATION kind rituals.
 */
public final class CreationRitualCategory implements IRecipeCategory<RitualManifest> {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 100;

    private final RecipeType<RitualManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;

    public CreationRitualCategory(IGuiHelper gui, RecipeType<RitualManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.creation_ritual");
        // Use ritual altar as icon
        this.icon = gui.createDrawable(ACRef.id("textures/block/ritual_altar.png"), 0, 0, 16, 16);
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<RitualManifest> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RitualManifest ritual, IFocusGroup focuses) {
        // Center altar slot (top center)
        if (!ritual.center().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 62, 5).setBackground(slot, -1, -1)
                .addItemStack(ritual.center().example());
        }

        // 8 pedestal offerings arranged in a circle around center
        List<RitualIngredient> offerings = ritual.offeringLayout();
        int[][] pedestalPositions = {
            {30, 25}, {62, 15}, {94, 25},  // top row (positions 0, 1, 2)
            {104, 42},                      // right middle (position 3)
            {94, 59}, {62, 69}, {30, 59},  // bottom row (positions 4, 5, 6)
            {20, 42}                        // left middle (position 7)
        };

        for (int i = 0; i < offerings.size() && i < pedestalPositions.length; i++) {
            RitualIngredient ingredient = offerings.get(i);
            if (!ingredient.isEmpty()) {
                int[] pos = pedestalPositions[i];
                builder.addSlot(RecipeIngredientRole.INPUT, pos[0], pos[1])
                    .setBackground(slot, -1, -1)
                    .addItemStack(ingredient.example());
            }
        }

        // Result item (right side)
        if (ritual.result() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 42).setBackground(slot, -1, -1)
                .addItemStack(new ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ritual.result())));
        }
    }

    @Override
    public void draw(RitualManifest ritual, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Display PE requirement
        if (ritual.requiredEnergy() > 0) {
            Component peText = Component.translatable("jei.abyssalcraft.ritual_energy",
                (int)ritual.requiredEnergy());
            graphics.drawString(Minecraft.getInstance().font, peText, 2, 2, 0x808080, false);
        }

        // Display book type requirement
        Component bookText = Component.translatable("jei.abyssalcraft.ritual_book_type",
            ritual.bookType());
        graphics.drawString(Minecraft.getInstance().font, bookText, 2, 88, 0x606060, false);
    }

    /**
     * Get all CREATION rituals from the manifest catalog.
     */
    public static List<RitualManifest> getCreationRituals() {
        return RitualManifestCatalog.entries().stream()
            .filter(ritual -> ritual.kind() == RitualManifest.Kind.CREATION)
            .toList();
    }
}
