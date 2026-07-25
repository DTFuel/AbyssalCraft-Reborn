package com.shinoow.abyssalcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.content.recipe.anvil.AnvilForgingRecipe;
import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.content.recipe.materialization.MaterializationRecipe;
import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.content.recipe.transmutation.TransmutationRecipe;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.AnvilForgingRecipeSerializer;
import com.shinoow.abyssalcraft.platform.CrystallizationRecipeSerializer;
import com.shinoow.abyssalcraft.platform.MaterializationRecipeSerializer;
import com.shinoow.abyssalcraft.platform.ModRegistrar;
import com.shinoow.abyssalcraft.platform.RendingRecipeSerializer;
import com.shinoow.abyssalcraft.platform.TransmutationRecipeSerializer;

/**
 * Central custom-recipe catalog (PC-2, T2.2): the home of AbyssalCraft's five custom recipe types.
 *
 * <p>Two of them -- {@code anvil_forging} and {@code rending} -- are registered here with their real
 * 1.12.2 shapes ({@link AnvilForgingRecipe}: two inputs to a result plus a price/type;
 * {@link RendingRecipe}: an entity-drain that yields an essence). Registration is loader/version-free:
 * the {@link RecipeType}/{@link RecipeSerializer} forks live in {@code platform/}
 * ({@code DataRecipeCompat} + the two serializers).
 *
 * <p>The other three ({@code crystallization}/{@code materialization}/{@code transmutation}) were
 * registered by the MP pilot machines over the simplified {@link ProcessingRecipe}; they are
 * re-exported below so this class is the single catalog of all five. Folding their registration into
 * this class (and their upgrade to the richer 1.12.2 shapes) is a Stage C2a regression item, so the
 * frozen pilot machine files are left untouched here.
 */
public final class ModRecipes {

    private ModRecipes() {}

    /** {@code minecraft:recipe_type} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<RecipeType<?>> RECIPE_TYPES =
        ModRegistrar.of(Registries.RECIPE_TYPE, AbyssalCraft.MODID);

    /** {@code minecraft:recipe_serializer} registrar in the AbyssalCraft namespace. */
    public static final ModRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        ModRegistrar.of(Registries.RECIPE_SERIALIZER, AbyssalCraft.MODID);

    public static final Supplier<RecipeType<CrystallizationRecipe>> CRYSTALLIZATION = RECIPE_TYPES.register("crystallization", () ->
        RecipeType.<CrystallizationRecipe>simple(ACRef.id("crystallization")));

    public static final Supplier<RecipeSerializer<CrystallizationRecipe>> CRYSTALLIZATION_SERIALIZER = RECIPE_SERIALIZERS.register("crystallization",
        CrystallizationRecipeSerializer::new);

    public static final Supplier<RecipeType<MaterializationRecipe>> MATERIALIZATION = RECIPE_TYPES.register("materialization", () ->
        RecipeType.<MaterializationRecipe>simple(ACRef.id("materialization")));

    public static final Supplier<RecipeSerializer<MaterializationRecipe>> MATERIALIZATION_SERIALIZER = RECIPE_SERIALIZERS.register("materialization",
        MaterializationRecipeSerializer::new);

    public static final Supplier<RecipeType<TransmutationRecipe>> TRANSMUTATION = RECIPE_TYPES.register("transmutation", () ->
        RecipeType.<TransmutationRecipe>simple(ACRef.id("transmutation")));

    public static final Supplier<RecipeSerializer<TransmutationRecipe>> TRANSMUTATION_SERIALIZER = RECIPE_SERIALIZERS.register("transmutation",
        TransmutationRecipeSerializer::new);

    // ---- Anvil forging (input1 + input2 -> result, + price + forging_type) ----
    public static final Supplier<RecipeType<AnvilForgingRecipe>> ANVIL_FORGING = RECIPE_TYPES.register("anvil_forging", () ->
        RecipeType.<AnvilForgingRecipe>simple(ACRef.id("anvil_forging")));

    public static final Supplier<RecipeSerializer<AnvilForgingRecipe>> ANVIL_FORGING_SERIALIZER = RECIPE_SERIALIZERS.register("anvil_forging", () ->
        new AnvilForgingRecipeSerializer());

    // ---- Rending (name + max_energy + entity -> result essence) ----
    public static final Supplier<RecipeType<RendingRecipe>> RENDING = RECIPE_TYPES.register("rending", () ->
        RecipeType.<RendingRecipe>simple(ACRef.id("rending")));

    public static final Supplier<RecipeSerializer<RendingRecipe>> RENDING_SERIALIZER = RECIPE_SERIALIZERS.register("rending", () ->
        new RendingRecipeSerializer());

}
