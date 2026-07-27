package com.shinoow.abyssalcraft.integration.jei;

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

import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;

/**
 * JEI category for Rending recipes (RR-JEI-AUTO / TP.5b / T8.1b).
 * Shows entity → essence conversion via Staff of Rending / Rending Pedestal.
 *
 * <p>Recipe data comes from {@code abyssalcraft:rending} data-driven recipes, the authoritative source.
 * Entity display uses entity name translation. Pedestal machine is not yet implemented, but recipes exist.
 */
public final class RendingCategory implements IRecipeCategory<RendingRecipe> {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 54;

    private final RecipeType<RendingRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;

    public RendingCategory(IGuiHelper gui, RecipeType<RendingRecipe> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.rending");
        // Use staff of rending as icon (base tier)
        this.icon = gui.createDrawable(ACRef.id("textures/item/staff_of_rending.png"), 0, 0, 16, 16);
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<RendingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RendingRecipe recipe, IFocusGroup focuses) {
        // Output essence on the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 19).setBackground(slot, -1, -1)
            .addItemStack(recipe.result());
    }

    @Override
    public void draw(RendingRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // Display entity name on left side
        String entityKey = "entity." + recipe.entity().replace(':', '.');
        Component entityName = Component.translatable(entityKey);
        graphics.drawString(Minecraft.getInstance().font, entityName, 1, 8, 0x404040, false);

        // Display energy requirement
        Component energyText = Component.translatable("jei.abyssalcraft.rending_energy",
            recipe.maxEnergy());
        graphics.drawString(Minecraft.getInstance().font, energyText, 1, 20, 0x808080, false);

        // Display energy type (essence name)
        Component essenceText = Component.translatable("jei.abyssalcraft.essence_type",
            recipe.energyName());
        graphics.drawString(Minecraft.getInstance().font, essenceText, 1, 32, 0x606060, false);
    }
}
