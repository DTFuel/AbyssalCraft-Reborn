package com.shinoow.abyssalcraft.common.handlers;

import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

/** Shared Purged-biome checks for effect and interaction hooks. */
public final class PurgeHooks {

    private static final ResourceKey<Biome> PURGED = ResourceKey.create(Registries.BIOME, ACRef.id("purged"));

    private PurgeHooks() {}

    public static boolean isPurged(LivingEntity entity) {
        return isPurged(entity.level(), entity.blockPosition());
    }

    public static boolean isPurged(LevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(PURGED);
    }
}