package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.content.entity.pathfinding.ACWallClimberNavigation;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/** Wall-climbing Spawn of Cha'garoth with Dread Plague melee. */
public final class ChagarothSpawnMob extends EliteMob {

    public ChagarothSpawnMob(EntityType<? extends Monster> type, Level level) {
        super(type, level, EliteKind.CHAGAROTH_SPAWN);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ACWallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !EffectHooks.isDreadImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event("dreadspawn.idle");
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return ModSounds.event("dreadspawn.hit");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("dreadspawn.death");
    }
}