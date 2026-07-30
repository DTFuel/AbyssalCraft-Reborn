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

    private final RecipeType<RitualManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public CreationRitualCategory(IGuiHelper gui, RecipeType<RitualManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.creation_ritual");
        // Use ritual altar as icon
        this.icon = gui.createDrawable(ACRef.id("textures/block/ritual_altar.png"), 0, 0, 16, 16);
        this.background = LegacyJeiBackgrounds.ritual(gui);
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
        // Center altar slot (top center)
        if (!ritual.center().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, RitualJeiLayout.CENTER_X, RitualJeiLayout.CENTER_Y)
                .setBackground(slot, -1, -1)
                .addItemStack(ritual.center().example());
        }
        RitualJeiLayout.addOfferings(builder, ritual.offeringLayout(), slot);
        RitualJeiLayout.addBook(builder, ritual.bookType(), slot);

        // Result item (right side)
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
        // Display PE requirement
        if (ritual.requiredEnergy() > 0) {
            Component peText = Component.translatable("jei.abyssalcraft.ritual_energy",
                (int)ritual.requiredEnergy());
            graphics.drawString(Minecraft.getInstance().font, peText,
                2, RitualJeiLayout.FOOTER_Y, 0x808080, false);
        }

        // Display book type requirement
        Component bookText = Component.translatable("jei.abyssalcraft.ritual_book_type",
            ritual.bookType());
        graphics.drawString(Minecraft.getInstance().font, bookText,
            2, RitualJeiLayout.FOOTER_SECOND_Y, 0x606060, false);
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
