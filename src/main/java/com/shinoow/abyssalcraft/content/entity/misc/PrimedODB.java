package com.shinoow.abyssalcraft.content.entity.misc;

import java.util.UUID;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.platform.ACSimpleEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Primed Oblivion Deathbomb (1.12.2 {@code primedodb}) and its core variant ({@code primedodbcore}),
 * collapsed into one class. The full ODB uses the configured radius and a 200-tick fuse; the core uses
 * radius 16 and a 40-tick fuse. Large ellipsoid craters are processed in a fixed per-tick budget rather
 * than handed to vanilla's single-tick ray explosion. The full ODB summons Sacthoth when clearing ends.
 */
public class PrimedODB extends ACSimpleEntity {

    private static final int BLOCKS_PER_TICK = 4096;

    private final boolean core;
    private int fuse;
    private UUID ownerUuid;
    private boolean detonating;
    private boolean blastApplied;
    private int radius;
    private int scanX;
    private int scanY;
    private int scanZ;
    private int scanZMax;

    public PrimedODB(EntityType<?> type, Level level, boolean core) {
        super(type, level);
        this.core = core;
        this.fuse = core ? 40 : 200;
        float angle = random.nextFloat() * ((float) Math.PI * 2.0F);
        setDeltaMovement(-Math.sin(angle) * 0.02D, 0.2D, -Math.cos(angle) * 0.02D);
    }

    public int getFuse() {
        return fuse;
    }

    public int getInitialFuse() {
        return core ? 40 : 200;
    }

    public boolean isCore() {
        return core;
    }

    public PrimedODB setOwner(LivingEntity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (detonating) {
            if (!level().isClientSide) processCrater();
            return;
        }

        Vec3 velocity = getDeltaMovement().add(0.0D, -0.04D, 0.0D);
        move(MoverType.SELF, velocity);
        velocity = velocity.scale(0.98D);
        if (onGround()) velocity = new Vec3(velocity.x * 0.7D, velocity.y * -0.5D, velocity.z * 0.7D);
        setDeltaMovement(velocity);

        if (--fuse <= 0) {
            if (level().isClientSide || ACConfig.no_odb_explosions.get()) {
                discard();
                return;
            }
            beginDetonation();
        } else if (ACConfig.particleEntity.get() && level() instanceof ServerLevel server) {
            server.sendParticles(core ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.PORTAL,
                getX(), getY() + 0.5D, getZ(), 10, 0.0D, 0.0D, 0.0D, 0.1D);
        }
    }

    private void beginDetonation() {
        detonating = true;
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        radius = core ? 16 : ACConfig.odbExplosionSize.get();
        scanX = -radius;
        scanY = -(int) Math.ceil(radius * 0.6D);
        resetZRange();
        applyBlastToEntities();
    }

    private void applyBlastToEntities() {
        if (blastApplied) return;
        blastApplied = true;
        double reach = radius * 2.0D;
        AABB area = AABB.ofSize(position(), reach * 2.0D, reach * 2.0D, reach * 2.0D);
        Entity owner = owner();
        for (Entity entity : level().getEntities(this, area, Entity::isAlive)) {
            double normalized = Math.sqrt(distanceToSqr(entity)) / reach;
            if (normalized > 1.0D) continue;
            Vec3 direction = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D)
                .subtract(position()).normalize();
            double exposure = 1.0D - normalized;
            float damage = (float) ((exposure * exposure + exposure) * 3.5D * reach + 1.0D);
            entity.hurt(damageSources().explosion(this, owner), damage);
            entity.push(direction.x * exposure, direction.y * exposure, direction.z * exposure);
        }
    }

    private void processCrater() {
        if (!(level() instanceof ServerLevel server)) return;
        int processed = 0;
        int verticalRadius = (int) Math.ceil(radius * 0.6D);
        BlockPos center = blockPosition();
        while (processed < BLOCKS_PER_TICK && scanX <= radius) {
            if (scanZ <= scanZMax) {
                BlockPos pos = center.offset(scanX, scanY, scanZ++);
                var state = server.getBlockState(pos);
                if (!state.isAir() && state.getBlock().getExplosionResistance() < 600000.0F) {
                    server.destroyBlock(pos, false, this, 512);
                }
                processed++;
                continue;
            }
            scanY++;
            if (scanY > verticalRadius) {
                scanY = -verticalRadius;
                scanX++;
            }
            resetZRange();
        }
        if (scanX > radius) finishDetonation(server, center);
    }

    private void resetZRange() {
        double xNorm = scanX / (radius + 0.5D);
        double yNorm = scanY / (radius * 0.6D + 0.5D);
        double remaining = 1.0D - xNorm * xNorm - yNorm * yNorm;
        if (remaining < 0.0D) {
            scanZ = 1;
            scanZMax = 0;
        } else {
            scanZMax = (int) Math.floor((radius + 0.5D) * Math.sqrt(remaining));
            scanZ = -scanZMax;
        }
    }

    private void finishDetonation(ServerLevel server, BlockPos center) {
        if (!core) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos pos = center.offset(x, 0, z);
                    if (server.getBlockState(pos).getBlock().getExplosionResistance() < 600000.0F) {
                        server.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    }
                }
            }
            var sacthoth = BossEntities.SACTHOTH.get().create(server);
            if (sacthoth != null) {
                sacthoth.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                    getYRot(), getXRot());
                //? if <1.21 {
                sacthoth.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                    MobSpawnType.TRIGGERED, null, null);
                //?} else {
                /*sacthoth.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                    MobSpawnType.TRIGGERED, null);
                *///?}
                server.addFreshEntity(sacthoth);
            }
        }
        discard();
    }

    private Entity owner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel server)) return null;
        return server.getEntity(ownerUuid);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("Fuse", (short) fuse);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putBoolean("Detonating", detonating);
        tag.putBoolean("BlastApplied", blastApplied);
        tag.putInt("Radius", radius);
        tag.putInt("ScanX", scanX);
        tag.putInt("ScanY", scanY);
        tag.putInt("ScanZ", scanZ);
        tag.putInt("ScanZMax", scanZMax);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        fuse = tag.getShort("Fuse");
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        detonating = tag.getBoolean("Detonating");
        blastApplied = tag.getBoolean("BlastApplied");
        radius = tag.getInt("Radius");
        scanX = tag.getInt("ScanX");
        scanY = tag.getInt("ScanY");
        scanZ = tag.getInt("ScanZ");
        scanZMax = tag.getInt("ScanZMax");
        if (detonating) {
            noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
        }
    }
}
