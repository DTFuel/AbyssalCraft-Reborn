package com.shinoow.abyssalcraft.content.recipe.anvil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

/**
 * Anvil forging recipe (PC-2): two ingredient inputs produce one result, plus an XP-like {@code price}
 * and a {@code forgingType} tag (default / ritual_charm, mirroring the 1.12.2
 * {@code api.recipe.AnvilForgingType}). Fork-free -- the {@link net.minecraft.world.item.crafting.Recipe}
 * interface and serializer forks live in {@code platform/}.
 */
public class AnvilForgingRecipe extends DataRecipeCompat {

    private final Ingredient input1;
    private final Ingredient input2;
    private final int price;
    private final String forgingType;

    public AnvilForgingRecipe(Ingredient input1, Ingredient input2, ItemStack result, int price, String forgingType) {
        super(result);
        this.input1 = input1;
        this.input2 = input2;
        this.price = price;
        this.forgingType = forgingType;
    }

    public Ingredient input1() { return input1; }
    public Ingredient input2() { return input2; }
    public int price() { return price; }
    public String forgingType() { return forgingType; }

    public boolean matches(ItemStack left, ItemStack right) {
        return input1.test(left) && input2.test(right);
    }

    public static Optional<AnvilForgingRecipe> find(Level level, ItemStack left, ItemStack right) {
        return DataRecipeCompat.findEntry(level, ModRecipes.ANVIL_FORGING.get(),
            recipe -> recipe.matches(left, right)).map(DataRecipeCompat.Entry::value);
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANVIL_FORGING.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ANVIL_FORGING_SERIALIZER.get();
    }
}
