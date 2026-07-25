package com.shinoow.abyssalcraft.platform;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if forge {
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeInput;
*///?}

/**
 * Compat base for AbyssalCraft's non-processing "data" recipes (anvil forging, rending): each carries a
 * result {@link ItemStack} plus its own extra fields, and its real matching lives in the consuming
 * machine/item -- they are enumerated via {@code getAllRecipesFor}, not vanilla container matching, so
 * {@link #matches} is a deliberate stub.
 *
 * <p>Absorbs the 1.21 {@link Recipe} rework ({@code Container} -&gt; {@code RecipeInput},
 * {@code RegistryAccess} -&gt; {@code HolderLookup.Provider}, recipe id moved to the external
 * {@code RecipeHolder}) so business subclasses in {@code content/recipe/**} stay fork-free and only
 * supply their {@link RecipeType}/{@link RecipeSerializer} and fields. Mirrors the shape of
 * {@link RecipeCompat} (the single-input processing base) for the arbitrary-shape recipes.
 */
public abstract class DataRecipeCompat implements Recipe<
        //? if forge {
        Container
        //?} else {
        /*RecipeInput
        *///?}
        > {

    protected final ItemStack result;

    //? if forge {
    private ResourceLocation id;
    //?}

    protected DataRecipeCompat(ItemStack result) {
        this.result = result;
    }

    public ItemStack result() { return result; }

    public record Entry<T extends DataRecipeCompat>(ResourceLocation id, T value) {}

    /**
     * All recipes of {@code type} in the level's recipe manager, as plain recipe values (mirrors
     * {@link RecipeCompat#allOfType} for the data-recipe base). Absorbs the 1.21 change where
     * {@code getAllRecipesFor} returns {@code List<RecipeHolder<T>>} rather than {@code List<T>}. Used by
     * the JEI integration (PJ-1) to enumerate anvil forging / rending recipes.
     */
    public static <T extends DataRecipeCompat> java.util.List<T> allOfType(Level level, RecipeType<T> type) {
        //? if forge {
        return level.getRecipeManager().getAllRecipesFor(type);
        //?} else {
        /*return level.getRecipeManager().getAllRecipesFor(type).stream()
            .map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();
        *///?}
    }

    /** Enumerate recipes while retaining their stable datapack ids on both supported versions. */
    public static <T extends DataRecipeCompat> List<Entry<T>> entriesOfType(Level level, RecipeType<T> type) {
        //? if forge {
        return level.getRecipeManager().getAllRecipesFor(type).stream()
            .map(recipe -> new Entry<>(recipe.getId(), recipe)).toList();
        //?} else {
        /*return level.getRecipeManager().getAllRecipesFor(type).stream()
            .map(holder -> new Entry<>(holder.id(), holder.value())).toList();
        *///?}
    }

    /** First recipe entry matching a machine-owned predicate. */
    public static <T extends DataRecipeCompat> Optional<Entry<T>> findEntry(
            Level level, RecipeType<T> type, Predicate<T> predicate) {
        return entriesOfType(level, type).stream().filter(entry -> predicate.test(entry.value())).findFirst();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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
        return false;
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
        return false;
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
