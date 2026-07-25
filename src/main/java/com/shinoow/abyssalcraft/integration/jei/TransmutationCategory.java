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

import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;

public final class TransmutationCategory implements IRecipeCategory<TransmutationRecipe> {

    private final RecipeType<TransmutationRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public TransmutationCategory(IGuiHelper gui, RecipeType<TransmutationRecipe> type,
                                 Component title, ItemStack iconStack) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemStack(iconStack);
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.createAnimatedRecipeArrow(200);
    }

    @Override public RecipeType<TransmutationRecipe> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public int getWidth() { return 82; }
    @Override public int getHeight() { return 54; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TransmutationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).setBackground(slot, -1, -1).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 19).setBackground(slot, -1, -1).addItemStack(recipe.result());
    }

    @Override
    public void draw(TransmutationRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 26, 18);
    }
}