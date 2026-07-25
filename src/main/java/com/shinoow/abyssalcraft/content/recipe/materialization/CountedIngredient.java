package com.shinoow.abyssalcraft.content.recipe.materialization;

import net.minecraft.world.item.crafting.Ingredient;

public record CountedIngredient(Ingredient ingredient, int count) {

    public CountedIngredient {
        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException("Materialization ingredient must not be empty");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Materialization ingredient count must be positive");
        }
    }
}