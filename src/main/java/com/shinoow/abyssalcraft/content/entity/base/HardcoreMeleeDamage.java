package com.shinoow.abyssalcraft.content.entity.base;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;

public final class HardcoreMeleeDamage {

    private HardcoreMeleeDamage() {}

    public static void applyChip(Mob attacker, Entity target, float baseDamage) {
        if (baseDamage <= 0.0F || attacker.level().isClientSide || !ACConfig.hardcoreMode.get()
            || !(target instanceof Player)) return;
        target.hurt(ACDamageTypes.attributedSource(attacker, ACDamageTypes.DREAD),
            scaledDamage(baseDamage, ACConfig.damageAmpl.get()));
    }

    public static float scaledDamage(float baseDamage, double amplifier) {
        return (float) (baseDamage * Math.max(amplifier, 1.0D));
    }
}