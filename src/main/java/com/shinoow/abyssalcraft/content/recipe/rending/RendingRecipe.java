package com.shinoow.abyssalcraft.content.recipe.rending;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

/**
 * Rending recipe (PC-2): the Staff of Rending / Rending Pedestal drains {@code maxEnergy} from a target
 * entity to produce a result essence. Data-driven port of the 1.12.2 {@code api.rending.Rending} -- the
 * original {@code Predicate<EntityLiving>} becomes an {@code entity} type id (plus an optional
 * {@code dimension} for filtering); the predicate/energy logic itself is machine-side (deferred).
 * Fork-free.
 */
public class RendingRecipe extends DataRecipeCompat {

    private final String energyName;
    private final int maxEnergy;
    private final String entity;
    private final int dimension;

    public RendingRecipe(String energyName, int maxEnergy, ItemStack result, String entity, int dimension) {
        super(result);
        this.energyName = energyName;
        this.maxEnergy = maxEnergy;
        this.entity = entity;
        this.dimension = dimension;
    }

    public String energyName() { return energyName; }
    public int maxEnergy() { return maxEnergy; }
    public String entity() { return entity; }
    public int dimension() { return dimension; }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RENDING.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RENDING_SERIALIZER.get();
    }
}
