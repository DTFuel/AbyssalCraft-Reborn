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

/** JEI presentation of all infusion rituals from the authoritative ritual manifest. */
public final class InfusionRitualCategory implements IRecipeCategory<RitualManifest> {

    private final RecipeType<RitualManifest> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;

    public InfusionRitualCategory(IGuiHelper gui, RecipeType<RitualManifest> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.infusion_ritual");
        this.icon = gui.createDrawableItemStack(new ItemStack(RitualBlocks.RITUAL_ALTAR_ITEM.get()));
        this.slot = gui.getSlotDrawable();
    }

    @Override public RecipeType<RitualManifest> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public int getWidth() { return RitualJeiLayout.WIDTH; }
    @Override public int getHeight() { return RitualJeiLayout.HEIGHT; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RitualManifest ritual, IFocusGroup focuses) {
        if (!ritual.center().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, RitualJeiLayout.CENTER_X, RitualJeiLayout.CENTER_Y)
                .setBackground(slot, -1, -1)
                .addItemStack(ritual.center().example());
        }
        RitualJeiLayout.addOfferings(builder, ritual.offeringLayout(), slot);
        builder.addSlot(RecipeIngredientRole.OUTPUT, RitualJeiLayout.OUTPUT_X, RitualJeiLayout.OUTPUT_Y)
            .setBackground(slot, -1, -1)
            .addItemStack(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ritual.result())));
    }

    @Override
    public void draw(RitualManifest ritual, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Component energy = Component.translatable("jei.abyssalcraft.ritual_energy", (int) ritual.requiredEnergy());
        graphics.drawString(Minecraft.getInstance().font, energy, 2, 2, 0x808080, false);
        Component book = Component.translatable("jei.abyssalcraft.ritual_book_type", ritual.bookType());
        graphics.drawString(Minecraft.getInstance().font, book, 2, RitualJeiLayout.FOOTER_Y, 0x606060, false);
    }

    public static List<RitualManifest> getInfusionRituals() {
        return RitualManifestCatalog.entries().stream()
            .filter(ritual -> ritual.kind() == RitualManifest.Kind.INFUSION)
            .toList();
    }
}