package com.shinoow.abyssalcraft.platform;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if forge {
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
*///?}

/**
 * Compat base for single-input "processing" recipes -- input {@link Ingredient} + result
 * {@link ItemStack} + time -- the shared shape of the crystallizer/materializer/transmutator
 * (vanilla axis).
 *
 * <p>1.21 reworked the {@link Recipe} interface: {@code Container} -&gt; {@code RecipeInput},
 * {@code RegistryAccess} -&gt; {@code HolderLookup.Provider}, and the recipe id was removed (moved to
 * the external {@code RecipeHolder}). This class maps those version-specific signatures onto the fixed
 * processing shape so business subclasses (see {@code content/recipe/base/ProcessingRecipe}) stay
 * fork-free and only supply their {@link RecipeType}/{@link RecipeSerializer}.
 */
public abstract class RecipeCompat implements Recipe<
        //? if forge {
        Container
        //?} else {
        /*RecipeInput
        *///?}
        > {

    protected final Ingredient input;
    protected final ItemStack result;
    protected final int time;

    //? if forge {
    private ResourceLocation id;
    //?}

    protected RecipeCompat(Ingredient input, ItemStack result, int time) {
        this.input = input;
        this.result = result;
        this.time = time;
    }

    public Ingredient input() { return input; }
    public ItemStack result() { return result; }
    public int time() { return time; }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /**
     * Look up the result of the first {@code type} recipe matching {@code input} in the level's recipe
     * manager. Absorbs the 1.21 lookup rework ({@code Container}/{@code Optional<T>} -&gt;
     * {@code RecipeInput}/{@code Optional<RecipeHolder<T>>}). Empty if no recipe matches.
     */
    public static <T extends RecipeCompat> Optional<ItemStack> findResult(Level level, RecipeType<T> type, ItemStack input) {
        //? if forge {
        return level.getRecipeManager().getRecipeFor(type, new SimpleContainer(input), level)
            .map(recipe -> recipe.getResultItem(level.registryAccess()));
        //?} else {
        /*return level.getRecipeManager().getRecipeFor(type, new SingleRecipeInput(input), level)
            .map(holder -> holder.value().getResultItem(level.registryAccess()));
        *///?}
    }

    /**
     * All recipes of {@code type} in the level's recipe manager, as plain recipe values. Absorbs the
     * 1.21 change where {@code getAllRecipesFor} returns {@code List<RecipeHolder<T>>} rather than
     * {@code List<T>}. Used by the JEI integration (PP-5) to enumerate a machine's recipes.
     */
    public static <T extends RecipeCompat> java.util.List<T> allOfType(Level level, RecipeType<T> type) {
        //? if forge {
        return level.getRecipeManager().getAllRecipesFor(type);
        //?} else {
        /*return level.getRecipeManager().getAllRecipesFor(type).stream()
            .map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();
        *///?}
    }

    //? if forge {
    /** 1.20.1 only: the serializer assigns the id parsed from the recipe file path. */
    public void assignId(ResourceLocation recipeId) {
        this.id = recipeId;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return input.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }
    //?} else {
    /*@Override
    public boolean matches(RecipeInput inv, Level level) {
        return input.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }
    *///?}

    @Override
    public abstract RecipeType<?> getType();

    @Override
    public abstract RecipeSerializer<?> getSerializer();
}
