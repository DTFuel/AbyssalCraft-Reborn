package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Compat: {@code NonNullList<ItemStack>} NBT persistence (vanilla axis).
 *
 * <p>1.21 made {@link ContainerHelper#saveAllItems}/{@code loadAllItems} take a
 * {@link HolderLookup.Provider} because {@code ItemStack} NBT became component-based. Callers pass the
 * provider they received in the version-correct save/load hook (see {@link BlockEntityCompat}); on
 * 1.20.1 the parameter is ignored. Keeps the item-NBT fork out of business code.
 */
public final class ContainerCompat {

    private ContainerCompat() {}

    /** Write all stacks under the {@code Items} tag. {@code registries} is ignored on 1.20.1. */
    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        //? if >=1.21 {
        /*ContainerHelper.saveAllItems(tag, items, registries);
        *///?} else {
        ContainerHelper.saveAllItems(tag, items);
        //?}
    }

    /** Read all stacks from the {@code Items} tag. {@code registries} is ignored on 1.20.1. */
    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        items.clear();
        //? if >=1.21 {
        /*ContainerHelper.loadAllItems(tag, items, registries);
        *///?} else {
        ContainerHelper.loadAllItems(tag, items);
        //?}
    }

    /**
     * Whether two stacks may merge -- same item and same data. 1.21 renamed
     * {@code isSameItemSameTags} to {@code isSameItemSameComponents} (NBT became components), so the
     * item-stacking check is kept out of business code (the item-transfer engine's filters).
     */
    public static boolean canStack(ItemStack a, ItemStack b) {
        //? if >=1.21 {
        /*return ItemStack.isSameItemSameComponents(a, b);
        *///?} else {
        return ItemStack.isSameItemSameTags(a, b);
        //?}
    }

    /** Compare item data while deliberately ignoring durability/damage (legacy wildcard-subtype filter). */
    public static boolean canStackIgnoringDamage(ItemStack a, ItemStack b) {
        if (!ItemStack.isSameItem(a, b)) {
            return false;
        }
        ItemStack normalizedA = a.copy();
        ItemStack normalizedB = b.copy();
        normalizedA.setDamageValue(0);
        normalizedB.setDamageValue(0);
        return canStack(normalizedA, normalizedB);
    }
}
