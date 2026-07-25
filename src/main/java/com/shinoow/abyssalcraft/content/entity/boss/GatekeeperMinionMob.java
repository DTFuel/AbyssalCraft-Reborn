package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.registry.ModSounds;

/** Gatekeeper minion that can rally nearby Remnants against its attacker. */
public final class GatekeeperMinionMob extends EliteMob {

    public GatekeeperMinionMob(EntityType<? extends Monster> type, Level level) {
        super(type, level, EliteKind.GATEKEEPER_MINION);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof LivingEntity attacker
                && getRandom().nextInt(10) == 0) {
            for (RemnantMob remnant : level().getEntitiesOfClass(RemnantMob.class,
                    getBoundingBox().inflate(16.0D), LivingEntity::isAlive)) {
                remnant.enrage(false, attacker);
            }
            playSound(ModSounds.event("remnant.scream"), 3.0F, 1.0F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("shadow.death");
    }
}