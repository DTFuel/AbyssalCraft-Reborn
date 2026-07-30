package com.shinoow.abyssalcraft.integration.jei;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.shinoow.abyssalcraft.content.recipe.materialization.CountedIngredient;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;

public final class MaterializationCategory implements IRecipeCategory<MaterializationRecipe> {

    private final RecipeType<MaterializationRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public MaterializationCategory(IGuiHelper gui, RecipeType<MaterializationRecipe> type,
                                   Component title, ItemStack iconStack) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemStack(iconStack);
        this.background = LegacyJeiBackgrounds.materialization(gui);
        this.slot = gui.getSlotDrawable();
    }

    @Override public RecipeType<MaterializationRecipe> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getBackground() { return background; }
    @Override public int getWidth() { return 118; }
    @Override public int getHeight() { return 72; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterializationRecipe recipe, IFocusGroup focuses) {
        int index = 0;
        for (CountedIngredient input : recipe.inputs()) {
            int x = 3 + index * 23 + (index > 1 ? 1 : 0);
            int y = 3;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).setBackground(slot, -1, -1)
                .addIngredients(input.ingredient());
            index++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 50, 55).setBackground(slot, -1, -1)
            .addItemStack(recipe.result());
    }
}