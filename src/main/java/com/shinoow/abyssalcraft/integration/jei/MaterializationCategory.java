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
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public MaterializationCategory(IGuiHelper gui, RecipeType<MaterializationRecipe> type,
                                   Component title, ItemStack iconStack) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemStack(iconStack);
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.createAnimatedRecipeArrow(200);
    }

    @Override public RecipeType<MaterializationRecipe> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public int getWidth() { return 118; }
    @Override public int getHeight() { return 54; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterializationRecipe recipe, IFocusGroup focuses) {
        int index = 0;
        for (CountedIngredient input : recipe.inputs()) {
            int x = 1 + (index % 3) * 18;
            int y = 10 + (index / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).setBackground(slot, -1, -1)
                .addIngredients(input.ingredient());
            index++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 19).setBackground(slot, -1, -1).addItemStack(recipe.result());
    }

    @Override
    public void draw(MaterializationRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 62, 18);
    }
}