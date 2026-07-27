package com.shinoow.abyssalcraft.integration.jei;

import java.util.List;

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
 * JEI category for Transformation rituals (RR-JEI-AUTO / TP.5b / T8.1b).
 * Shows Necronomicon rituals that transform ground blocks (e.g., soul sand → ethaxium).
 *
 * <p>Recipe data comes from {@link RitualManifestCatalog}, the immutable authoritative source.
 * Displays 8 pedestal offerings and transformed result block. Only TRANSFORMATION kind rituals.
 */
public final class TransformationRitualCategory implements IRecipeCategory<RitualManifest> {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 100;

    private final RecipeType<RitualManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;

    public TransformationRitualCategory(IGuiHelper gui, RecipeType<RitualManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.transformation_ritual");
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
        // 8 pedestal offerings arranged in a circle
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

        // Transformation result (right side)
        if (ritual.result() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 42).setBackground(slot, -1, -1)
                .addItemStack(new ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ritual.result())));
        }
    }

    @Override
    public void draw(RitualManifest ritual, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Display ritual name
        Component nameText = Component.translatable("ritual.abyssalcraft." + ritual.id());
        graphics.drawString(graphics.pose().last().pose(), nameText, 2, 2, 0x404040, false);

        // Display PE requirement
        if (ritual.requiredEnergy() > 0) {
            Component peText = Component.translatable("jei.abyssalcraft.ritual_energy",
                (int)ritual.requiredEnergy());
            graphics.drawString(graphics.pose().last().pose(), peText, 2, 14, 0x808080, false);
        }

        // Display dimension requirement if present
        if (ritual.dimension() != null) {
            String dimName = ritual.dimension().location().getPath();
            Component dimText = Component.translatable("jei.abyssalcraft.ritual_dimension", dimName);
            graphics.drawString(graphics.pose().last().pose(), dimText, 2, 26, 0x606060, false);
        }

        // Display book type requirement
        Component bookText = Component.translatable("jei.abyssalcraft.ritual_book_type",
            ritual.bookType());
        graphics.drawString(graphics.pose().last().pose(), bookText, 2, 88, 0x606060, false);
    }

    /**
     * Get all TRANSFORMATION rituals from the manifest catalog.
     */
    public static List<RitualManifest> getTransformationRituals() {
        return RitualManifestCatalog.entries().stream()
            .filter(ritual -> ritual.kind() == RitualManifest.Kind.TRANSFORMATION)
            .toList();
    }
}
