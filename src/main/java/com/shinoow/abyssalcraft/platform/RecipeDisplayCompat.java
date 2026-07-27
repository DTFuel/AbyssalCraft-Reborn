package com.shinoow.abyssalcraft.platform;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/** Version-neutral, read-only recipe data used by in-game documentation screens. */
public final class RecipeDisplayCompat {

    private RecipeDisplayCompat() {}

    public static Optional<DisplayRecipe> find(Level level, ResourceLocation id) {
        //? if forge {
        return level.getRecipeManager().byKey(id).map(recipe -> snapshot(recipe, level));
        //?} else {
        /*return level.getRecipeManager().byKey(id).map(holder -> snapshot(holder.value(), level));
        *///?}
    }

    private static DisplayRecipe snapshot(Recipe<?> recipe, Level level) {
        List<Ingredient> ingredients = List.copyOf(recipe.getIngredients());
        ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
        //? if forge {
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : Math.min(3, ingredients.size());
        int height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight()
            : (ingredients.size() + Math.max(1, width) - 1) / Math.max(1, width);
        //?} else {
        /*int width = recipe instanceof ShapedRecipe shaped ? shaped.width() : Math.min(3, ingredients.size());
        int height = recipe instanceof ShapedRecipe shaped ? shaped.height()
            : (ingredients.size() + Math.max(1, width) - 1) / Math.max(1, width);
        *///?}
        return new DisplayRecipe(ingredients, output, width, height);
    }

    public record DisplayRecipe(List<Ingredient> ingredients, ItemStack output, int width, int height) {
        public DisplayRecipe {
            ingredients = List.copyOf(ingredients);
            output = output.copy();
            if (ingredients.isEmpty() || ingredients.stream().noneMatch(input -> input.getItems().length > 0)
                || output.isEmpty() || ingredients.size() > 9 || width < 1 || width > 3
                || height < 1 || height > 3) {
                throw new IllegalArgumentException("recipe display requires non-empty 3x3 inputs and output");
            }
        }
    }
}