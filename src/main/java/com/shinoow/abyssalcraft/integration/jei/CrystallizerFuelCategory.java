package com.shinoow.abyssalcraft.integration.jei;

import java.util.ArrayList;
import java.util.List;

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

import com.shinoow.abyssalcraft.content.machine.crystallizer.CrystallizerBlockEntity;
import com.shinoow.abyssalcraft.content.machine.crystallizer.Crystallizers;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.MachineItemCompat;

/**
 * JEI fuel category for the Crystallizer (RR-JEI-AUTO / TP.5b / T8.1b).
 * Shows which items can be used as fuel in the Crystallizer, with burn times.
 *
 * <p>Fuel items are discovered from {@link CrystallizerBlockEntity#fuelBurnTime}, the single
 * authoritative source. No copy-drift data.
 */
public final class CrystallizerFuelCategory implements IRecipeCategory<FuelRecipe> {

    private final RecipeType<FuelRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable flame;

    public CrystallizerFuelCategory(IGuiHelper gui, RecipeType<FuelRecipe> type) {
        this.type = type;
        this.title = Component.translatable("jei.abyssalcraft.crystallizer_fuel");
        this.icon = gui.createDrawableItemStack(new ItemStack(Crystallizers.CRYSTALLIZER.get()));
        this.slot = gui.getSlotDrawable();
        this.flame = gui.createDrawable(ACRef.id("textures/gui/container/machine.png"), 176, 0, 14, 14);
    }

    @Override
    public RecipeType<FuelRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 34;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FuelRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 10).setBackground(slot, -1, -1)
            .addItemStack(recipe.fuel());
    }

    @Override
    public void draw(FuelRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        flame.draw(graphics, 24, 10);
        int burnTicks = recipe.burnTime();
        Component text = Component.translatable("jei.abyssalcraft.fuel_time",
            String.format("%.1f", burnTicks / 20.0));
        graphics.drawString(graphics.pose().last().pose(), text, 44, 12,
            0x808080, false);
    }

    /**
     * Enumerate all known Crystallizer fuel items from the authoritative fuel predicate.
     * Called once during JEI recipe registration.
     */
    public static List<FuelRecipe> getAllFuels() {
        List<FuelRecipe> fuels = new ArrayList<>();
        JEIAutomationCatalog.CRYSTALLIZER_FUELS.forEach(itemId -> tryAddFuel(fuels, itemId));
        return fuels;
    }

    private static void tryAddFuel(List<FuelRecipe> fuels, String itemId) {
        ItemStack stack = MachineItemCompat.stack(itemId);
        if (!stack.isEmpty()) {
            int burnTime = CrystallizerBlockEntity.fuelBurnTime(stack);
            if (burnTime > 0) {
                fuels.add(new FuelRecipe(stack, burnTime));
            }
        }
    }
}
