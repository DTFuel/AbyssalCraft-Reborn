package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/** Dreadguard's persistent sixty-tick Dread breath attack. */
public final class DreadguardMob extends EliteMob {

    private static final EntityDataAccessor<Integer> BARF_TIMER =
        SynchedEntityData.defineId(DreadguardMob.class, EntityDataSerializers.INT);

    public DreadguardMob(EntityType<? extends Monster> type, Level level) {
        super(type, level, EliteKind.DREADGUARD);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BARF_TIMER, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BARF_TIMER, 0);
    }
    *///?}

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int timer = entityData.get(BARF_TIMER);
        LivingEntity attackTarget = getTarget();
        if (attackTarget != null && attackTarget.isAlive() && distanceToSqr(attackTarget) <= 64.0D
                && timer <= -200) timer = 60;
        if (timer > 0) {
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            level().broadcastEntityEvent(this, (byte) 23);
            if (tickCount % 5 == 0 && timer > 30) {
                playSound(ModSounds.event("dreadguard.barf"), 0.7F + getRandom().nextFloat(),
                    0.2F + getRandom().nextFloat() * 0.5F);
            }
            LivingEntity target = findBreathTarget();
            if (target != null) applyBreath(target);
        }
        entityData.set(BARF_TIMER, timer - 1);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BarfTimer", entityData.get(BARF_TIMER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(BARF_TIMER, tag.getInt("BarfTimer"));
    }

    private LivingEntity findBreathTarget() {
        double range = 4.0D + getRandom().nextDouble() * 8.0D;
        Vec3 start = getEyePosition();
        Vec3 intendedEnd = start.add(getLookAngle().scale(range));
        HitResult blockHit = level().clip(new ClipContext(start, intendedEnd, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, this));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? intendedEnd : blockHit.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(this, start, end,
            getBoundingBox().expandTowards(end.subtract(start)).inflate(4.0D),
            entity -> entity instanceof LivingEntity living && living.isAlive() && living != this
                && !EffectHooks.isDreadImmune(living), start.distanceToSqr(end));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private void applyBreath(LivingEntity target) {
        for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(2.0D), living -> living.isAlive()
                    && !EffectHooks.isDreadImmune(living))) {
            if (nearby != target && getRandom().nextInt(3) != 0) continue;
            float damage = Math.max(0.0F, (float) (4.5D - distanceTo(nearby)));
            if (damage > 0.0F && nearby.hurt(ACDamageTypes.source(this, ACDamageTypes.DREAD), damage)) {
                nearby.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
            }
        }
    }
}