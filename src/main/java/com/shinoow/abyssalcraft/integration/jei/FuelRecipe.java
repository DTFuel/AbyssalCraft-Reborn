package com.shinoow.abyssalcraft.integration.jei;

import net.minecraft.world.item.ItemStack;

/**
 * Synthetic "recipe" for JEI fuel display (RR-JEI-AUTO / TP.5b / T8.1b).
 * Not a real Minecraft recipe type -- only used by JEI to show fuel items and burn times.
 */
public record FuelRecipe(ItemStack fuel, int burnTime) {
}
