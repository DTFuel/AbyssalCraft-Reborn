package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.UUID;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.behavior.EldritchEntities;
import com.shinoow.abyssalcraft.platform.ACSimpleEntity;
import com.shinoow.abyssalcraft.registry.ModSounds;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Implosion (1.12.2 {@code implosion}), another of Jzahar's attacks: a 360-tick gravity well that
 * accelerates inward before its final outward blast, excluding all eldritch entities.
 */
public class Implosion extends ACSimpleEntity {

    private UUID ownerUuid;

    public Implosion(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public Implosion setOwner(LivingEntity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount == 1) playSound(ModSounds.event("jzahar.charge"), 5.0F, 1.0F);
        if (tickCount > 360) {
            collapse();
            discard();
            return;
        }
        for (LivingEntity target : targets()) {
            Vec3 direction = target.position().subtract(position()).normalize();
            double scale = Math.max(0.0D, (64.0D - target.distanceTo(this)) / 64.0D);
            target.push(direction.x * -tickCount * 0.0005D * scale,
                direction.y * -tickCount * 0.0005D * scale,
                direction.z * -tickCount * 0.0005D * scale);
            if (target.distanceToSqr(this) <= 4.0D) {
                target.hurt(damageSources().lightningBolt(), ACConfig.hardcoreMode.get() ? 8.0F : 4.0F);
            }
        }
    }

    private java.util.List<LivingEntity> targets() {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(64.0D),
            entity -> entity.isAlive() && !EldritchEntities.isEldritch(entity));
    }

    private void collapse() {
        if (level() instanceof ServerLevel server) server.levelEvent(null, 3000, blockPosition(), 0);
        playSound(ModSounds.event("jzahar.blast"), 5.0F, 1.0F);
        for (LivingEntity target : targets()) {
            Vec3 direction = target.position().subtract(position()).normalize();
            double scale = Math.max(0.0D, (64.0D - target.distanceTo(this)) / 64.0D);
            if (ACConfig.hardcoreMode.get() && target.distanceToSqr(this) <= 25.0D) {
                target.invulnerableTime = 0;
                target.hurt(damageSources().lightningBolt(), 100.0F);
            }
            target.push(direction.x * 2.5D * scale,
                1.5D + random.nextDouble(), direction.z * 2.5D * scale);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
}
