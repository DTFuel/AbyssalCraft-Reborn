package com.shinoow.abyssalcraft.content.entity.behavior;

import java.time.LocalDate;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.shinoow.abyssalcraft.config.ACConfig;

/** Shared legacy shadow particles and Halloween head equipment. */
public final class ShadowEntityEffects {

    private ShadowEntityEffects() {}

    public static void tickParticles(LivingEntity entity) {
        if (!entity.level().isClientSide || !ACConfig.particleEntity.get()) return;
        int count = Math.max(1, (int) (2.0F * Math.max(0.0F, entity.getLightLevelDependentMagicValue())));
        for (int index = 0; index < count; index++) {
            entity.level().addParticle(ParticleTypes.SMOKE,
                entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth(),
                entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight(),
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth(),
                0.0D, 0.0D, 0.0D);
        }
    }

    public static void equipHalloweenHead(LivingEntity entity) {
        if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || entity.getRandom().nextFloat() >= 0.25F) return;
        LocalDate date = LocalDate.now();
        if (date.getMonthValue() != 10 || date.getDayOfMonth() != 31) return;
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(
            entity.getRandom().nextFloat() < 0.1F ? Items.JACK_O_LANTERN : Items.CARVED_PUMPKIN));
        if (entity instanceof net.minecraft.world.entity.Mob mob) mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
    }
}