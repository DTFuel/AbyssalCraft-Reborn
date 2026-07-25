package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
//? if <1.21 {
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
*///?}

/**
 * Compat: vanilla potion brewing (version axis).
 *
 * <p>1.20.1 exposes brewing through the static {@code PotionBrewing.isIngredient/hasMix/mix}; 1.21
 * made {@code PotionBrewing} a data-driven instance held by the level ({@code level.potionBrewing()}).
 * The sequential brewing stand calls these version-neutral helpers so it stays {@code //?}-free.
 */
public final class PotionBrewingCompat {

    private PotionBrewingCompat() {}

    public static void attach(IEventBus modBus) {
        //? if >=1.21 {
        /*EventBuses.game().addListener(PotionBrewingCompat::registerNeoRecipes);
        *///?} else {
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(PotionBrewingCompat::registerForgeRecipes));
        //?}
    }

    //? if >=1.21 {
    /*private static void registerNeoRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            com.shinoow.abyssalcraft.content.item.misc.MiscItems.CORALIUM_PLAGUED_FLESH.get(),
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get()));
        builder.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            com.shinoow.abyssalcraft.content.item.misc.MiscItems.ABYSSAL_GHOUL_FLESH.get(),
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get()));
        builder.addMix(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get()),
            net.minecraft.world.item.Items.REDSTONE,
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE_LONG.get()));
        builder.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("dread_fragment")),
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get()));
        builder.addMix(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get()),
            net.minecraft.world.item.Items.REDSTONE,
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE_LONG.get()));
        builder.addMix(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get()),
            net.minecraft.world.item.Items.GLOWSTONE_DUST,
            net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE_STRONG.get()));
    }
    *///?} else {
    private static void registerForgeRecipes() {
        addForgeMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            com.shinoow.abyssalcraft.content.item.misc.MiscItems.CORALIUM_PLAGUED_FLESH.get(),
            com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get());
        addForgeMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            com.shinoow.abyssalcraft.content.item.misc.MiscItems.ABYSSAL_GHOUL_FLESH.get(),
            com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get());
        addForgeMix(com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE.get(), net.minecraft.world.item.Items.REDSTONE,
            com.shinoow.abyssalcraft.system.effect.ACEffects.CPLAGUE_LONG.get());
        addForgeMix(net.minecraft.world.item.alchemy.Potions.AWKWARD,
            net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ACRef.id("dread_fragment")),
            com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get());
        addForgeMix(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get(), net.minecraft.world.item.Items.REDSTONE,
            com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE_LONG.get());
        addForgeMix(com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE.get(), net.minecraft.world.item.Items.GLOWSTONE_DUST,
            com.shinoow.abyssalcraft.system.effect.ACEffects.DPLAGUE_STRONG.get());
    }

    private static void addForgeMix(net.minecraft.world.item.alchemy.Potion input,
                                    net.minecraft.world.item.Item ingredient,
                                    net.minecraft.world.item.alchemy.Potion output) {
        BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
            @Override public boolean isInput(ItemStack stack) { return PotionUtils.getPotion(stack) == input; }
            @Override public boolean isIngredient(ItemStack stack) { return stack.is(ingredient); }
            @Override public ItemStack getOutput(ItemStack inputStack, ItemStack ingredientStack) {
                return isInput(inputStack) && isIngredient(ingredientStack)
                    ? PotionUtils.setPotion(inputStack.copyWithCount(1), output) : ItemStack.EMPTY;
            }
        });
    }
    //?}

    /** True if the stack is a valid brewing ingredient (nether wart, blaze powder, etc.). */
    public static boolean isIngredient(Level level, ItemStack stack) {
        //? if >=1.21 {
        /*return level.potionBrewing().isIngredient(stack);
        *///?} else {
        return BrewingRecipeRegistry.isValidIngredient(stack) || PotionBrewing.isIngredient(stack);
        //?}
    }

    /** True if the stack can occupy one of the three potion input/output slots. */
    public static boolean isInput(Level level, ItemStack stack) {
        //? if >=1.21 {
        /*return level.potionBrewing().isInput(stack);
        *///?} else {
        return BrewingRecipeRegistry.isValidInput(stack);
        //?}
    }

    /** True if {@code ingredient} can transform the potion {@code input} into another potion. */
    public static boolean hasMix(Level level, ItemStack input, ItemStack ingredient) {
        //? if >=1.21 {
        /*return level.potionBrewing().hasMix(input, ingredient);
        *///?} else {
        return BrewingRecipeRegistry.hasOutput(input, ingredient) || PotionBrewing.hasMix(input, ingredient);
        //?}
    }

    /** The result of brewing {@code input} with {@code ingredient} (a new potion stack). */
    public static ItemStack mix(Level level, ItemStack ingredient, ItemStack input) {
        //? if >=1.21 {
        /*return level.potionBrewing().mix(ingredient, input);
        *///?} else {
        ItemStack modded = BrewingRecipeRegistry.getOutput(input, ingredient);
        return modded.isEmpty() ? PotionBrewing.mix(ingredient, input) : modded;
        //?}
    }
}
