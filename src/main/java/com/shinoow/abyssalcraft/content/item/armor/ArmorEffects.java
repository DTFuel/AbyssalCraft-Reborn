package com.shinoow.abyssalcraft.content.item.armor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ContentConfigMatrix;

/** Server-authoritative legacy armor potion effects. */
public final class ArmorEffects {

    private ArmorEffects() {}

    public static void tick(ServerPlayer player) {
        if (!ContentConfigMatrix.armorPotionEffects()) return;
        apply(player, player.getItemBySlot(EquipmentSlot.HEAD), true);
        apply(player, player.getItemBySlot(EquipmentSlot.FEET), false);
    }

    private static void apply(ServerPlayer player, ItemStack stack, boolean helmet) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (helmet && id.equals("abyssalnite_helmet")) {
            effect(player, MobEffects.WATER_BREATHING, 20, 0);
        } else if (!helmet && id.equals("abyssalnite_boots")) {
            effect(player, MobEffects.MOVEMENT_SPEED, 20, 0);
        } else if (helmet && id.equals("plated_coralium_helmet")) {
            if (player.level().dimension().equals(Level.OVERWORLD) || ContentConfigMatrix.nightVisionEverywhere()) {
                effect(player, MobEffects.NIGHT_VISION, 260, 0);
            }
        } else if (!helmet && id.equals("plated_coralium_boots")) {
            effect(player, MobEffects.MOVEMENT_SPEED, 20, player.isInWater() ? 2 : 1);
            if (player.isInWater()) effect(player, MobEffects.WATER_BREATHING, 20, 1);
        }
    }

    private static void effect(ServerPlayer player, net.minecraft.world.effect.MobEffect effect,
                               int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
    }
}