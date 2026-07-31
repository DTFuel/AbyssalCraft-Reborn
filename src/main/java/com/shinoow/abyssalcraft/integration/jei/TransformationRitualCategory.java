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

import com.shinoow.abyssalcraft.content.block.ritual.RitualBlocks;
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

    private final RecipeType<RitualManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public TransformationRitualCategory(IGuiHelper gui, RecipeType<RitualManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.transformation_ritual");
        this.icon = gui.createDrawableItemStack(new ItemStack(RitualBlocks.RITUAL_ALTAR_ITEM.get()));
        this.background = LegacyJeiBackgrounds.transformationRitual(gui);
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return RitualJeiLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return RitualJeiLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RitualManifest ritual, IFocusGroup focuses) {
        RitualJeiLayout.addOfferings(builder, ritual.offeringLayout(), slot);
        if (!ritual.center().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT,
                RitualJeiLayout.TRANSFORMATION_INPUT_X, RitualJeiLayout.TRANSFORMATION_INPUT_Y)
            .setBackground(slot, -1, -1).addItemStack(ritual.center().example());
        }
        RitualJeiLayout.addTransformationBook(builder, ritual.bookType(), slot);

        // Transformation result (right side)
        if (ritual.result() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RitualJeiLayout.OUTPUT_X, RitualJeiLayout.OUTPUT_Y)
                .setBackground(slot, -1, -1)
                .addItemStack(new ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ritual.result())));
        }
    }

    @Override
    public void draw(RitualManifest ritual, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Display ritual name
        Component nameText = Component.translatable("ritual.abyssalcraft." + ritual.id());
        graphics.drawString(Minecraft.getInstance().font, nameText,
            2, RitualJeiLayout.FOOTER_Y, 0x404040, false);

        // Display PE requirement
        if (ritual.requiredEnergy() > 0) {
            Component peText = Component.translatable("jei.abyssalcraft.ritual_energy",
                (int)ritual.requiredEnergy());
            graphics.drawString(Minecraft.getInstance().font, peText,
                2, RitualJeiLayout.FOOTER_SECOND_Y, 0x808080, false);
        }

        // Display dimension requirement if present
        if (ritual.dimension() != null) {
            var id = ritual.dimension().location();
            Component dimension = Component.translatable("dimension." + id.getNamespace() + "." + id.getPath());
            Component dimText = Component.translatable("jei.abyssalcraft.ritual_dimension", dimension);
            graphics.drawString(Minecraft.getInstance().font, dimText,
                2, RitualJeiLayout.FOOTER_THIRD_Y, 0x606060, false);
        }

        // Display book type requirement
        Component bookText = Component.translatable("jei.abyssalcraft.ritual_book_type",
            ritual.bookType());
        if (ritual.dimension() == null) {
            graphics.drawString(Minecraft.getInstance().font, bookText,
                2, RitualJeiLayout.FOOTER_THIRD_Y, 0x606060, false);
        }
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
