package com.shinoow.abyssalcraft.content.recipe.crystallization;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

public final class CrystallizationRecipe extends DataRecipeCompat {

    private final Ingredient input;
    private final ItemStack secondaryResult;
    private final float experience;
    private final int time;

    public CrystallizationRecipe(Ingredient input, ItemStack result, ItemStack secondaryResult,
                                 float experience, int time) {
        super(result);
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Crystallization input must not be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Crystallization result must not be empty");
        }
        if (experience < 0F) {
            throw new IllegalArgumentException("Crystallization experience must not be negative");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("Crystallization time must be positive");
        }
        this.input = input;
        this.secondaryResult = secondaryResult.copy();
        this.experience = experience;
        this.time = time;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStack secondaryResult() {
        return secondaryResult;
    }

    public float experience() {
        return experience;
    }

    public int time() {
        return time;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRYSTALLIZATION.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRYSTALLIZATION_SERIALIZER.get();
    }
}