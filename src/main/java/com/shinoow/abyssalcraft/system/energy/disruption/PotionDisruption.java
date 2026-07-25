package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A potion disruption (pilot, owned by PS-9): applies an effect to every living entity within ~16 blocks.
 * Faithful to the 1.12.2 {@code DisruptionPotion}. The effect is supplied by a factory so a fresh
 * {@link MobEffectInstance} is applied per entity; a vanilla effect factory
 * ({@code () -> new MobEffectInstance(MobEffects.POISON, 600)}) is fork-free (the constant and the constructor
 * parameter co-vary between 1.20.1 and 1.21.1).
 */
public final class PotionDisruption extends Disruption {

    private final Supplier<MobEffectInstance> effect;

    public PotionDisruption(String name, DeityType deity, Supplier<MobEffectInstance> effect) {
        super(name, deity);
        this.effect = effect;
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        AABB area = new AABB(pos).inflate(16);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            entity.addEffect(effect.get());
        }
    }
}
