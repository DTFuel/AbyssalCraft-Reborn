package com.shinoow.abyssalcraft.content.entity.ghoul;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.platform.IgniteCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;

/**
 * Shared base for the AbyssalCraft ghoul family (owned by PD-5, Stage D2a).
 *
 * <p>Faithful successor to 1.12.2 {@code common.entity.ghoul.EntityGhoulBase} ({@code extends
 * EntityMobBase} = {@link ACMob}). Ports the common ghoul melee AI set and the shared attribute
 * baseline (follow range 42, movement speed 0.23); concrete ghouls layer on their own health/damage
 * and any extra traits.
 *
 * <p>The shared goal and sound set lives here; concrete ghouls opt into legacy sunlight burning and
 * family-specific on-hit effects.
 */
public abstract class AbstractGhoul extends ACMob {

    protected AbstractGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0D));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Shared ghoul attribute baseline (follow range + movement speed); concrete ghouls add health/damage. */
    protected static AttributeSupplier.Builder ghoulAttributes() {
        return ACMob.createAttributes()
            .add(Attributes.FOLLOW_RANGE, 42.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23000000417232513D);
    }

    protected final void burnInSunlightWhenConfigured() {
        if (ACConfig.ghouls_burn.get() && isSunBurnTick()) IgniteCompat.ignite(this, 8);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event("ghoul.normal.idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("ghoul.hit");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("ghoul.death");
    }
}
