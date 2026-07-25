package com.shinoow.abyssalcraft.content.entity.ai;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Reusable "worship" goal (ported from 1.12.2 {@code EntityAIWorship}, Stage D1 / PD-2).
 *
 * <p>A mob occasionally scans a cube around itself for a worship-target block -- identified by a
 * {@link TagKey} of blocks (e.g. the AbyssalCraft decorative statues) so this goal never hard-references
 * a specific block -- and, when one is found, walks up next to it, faces it, and (optionally) plays a
 * chant {@link SoundEvent}. Everything is parameterised (mob, target tag, speed, radius, sound), so any
 * {@link PathfinderMob} can reuse it; there is no vanilla equivalent.
 *
 * <p>Fork-free: every method used is identical across 1.20.1 and 1.21 (the loader/version split, if a
 * consumer ever needs one, would live in {@code platform/}).
 */
public class WorshipGoal extends Goal {

    private final PathfinderMob mob;
    private final TagKey<Block> worshipTargets;
    private final double speed;
    private final int searchRadius;
    @Nullable
    private final SoundEvent chant;

    @Nullable
    private BlockPos targetPos;
    private int worshipTime;

    public WorshipGoal(PathfinderMob mob, TagKey<Block> worshipTargets, double speed, int searchRadius, @Nullable SoundEvent chant) {
        this.mob = mob;
        this.worshipTargets = worshipTargets;
        this.speed = speed;
        this.searchRadius = searchRadius;
        this.chant = chant;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.getRandom().nextFloat() >= 0.01F) {
            return false;
        }
        BlockPos found = findWorshipTarget();
        if (found == null) {
            return false;
        }
        this.targetPos = found;
        return true;
    }

    @Nullable
    private BlockPos findWorshipTarget() {
        Level level = mob.level();
        BlockPos origin = mob.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (level.getBlockState(cursor).is(worshipTargets)) {
                        return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean canContinueToUse() {
        return worshipTime >= 0 && targetPos != null;
    }

    @Override
    public void start() {
        worshipTime = 20 + mob.getRandom().nextInt(20);
        if (targetPos != null) {
            int dx = mob.getRandom().nextBoolean() ? 1 : -1;
            int dz = mob.getRandom().nextBoolean() ? 1 : -1;
            mob.getNavigation().moveTo(targetPos.getX() + dx, targetPos.getY(), targetPos.getZ() + dz, speed);
            if (chant != null && !mob.level().isClientSide()) {
                mob.playSound(chant, 1.0F, (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    @Override
    public void stop() {
        targetPos = null;
    }

    @Override
    public void tick() {
        --worshipTime;
        if (targetPos != null) {
            mob.getLookControl().setLookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                mob.getMaxHeadYRot(), mob.getMaxHeadXRot());
        }
    }
}
