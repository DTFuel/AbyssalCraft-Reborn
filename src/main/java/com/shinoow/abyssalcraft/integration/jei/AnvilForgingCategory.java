package com.shinoow.abyssalcraft.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import com.shinoow.abyssalcraft.content.recipe.anvil.AnvilForgingRecipe;

/**
 * JEI recipe category for anvil forging (PJ-1 / Stage J), extending the PP-5 JEI pilot to the
 * {@code anvil_forging} recipe type (PC-2). Two ingredient inputs are combined on an anvil into one
 * result at an XP {@code price}; catalyst is the vanilla anvil.
 *
 * <p>Uses JEI's built-in drawables + the recipe's
 * fork-free {@link AnvilForgingRecipe} accessors, with a signature-identical JEI API on JEI 15
 * (Forge 1.20.1) and JEI 19 (NeoForge 1.21.1).
 */
public class AnvilForgingCategory implements IRecipeCategory<AnvilForgingRecipe> {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 54;

    private final RecipeType<AnvilForgingRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public AnvilForgingCategory(IGuiHelper gui, RecipeType<AnvilForgingRecipe> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.anvil_forging");
        this.icon = gui.createDrawableItemStack(new ItemStack(Items.ANVIL));
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<AnvilForgingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilForgingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).setBackground(slot, -1, -1).addIngredients(recipe.input1());
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 19).setBackground(slot, -1, -1).addIngredients(recipe.input2());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 19).setBackground(slot, -1, -1).addItemStack(recipe.result());
    }

    @Override
    public void draw(AnvilForgingRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 44, 18);
        graphics.drawString(Minecraft.getInstance().font,
            Component.translatable("jei.abyssalcraft.anvil_price", recipe.price()), 1, 44, 0x808080, false);
    }
}
