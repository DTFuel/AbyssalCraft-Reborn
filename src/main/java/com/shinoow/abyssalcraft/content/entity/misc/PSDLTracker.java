package com.shinoow.abyssalcraft.content.entity.misc;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.item.misc.MiscItems;
import com.shinoow.abyssalcraft.platform.ACSimpleEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Powerstone tracker (1.12.2 {@code powerstonetracker}), the eye-of-ender-like locator fired to hunt
 * the Power Stone of the Depths Lord. It flies toward a target segment for 80 ticks, then has the
 * legacy 80% chance to drop itself and 20% chance to shatter.
 */
public class PSDLTracker extends ACSimpleEntity {

    private double targetX;
    private double targetY;
    private double targetZ;
    private int despawnTimer;
    private boolean dropAtEnd;
    private boolean hasTarget;

    public PSDLTracker(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public void moveTowards(BlockPos target) {
        double deltaX = target.getX() - getX();
        double deltaZ = target.getZ() - getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (horizontal > 12.0D) {
            targetX = getX() + deltaX / horizontal * 12.0D;
            targetY = getY() + 8.0D;
            targetZ = getZ() + deltaZ / horizontal * 12.0D;
        } else {
            targetX = target.getX();
            targetY = target.getY();
            targetZ = target.getZ();
        }
        despawnTimer = 0;
        dropAtEnd = random.nextInt(5) > 0;
        hasTarget = true;
    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, getDeltaMovement());
        double horizontalSpeed = getDeltaMovement().horizontalDistance();
        setYRot((float) (Mth.atan2(getDeltaMovement().x, getDeltaMovement().z) * 180.0D / Math.PI));
        setXRot((float) (Mth.atan2(getDeltaMovement().y, horizontalSpeed) * 180.0D / Math.PI));

        if (!level().isClientSide && hasTarget) {
            double deltaX = targetX - getX();
            double deltaZ = targetZ - getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double speed = horizontalSpeed + (distance - horizontalSpeed) * 0.0025D;
            if (distance < 1.0D) {
                speed *= 0.8D;
                setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.8D, 1.0D));
            }
            double angle = Mth.atan2(deltaZ, deltaX);
            double vertical = getDeltaMovement().y
                + ((getY() < targetY ? 1.0D : -1.0D) - getDeltaMovement().y) * 0.015D;
            setDeltaMovement(Math.cos(angle) * speed, vertical, Math.sin(angle) * speed);
        }

        if (ACConfig.particleEntity.get()) {
            var particle = isInWater() ? ParticleTypes.BUBBLE : ParticleTypes.SMOKE;
            level().addParticle(particle, getX() - getDeltaMovement().x * 0.25D,
                getY() - getDeltaMovement().y * 0.25D - 0.5D,
                getZ() - getDeltaMovement().z * 0.25D,
                getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
        }

        if (!level().isClientSide && ++despawnTimer > 80) {
            if (hasTarget && dropAtEnd) spawnAtLocation(MiscItems.POWERSTONE_TRACKER.get());
            else if (hasTarget) level().levelEvent(null, 2003, blockPosition(), 0);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("TargetX", targetX);
        tag.putDouble("TargetY", targetY);
        tag.putDouble("TargetZ", targetZ);
        tag.putInt("DespawnTimer", despawnTimer);
        tag.putBoolean("DropAtEnd", dropAtEnd);
        tag.putBoolean("HasTarget", hasTarget);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        targetX = tag.getDouble("TargetX");
        targetY = tag.getDouble("TargetY");
        targetZ = tag.getDouble("TargetZ");
        despawnTimer = tag.getInt("DespawnTimer");
        dropAtEnd = tag.getBoolean("DropAtEnd");
        hasTarget = tag.getBoolean("HasTarget");
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
