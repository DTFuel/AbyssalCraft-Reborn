package com.shinoow.abyssalcraft.content.recipe.base;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.platform.RecipeCompat;

/**
 * Single-input processing recipe (owned by PP-1; frozen for P2 reuse).
 *
 * <p>Concrete, fork-free extension of {@link RecipeCompat}: it only carries its {@link RecipeType} and
 * {@link RecipeSerializer} so each P2 machine can register its own type
 * (crystallization/materialization/transmutation) over this one class.
 */
public class ProcessingRecipe extends RecipeCompat {

    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;

    public ProcessingRecipe(Ingredient input, ItemStack result, int time, RecipeType<?> type, RecipeSerializer<?> serializer) {
        super(input, result, time);
        this.type = type;
        this.serializer = serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }
}
