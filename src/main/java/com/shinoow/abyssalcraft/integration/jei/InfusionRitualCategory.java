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
import com.shinoow.abyssalcraft.system.ritual.RitualIngredient;
import com.shinoow.abyssalcraft.system.ritual.RitualManifest;
import com.shinoow.abyssalcraft.system.ritual.RitualManifestCatalog;

/** JEI presentation of all infusion rituals from the authoritative ritual manifest. */
public final class InfusionRitualCategory implements IRecipeCategory<RitualManifest> {

    private static final int[][] PEDESTAL_POSITIONS = {
        {30, 25}, {62, 15}, {94, 25}, {104, 42},
        {94, 59}, {62, 69}, {30, 59}, {20, 42}
    };

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
    @Override public int getWidth() { return 140; }
    @Override public int getHeight() { return 100; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RitualManifest ritual, IFocusGroup focuses) {
        if (!ritual.center().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 62, 38).setBackground(slot, -1, -1)
                .addItemStack(ritual.center().example());
        }
        List<RitualIngredient> offerings = ritual.offeringLayout();
        for (int index = 0; index < offerings.size(); index++) {
            RitualIngredient ingredient = offerings.get(index);
            if (!ingredient.isEmpty()) {
                int[] position = PEDESTAL_POSITIONS[index];
                builder.addSlot(RecipeIngredientRole.INPUT, position[0], position[1])
                    .setBackground(slot, -1, -1).addItemStack(ingredient.example());
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 42).setBackground(slot, -1, -1)
            .addItemStack(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ritual.result())));
    }

    @Override
    public void draw(RitualManifest ritual, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Component energy = Component.translatable("jei.abyssalcraft.ritual_energy", (int) ritual.requiredEnergy());
        graphics.drawString(Minecraft.getInstance().font, energy, 2, 2, 0x808080, false);
        Component book = Component.translatable("jei.abyssalcraft.ritual_book_type", ritual.bookType());
        graphics.drawString(Minecraft.getInstance().font, book, 2, 88, 0x606060, false);
    }

    public static List<RitualManifest> getInfusionRituals() {
        return RitualManifestCatalog.entries().stream()
            .filter(ritual -> ritual.kind() == RitualManifest.Kind.INFUSION)
            .toList();
    }
}