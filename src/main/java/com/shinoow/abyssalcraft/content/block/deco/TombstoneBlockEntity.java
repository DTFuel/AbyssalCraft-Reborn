package com.shinoow.abyssalcraft.content.block.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.shinoow.abyssalcraft.config.ContentConfigMatrix;
import com.shinoow.abyssalcraft.content.entity.ghoul.AbstractGhoul;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.platform.BlockEntityCompat;

/** Persistent server-side cooldown and ghoul spawning for all legacy tombstones. */
public final class TombstoneBlockEntity extends BlockEntityCompat {

    private int timer;

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(DecoBlocks.TOMBSTONE_BE.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state,
                                  TombstoneBlockEntity tombstone) {
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) || level.isDay()) return;
        if (++tombstone.timer < ContentConfigMatrix.tombstoneCooldown()) return;
        tombstone.timer = 0;
        int distance = ContentConfigMatrix.tombstoneGhoulDistance();
        if (level.getEntitiesOfClass(AbstractGhoul.class, new AABB(pos).inflate(distance)).size()
                >= ContentConfigMatrix.tombstoneMaxSpawn()) return;
        EntityType<? extends Mob> type = ghoulType(state.getBlock());
        Mob ghoul = type.create(level);
        BlockPos spawn = findSpawn(level, pos);
        if (ghoul == null || spawn == null) return;
        ghoul.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
            level.random.nextFloat() * 360.0F, 0.0F);
        ghoul.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.TRIGGERED, null, null);
        level.addFreshEntity(ghoul);
    }

    private static BlockPos findSpawn(ServerLevel level, BlockPos pos) {
        for (int radius : new int[]{0, 1, 4}) {
            for (int x = -radius; x <= radius; x += Math.max(1, radius * 2)) {
                for (int z = -radius; z <= radius; z += Math.max(1, radius * 2)) {
                    BlockPos candidate = pos.offset(x, 1, z);
                    if (level.getBlockState(candidate).isAir() && level.getBlockState(candidate.above()).isAir()) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static EntityType<? extends Mob> ghoulType(Block block) {
        if (block == DecoBlocks.TOMBSTONE_ABYSSAL_STONE.get()
                || block == DecoBlocks.TOMBSTONE_CORALIUM_STONE.get()) return GhoulEntities.DEPTHS_GHOUL.get();
        if (block == DecoBlocks.TOMBSTONE_DARKSTONE.get()) return GhoulEntities.SHADOW_GHOUL.get();
        if (block == DecoBlocks.TOMBSTONE_DREADSTONE.get()
                || block == DecoBlocks.TOMBSTONE_ELYSIAN_STONE.get()) return GhoulEntities.DREADED_GHOUL.get();
        if (block == DecoBlocks.TOMBSTONE_ETHAXIUM.get()
                || block == DecoBlocks.TOMBSTONE_OMOTHOL_STONE.get()) return GhoulEntities.OMOTHOL_GHOUL.get();
        return GhoulEntities.GHOUL.get();
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Timer", timer);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        timer = Math.max(0, tag.getInt("Timer"));
    }
}