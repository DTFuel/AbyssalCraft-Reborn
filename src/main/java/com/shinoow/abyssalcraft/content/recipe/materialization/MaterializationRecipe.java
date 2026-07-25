package com.shinoow.abyssalcraft.content.recipe.materialization;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

public final class MaterializationRecipe extends DataRecipeCompat {

    private final List<CountedIngredient> inputs;

    public MaterializationRecipe(List<CountedIngredient> inputs, ItemStack result) {
        super(result);
        if (inputs == null || inputs.isEmpty() || inputs.size() > 5) {
            throw new IllegalArgumentException("Materialization recipes require between one and five inputs");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Materialization result must not be empty");
        }
        this.inputs = List.copyOf(inputs);
    }

    public List<CountedIngredient> inputs() {
        return inputs;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MATERIALIZATION.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MATERIALIZATION_SERIALIZER.get();
    }
}