package com.shinoow.abyssalcraft.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
*///?}

/** Compat: whether a stack carries a player-assigned custom name. */
public final class ItemNameCompat {

    private ItemNameCompat() {}

    public static boolean hasCustomName(ItemStack stack) {
        //? if >=1.21 {
        /*return stack.has(DataComponents.CUSTOM_NAME);
        *///?} else {
        return stack.hasCustomHoverName();
        //?}
    }

    public static void setCustomName(ItemStack stack, Component name) {
        //? if >=1.21 {
        /*stack.set(DataComponents.CUSTOM_NAME, name);
        *///?} else {
        stack.setHoverName(name);
        //?}
    }
}