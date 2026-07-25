package com.shinoow.abyssalcraft.content.recipe.base;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.RecipeSerializerCompat;

/**
 * Processing recipe type + serializer (owned by PP-1).
 *
 * <p>One example {@code processing} type/serializer built through {@link RecipeSerializerCompat} to
 * prove the recipe-registration path on both loaders; P2 machines register their own type/serializer
 * over {@link ProcessingRecipe}.
 */
public final class ProcessingRecipes {

    private ProcessingRecipes() {}

    /** {@code minecraft:recipe_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<RecipeType<?>> RECIPE_TYPES =
        ModRegistrar.of(Registries.RECIPE_TYPE, AbyssalCraft.MODID);

    /** {@code minecraft:recipe_serializer} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        ModRegistrar.of(Registries.RECIPE_SERIALIZER, AbyssalCraft.MODID);

    /** Example processing recipe type. */
    public static final Supplier<RecipeType<ProcessingRecipe>> PROCESSING = RECIPE_TYPES.register("processing", () ->
        RecipeType.<ProcessingRecipe>simple(ACRef.id("processing")));

    /** Example processing recipe serializer, bound to {@link ProcessingRecipe}. */
    public static final Supplier<RecipeSerializer<ProcessingRecipe>> PROCESSING_SERIALIZER = RECIPE_SERIALIZERS.register("processing", () ->
        RecipeSerializerCompat.processing((input, result, time) ->
            new ProcessingRecipe(input, result, time, PROCESSING.get(), ProcessingRecipes.PROCESSING_SERIALIZER.get())));
}
