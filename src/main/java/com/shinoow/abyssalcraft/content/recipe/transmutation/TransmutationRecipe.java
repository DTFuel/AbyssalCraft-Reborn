package com.shinoow.abyssalcraft.content.recipe.transmutation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

public final class TransmutationRecipe extends DataRecipeCompat {

    private final Ingredient input;
    private final float experience;
    private final int time;

    public TransmutationRecipe(Ingredient input, ItemStack result, float experience, int time) {
        super(result);
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Transmutation input must not be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Transmutation result must not be empty");
        }
        if (experience < 0F) {
            throw new IllegalArgumentException("Transmutation experience must not be negative");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("Transmutation time must be positive");
        }
        this.input = input;
        this.experience = experience;
        this.time = time;
    }

    public Ingredient input() {
        return input;
    }

    public float experience() {
        return experience;
    }

    public int time() {
        return time;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.TRANSMUTATION.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TRANSMUTATION_SERIALIZER.get();
    }
}