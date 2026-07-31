package com.shinoow.abyssalcraft.content.block.shoggoth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.content.entity.shoggoth.ShoggothEntities;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.world.ACDimensions;

/** Persistent implementation of the original Shoggoth biomass spawning cycle. */
public final class ShoggothBiomassBlockEntity extends BlockEntityCompat {

    private int cooldown;
    private int spawnedShoggoths;

    public ShoggothBiomassBlockEntity(BlockPos pos, BlockState state) {
        super(ShoggothBlocks.SHOGGOTH_BIOMASS_BE.get(), pos, state);
    }

    /** Sets the deterministic structure-placement delay used by legacy biomass markers. */
    public void setInitialCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
        setChanged();
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state,
                                  ShoggothBiomassBlockEntity biomass) {
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        biomass.cooldown++;
        biomass.setChanged();
        if (biomass.cooldown < ACConfig.biomassCooldown.get()) return;

        biomass.cooldown = level.random.nextInt(10);
        biomass.resetNearbyBiomass(true);
        if (level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(),
                ACConfig.biomassPlayerDistance.get(), false) == null) return;
        double range = ACConfig.biomassShoggothDistance.get();
        if (level.getEntitiesOfClass(AbstractShoggoth.class, new AABB(pos).inflate(range)).size()
                >= ACConfig.biomassMaxSpawn.get()) return;

        AbstractShoggoth shoggoth = biomass.createShoggoth(level);
        if (shoggoth == null) return;
        biomass.setPosition(level, shoggoth);
        MobSpawnCompat.finalizeSpawnerSpawn(level, shoggoth);
        if (!level.addFreshEntity(shoggoth)) return;
        if (++biomass.spawnedShoggoths >= 5) {
            level.setBlock(pos, BaseBlocks.MONOLITH_STONE.get().defaultBlockState(), 2);
        } else {
            biomass.setChanged();
        }
    }

    private void resetNearbyBiomass(boolean recurse) {
        if (!(level instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX(), worldPosition.getY(),
            worldPosition.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (server.getBlockEntity(worldPosition.offset(dx, 0, dz))
                        instanceof ShoggothBiomassBlockEntity nearby) {
                    nearby.cooldown = server.random.nextInt(30);
                    nearby.setChanged();
                    if (recurse) nearby.resetNearbyBiomass(false);
                }
            }
        }
    }

    private AbstractShoggoth createShoggoth(ServerLevel level) {
        EntityType<? extends AbstractShoggoth> type = ShoggothEntities.LESSER_SHOGGOTH.get();
        if (level.random.nextBoolean() && isAbyssalDimension(level)) {
            type = level.random.nextInt(100) == 0
                ? ShoggothEntities.GREATER_SHOGGOTH.get()
                : ShoggothEntities.SHOGGOTH.get();
        }
        return type.create(level);
    }

    private static boolean isAbyssalDimension(ServerLevel level) {
        return level.dimension() == ACDimensions.ABYSSAL_WASTELAND
            || level.dimension() == ACDimensions.DREADLANDS
            || level.dimension() == ACDimensions.OMOTHOL
            || level.dimension() == ACDimensions.DARK_REALM;
    }

    private void setPosition(ServerLevel level, LivingEntity entity) {
        BlockPos origin = worldPosition;
        if (level.isEmptyBlock(origin.above()) && level.isEmptyBlock(origin.above(2))) {
            moveTo(entity, origin.above());
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = origin.offset(dx, 1, dz);
                if (isNonSolid(level, candidate)) {
                    moveTo(entity, candidate);
                    return;
                }
            }
        }
        for (int dx = -4; dx <= 4; dx += 4) {
            for (int dz = -4; dz <= 4; dz += 4) {
                BlockPos candidate = origin.offset(dx, 1, dz);
                if (isNonSolid(level, candidate)) {
                    moveTo(entity, candidate);
                    return;
                }
            }
        }
        if (isNonSolid(level, origin.above(2))) {
            moveTo(entity, origin.above(2));
            return;
        }
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 100));
        moveTo(entity, origin.above(isNonSolid(level, origin.above(15)) ? 15 : 20));
    }

    private static boolean isNonSolid(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static void moveTo(LivingEntity entity, BlockPos pos) {
        entity.moveTo(pos.getX(), pos.getY(), pos.getZ(), entity.getYRot(), entity.getXRot());
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Cooldown", cooldown);
        tag.putInt("SpawnedShoggoths", spawnedShoggoths);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        cooldown = Math.max(0, tag.getInt("Cooldown"));
        spawnedShoggoths = Math.max(0, tag.getInt("SpawnedShoggoths"));
    }
}
