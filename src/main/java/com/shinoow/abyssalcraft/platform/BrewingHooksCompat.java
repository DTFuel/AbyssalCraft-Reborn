package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if forge {
import net.minecraftforge.event.ForgeEventFactory;
//?} else {
/*import net.neoforged.neoforge.event.EventHooks;
*///?}

/** Loader-neutral access to the modded brewing lifecycle hooks. */
public final class BrewingHooksCompat {

    private BrewingHooksCompat() {}

    /** Returns true when another mod cancels the brew before any stack is consumed. */
    public static boolean onAttempt(NonNullList<ItemStack> stacks) {
        //? if forge {
        return ForgeEventFactory.onPotionAttemptBrew(stacks);
        //?} else {
        /*return EventHooks.onPotionAttemptBrew(stacks);
        *///?}
    }

    public static void onBrewed(NonNullList<ItemStack> stacks) {
        //? if forge {
        ForgeEventFactory.onPotionBrewed(stacks);
        //?} else {
        /*EventHooks.onPotionBrewed(stacks);
        *///?}
    }

    public static void onPlayerBrewed(Player player, ItemStack stack) {
        //? if forge {
        ForgeEventFactory.onPlayerBrewedPotion(player, stack);
        //?} else {
        /*EventHooks.onPlayerBrewedPotion(player, stack);
        *///?}
    }
}