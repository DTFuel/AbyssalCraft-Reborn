package com.shinoow.abyssalcraft.content.item.armor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ContentConfigMatrix;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;

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
            player.addEffect(MobEffectCompat.vanillaEffect(MobEffects.WATER_BREATHING, 20, 0));
        } else if (!helmet && id.equals("abyssalnite_boots")) {
            player.addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SPEED, 20, 0));
        } else if (helmet && id.equals("plated_coralium_helmet")) {
            if (player.level().dimension().equals(Level.OVERWORLD) || ContentConfigMatrix.nightVisionEverywhere()) {
                player.addEffect(MobEffectCompat.vanillaEffect(MobEffects.NIGHT_VISION, 260, 0));
            }
        } else if (!helmet && id.equals("plated_coralium_boots")) {
            player.addEffect(MobEffectCompat.vanillaEffect(
                MobEffects.MOVEMENT_SPEED, 20, player.isInWater() ? 2 : 1));
            if (player.isInWater()) {
                player.addEffect(MobEffectCompat.vanillaEffect(MobEffects.WATER_BREATHING, 20, 1));
            }
        }
    }
}