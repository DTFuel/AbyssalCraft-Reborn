package com.shinoow.abyssalcraft.platform;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Compat: damage equipped armor across the 1.20/1.21 ItemStack durability API split. */
public final class ArmorDurabilityCompat {

    private ArmorDurabilityCompat() {}

    public static void damage(ItemStack stack, int amount, LivingEntity wearer, EquipmentSlot slot) {
        if (stack.isEmpty() || amount <= 0) return;
        //? if <1.21 {
        stack.hurtAndBreak(amount, wearer, entity -> entity.broadcastBreakEvent(slot));
        //?} else {
        /*if (wearer.level() instanceof net.minecraft.server.level.ServerLevel level) {
            stack.hurtAndBreak(amount, level, wearer, item -> wearer.onEquippedItemBroken(item, slot));
        }
        *///?}
    }

    public static void damageHeld(ItemStack stack, int amount, LivingEntity wearer, InteractionHand hand) {
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        damage(stack, amount, wearer, slot);
    }
}