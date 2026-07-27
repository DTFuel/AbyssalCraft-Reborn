package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;

/** Compat: finalize a mob spawned by a block entity across the 1.20/1.21 signature change. */
public final class MobSpawnCompat {

    private MobSpawnCompat() {}

    public static void finalizeSpawnerSpawn(ServerLevel level, Mob mob) {
        //? if <1.21 {
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
            MobSpawnType.SPAWNER, null, null);
        //?} else {
        /*mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
            MobSpawnType.SPAWNER, null);
        *///?}
    }

    public static void finalizeTriggeredSpawn(ServerLevel level, Mob mob) {
        //? if <1.21 {
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
            MobSpawnType.TRIGGERED, null, null);
        //?} else {
        /*mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
            MobSpawnType.TRIGGERED, null);
        *///?}
    }

    /** Create and spawn one mob at a viable position within the legacy 16-block disruption radius. */
    public static boolean spawnNear(ServerLevel level, BlockPos origin, EntityType<? extends Mob> type) {
        Mob mob = type.create(level);
        if (mob == null) {
            return false;
        }
        BlockPos spawnPos = findSpawnPosition(level, origin);
        mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
            level.random.nextFloat() * 360.0F, 0.0F);
        finalizeSpawnerSpawn(level, mob);
        return level.addFreshEntity(mob);
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = origin.getX() + level.random.nextInt(32) - 16;
            int z = origin.getZ() + level.random.nextInt(32) - 16;
            int sampledY = origin.getY() + level.random.nextInt(32) - 16;
            BlockPos sampled = new BlockPos(x, sampledY, z);
            int y = level.canSeeSky(sampled)
                ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
                : sampledY;
            BlockPos candidate = new BlockPos(x, y, z);
            if (level.isEmptyBlock(candidate) && level.isEmptyBlock(candidate.above())
                && !level.isEmptyBlock(candidate.below())) {
                return candidate;
            }
        }
        return origin.above();
    }
}