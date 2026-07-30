package com.shinoow.abyssalcraft.integration.jei;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;

public final class CrystallizationCategory implements IRecipeCategory<CrystallizationRecipe> {

    private final RecipeType<CrystallizationRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public CrystallizationCategory(IGuiHelper gui, RecipeType<CrystallizationRecipe> type,
                                   Component title, ItemStack iconStack) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemStack(iconStack);
        this.background = LegacyJeiBackgrounds.crystallization(gui);
        this.slot = gui.getSlotDrawable();
        IDrawableStatic arrowTexture = gui.createDrawable(
            ACRef.id("textures/gui/container/crystallizer_nei.png"), 176, 14, 24, 17);
        this.arrow = gui.createAnimatedDrawable(
            arrowTexture, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public RecipeType<CrystallizationRecipe> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getBackground() { return background; }
    @Override public int getWidth() { return 116; }
    @Override public int getHeight() { return 54; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrystallizationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).setBackground(slot, -1, -1).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 19).setBackground(slot, -1, -1).addItemStack(recipe.result());
        if (!recipe.secondaryResult().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 19).setBackground(slot, -1, -1)
                .addItemStack(recipe.secondaryResult());
        }
    }

    @Override
    public void draw(CrystallizationRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 26, 18);
    }
}