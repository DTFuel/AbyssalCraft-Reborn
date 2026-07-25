package com.shinoow.abyssalcraft.content.item.bag;

import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.recipe.materialization.CountedIngredient;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;

public final class CrystalBagStorage {

    private static final String SIZE_KEY = "InvSize";
    private static final String ITEMS_KEY = "ItemInventory";

    private CrystalBagStorage() {}

    public static boolean isBag(ItemStack stack) {
        return stack.getItem() instanceof CrystalBagItem;
    }

    public static boolean isCrystal(ItemStack stack) {
        return contains(MaterialItems.CRYSTALS, stack)
            || contains(MaterialItems.CRYSTAL_SHARDS, stack)
            || contains(MaterialItems.CRYSTAL_FRAGMENTS, stack);
    }

    private static boolean contains(List<? extends java.util.function.Supplier<? extends net.minecraft.world.item.Item>> items,
                                    ItemStack stack) {
        for (java.util.function.Supplier<? extends net.minecraft.world.item.Item> item : items) {
            if (stack.is(item.get())) {
                return true;
            }
        }
        return false;
    }

    public static NonNullList<ItemStack> load(ItemStack bag, HolderLookup.Provider registries) {
        if (!(bag.getItem() instanceof CrystalBagItem bagItem)) {
            return NonNullList.create();
        }
        CompoundTag root = ItemDataCompat.copyData(bag);
        NonNullList<ItemStack> contents = NonNullList.withSize(bagItem.capacity(), ItemStack.EMPTY);
        CompoundTag containerTag = new CompoundTag();
        if (root.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            containerTag.put("Items", root.getList(ITEMS_KEY, Tag.TAG_COMPOUND).copy());
        }
        ContainerCompat.loadItems(containerTag, contents, registries);
        return contents;
    }

    public static void save(ItemStack bag, NonNullList<ItemStack> contents, HolderLookup.Provider registries) {
        save(bag, contents, registries, true);
    }

    static void saveInventory(ItemStack bag, NonNullList<ItemStack> contents, HolderLookup.Provider registries) {
        save(bag, contents, registries, false);
    }

    private static void save(ItemStack bag, NonNullList<ItemStack> contents, HolderLookup.Provider registries,
                             boolean requireOnlyCrystals) {
        if (!(bag.getItem() instanceof CrystalBagItem bagItem)) {
            throw new IllegalArgumentException("Cannot save crystal contents to a non-bag item");
        }
        if (contents.size() != bagItem.capacity()) {
            throw new IllegalArgumentException("Crystal Bag inventory size does not match its capacity");
        }
        if (requireOnlyCrystals && !hasOnlyCrystals(contents)) {
            throw new IllegalArgumentException("Crystal Bag inventory contains a non-crystal item");
        }
        CompoundTag containerTag = new CompoundTag();
        ContainerCompat.saveItems(containerTag, contents, registries);
        CompoundTag root = ItemDataCompat.copyData(bag);
        root.putInt(SIZE_KEY, bagItem.capacity());
        if (containerTag.contains("Items", Tag.TAG_LIST)) {
            root.put(ITEMS_KEY, containerTag.getList("Items", Tag.TAG_COMPOUND).copy());
        } else {
            root.remove(ITEMS_KEY);
        }
        ItemDataCompat.setData(bag, root);
    }

    public static boolean canConsume(ItemStack bag, List<CountedIngredient> inputs,
                                     HolderLookup.Provider registries) {
        return canConsume(load(bag, registries), inputs);
    }

    public static boolean canConsume(NonNullList<ItemStack> contents, List<CountedIngredient> inputs) {
        if (!hasOnlyCrystals(contents)) {
            return false;
        }
        int[] available = new int[contents.size()];
        for (int slot = 0; slot < contents.size(); slot++) {
            available[slot] = contents.get(slot).getCount();
        }
        for (CountedIngredient input : inputs) {
            int remaining = input.count();
            for (int slot = 0; slot < contents.size() && remaining > 0; slot++) {
                if (available[slot] > 0 && input.ingredient().test(contents.get(slot))) {
                    int consumed = Math.min(remaining, available[slot]);
                    available[slot] -= consumed;
                    remaining -= consumed;
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean consume(ItemStack bag, List<CountedIngredient> inputs,
                                  HolderLookup.Provider registries) {
        NonNullList<ItemStack> contents = load(bag, registries);
        if (!hasOnlyCrystals(contents)) {
            return false;
        }
        NonNullList<ItemStack> updated = copy(contents);
        if (!consumeFrom(updated, inputs)) {
            return false;
        }
        save(bag, updated, registries);
        return true;
    }

    private static boolean consumeFrom(NonNullList<ItemStack> contents, List<CountedIngredient> inputs) {
        for (CountedIngredient input : inputs) {
            int remaining = input.count();
            for (ItemStack stack : contents) {
                if (remaining > 0 && input.ingredient().test(stack)) {
                    int consumed = Math.min(remaining, stack.getCount());
                    stack.shrink(consumed);
                    remaining -= consumed;
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private static NonNullList<ItemStack> copy(NonNullList<ItemStack> source) {
        NonNullList<ItemStack> copy = NonNullList.withSize(source.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < source.size(); slot++) {
            copy.set(slot, source.get(slot).copy());
        }
        return copy;
    }

    private static boolean hasOnlyCrystals(NonNullList<ItemStack> contents) {
        for (ItemStack stack : contents) {
            if (!stack.isEmpty() && !isCrystal(stack)) {
                return false;
            }
        }
        return true;
    }
}