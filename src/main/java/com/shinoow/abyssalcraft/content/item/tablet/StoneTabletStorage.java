package com.shinoow.abyssalcraft.content.item.tablet;

import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Persistent 49-slot payload carried by a Stone Tablet. */
public final class StoneTabletStorage {

    public static final int INVENTORY_SIZE = 49;
    public static final String INVENTORY_KEY = "ItemInventory";
    public static final String ENERGY_KEY = "PotEnergy";
    public static final String CURSED_KEY = "Cursed";

    private StoneTabletStorage() {}

    public static boolean hasInventory(ItemStack tablet) {
        CompoundTag root = ItemDataCompat.copyData(tablet);
        return (root.contains(INVENTORY_KEY, Tag.TAG_LIST) || root.contains(INVENTORY_KEY, Tag.TAG_COMPOUND))
            && root.contains(ENERGY_KEY, Tag.TAG_ANY_NUMERIC);
    }

    public static NonNullList<ItemStack> load(ItemStack tablet, HolderLookup.Provider registries) {
        NonNullList<ItemStack> contents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        CompoundTag root = ItemDataCompat.copyData(tablet);
        CompoundTag container = new CompoundTag();
        if (root.contains(INVENTORY_KEY, Tag.TAG_LIST)) {
            ListTag legacyItems = root.getList(INVENTORY_KEY, Tag.TAG_COMPOUND).copy();
            normalizeSlots(legacyItems, 1, INVENTORY_SIZE);
            container.put("Items", legacyItems);
        } else if (root.contains(INVENTORY_KEY, Tag.TAG_COMPOUND)) {
            container = root.getCompound(INVENTORY_KEY);
            validateSlots(container.getList("Items", Tag.TAG_COMPOUND), 0, INVENTORY_SIZE - 1);
        }
        ContainerCompat.loadItems(container, contents, registries);
        if (contents.stream().filter(stack -> !stack.isEmpty()).count()
            != container.getList("Items", Tag.TAG_COMPOUND).size()) {
            throw new IllegalArgumentException("Stone Tablet contains an invalid item payload");
        }
        return contents;
    }

    public static void store(ItemStack tablet, NonNullList<ItemStack> contents, float potentialEnergy,
                             HolderLookup.Provider registries) {
        if (contents.size() != INVENTORY_SIZE) {
            throw new IllegalArgumentException("Stone Tablet payload must contain exactly 49 slots");
        }
        CompoundTag inventory = new CompoundTag();
        ContainerCompat.saveItems(inventory, contents, registries);
        ListTag legacyItems = inventory.getList("Items", Tag.TAG_COMPOUND).copy();
        for (Tag entry : legacyItems) {
            CompoundTag item = (CompoundTag) entry;
            item.putByte("Slot", (byte) (item.getByte("Slot") + 1));
        }
        CompoundTag root = ItemDataCompat.copyData(tablet);
        root.put(INVENTORY_KEY, legacyItems);
        root.putFloat(ENERGY_KEY, Math.max(0.0F, potentialEnergy));
        ItemDataCompat.setData(tablet, root);
    }

    public static void clear(ItemStack tablet) {
        CompoundTag root = ItemDataCompat.copyData(tablet);
        root.remove(INVENTORY_KEY);
        root.remove(ENERGY_KEY);
        ItemDataCompat.setData(tablet, root);
    }

    public static float potentialEnergy(ItemStack tablet) {
        return ItemDataCompat.copyData(tablet).getFloat(ENERGY_KEY);
    }

    public static int storedStacks(ItemStack tablet) {
        CompoundTag root = ItemDataCompat.copyData(tablet);
        if (root.contains(INVENTORY_KEY, Tag.TAG_LIST)) {
            return root.getList(INVENTORY_KEY, Tag.TAG_COMPOUND).size();
        }
        return root.contains(INVENTORY_KEY, Tag.TAG_COMPOUND)
            ? root.getCompound(INVENTORY_KEY).getList("Items", Tag.TAG_COMPOUND).size() : 0;
    }

    public static boolean isCursed(ItemStack tablet) {
        return ItemDataCompat.copyData(tablet).contains(CURSED_KEY);
    }

    public static void setCursed(ItemStack tablet, boolean cursed) {
        CompoundTag root = ItemDataCompat.copyData(tablet);
        if (cursed) root.putBoolean(CURSED_KEY, true);
        else root.remove(CURSED_KEY);
        ItemDataCompat.setData(tablet, root);
    }

    private static void normalizeSlots(ListTag items, int minimum, int maximum) {
        validateSlots(items, minimum, maximum);
        for (Tag entry : items) {
            CompoundTag item = (CompoundTag) entry;
            item.putByte("Slot", (byte) (item.getByte("Slot") - 1));
        }
    }

    private static void validateSlots(ListTag items, int minimum, int maximum) {
        boolean[] occupied = new boolean[INVENTORY_SIZE];
        for (Tag entry : items) {
            CompoundTag item = (CompoundTag) entry;
            if (!item.contains("Slot", Tag.TAG_ANY_NUMERIC)) {
                throw new IllegalArgumentException("Stone Tablet item is missing its slot");
            }
            int slot = item.getByte("Slot");
            if (slot < minimum || slot > maximum || occupied[slot - minimum]) {
                throw new IllegalArgumentException("Stone Tablet contains an invalid or duplicate slot");
            }
            occupied[slot - minimum] = true;
        }
    }
}