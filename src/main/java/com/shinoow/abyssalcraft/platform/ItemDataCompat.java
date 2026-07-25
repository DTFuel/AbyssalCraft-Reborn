package com.shinoow.abyssalcraft.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}

/**
 * Compat: per-{@link ItemStack} mod data (vanilla axis).
 *
 * <p>1.20.1 stores arbitrary mod data in the stack's NBT tag ({@code getTag}/{@code getOrCreateTag});
 * 1.21 moved it behind the {@code minecraft:custom_data} component ({@code CustomData}). Item code
 * reads/writes its stored floats (e.g. Potential Energy) through this class so the version divergence
 * stays out of business items. This is the ItemStack-NBT fork the spell/energy items deferred (PS-7).
 */
public final class ItemDataCompat {

    private ItemDataCompat() {}

    /** Return a defensive copy of the stack's complete custom-data compound. */
    public static CompoundTag copyData(ItemStack stack) {
        //? if >=1.21 {
        /*CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
        *///?} else {
        CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag.copy();
        //?}
    }

    /** Replace the stack's complete custom-data compound with a defensive copy of {@code tag}. */
    public static void setData(ItemStack stack, CompoundTag tag) {
        //? if >=1.21 {
        /*stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
        *///?} else {
        stack.setTag(tag.copy());
        //?}
    }

    /** Read a float stored under {@code key} in the stack's mod data ({@code 0} if absent). */
    public static float getFloat(ItemStack stack, String key) {
        return copyData(stack).getFloat(key);
    }

    /** Store {@code value} under {@code key} in the stack's mod data. */
    public static void putFloat(ItemStack stack, String key, float value) {
        CompoundTag tag = copyData(stack);
        tag.putFloat(key, value);
        setData(stack, tag);
    }

    public static int getInt(ItemStack stack, String key, int fallback) {
        CompoundTag tag = copyData(stack);
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    public static void putInt(ItemStack stack, String key, int value) {
        CompoundTag tag = copyData(stack);
        tag.putInt(key, value);
        setData(stack, tag);
    }

    public static String getString(ItemStack stack, String key) {
        return copyData(stack).getString(key);
    }

    public static void putString(ItemStack stack, String key, String value) {
        CompoundTag tag = copyData(stack);
        tag.putString(key, value);
        setData(stack, tag);
    }
}
