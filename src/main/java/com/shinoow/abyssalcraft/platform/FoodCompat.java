package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
*///?}

/**
 * Compat: FoodProperties builder (vanilla axis).
 *
 * <p>The food builder setter was renamed between versions: 1.20.1 {@code saturationMod(float)} vs
 * 1.21 {@code saturationModifier(float)} (and {@code alwaysEat()}/{@code meat()} became
 * {@code alwaysEdible()}/a wolf-food item tag). Item code builds food through {@link #food(int, float)}
 * so a version bump only touches this class, keeping business item registrars fork-free.
 *
 * <p>Food EFFECTS (the 1.12.2 {@code onFoodEaten} potion effects, including the custom coralium/dread
 * plague) are intentionally not modelled here yet -- they are re-added with the effect system (T7.10).
 * See {@code docs/spec/item-content.md}.
 */
public final class FoodCompat {

    private FoodCompat() {}

    /** A simple food component: hunger restored ({@code nutrition}) and its {@code saturation} modifier. */
    public static FoodProperties food(int nutrition, float saturation) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(nutrition);
        //? if >=1.21 {
        /*builder.saturationModifier(saturation);
        *///?} else {
        builder.saturationMod(saturation);
        //?}
        return builder.build();
    }

    /** Whether the stack carries a food component in this Minecraft version. */
    public static boolean isFood(ItemStack stack) {
        //? if >=1.21 {
        /*return stack.get(DataComponents.FOOD) != null;
        *///?} else {
        return stack.isEdible();
        //?}
    }
}
