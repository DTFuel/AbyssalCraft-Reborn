package com.shinoow.abyssalcraft.content.entity.ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.world.feature.MonolithFeature;

/** Four-Shoggoth cooperative goal that constructs a monolith after the configured cooldown. */
public final class ShoggothBuildMonolithGoal extends Goal {

    private static final int REQUIRED_ASSISTANTS = 3;
    private final AbstractShoggoth shoggoth;
    private final List<AbstractShoggoth> assistants = new ArrayList<>(REQUIRED_ASSISTANTS);

    public ShoggothBuildMonolithGoal(AbstractShoggoth shoggoth) {
        this.shoggoth = shoggoth;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(shoggoth.level() instanceof ServerLevel level)) return false;
        if (shoggoth.isAssistingMonolith()) {
            if (resolveLeader(level) != null) return true;
            shoggoth.clearMonolithBuildState();
            return false;
        }
        if (shoggoth.isBuildingMonolith()) {
            if (restoreAssistants(level)) return true;
            releaseGroup();
            return false;
        }
        int cooldown = ACConfig.monolithBuildingCooldown.get();
        if (cooldown <= 0 || shoggoth.getMonolithTimer() < cooldown) return false;
        BlockPos target = findPossibleLocation(level);
        if (target == null || !claimAssistants(level)) return false;
        shoggoth.beginMonolithBuild(target, 400);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (shoggoth.isAssistingMonolith()) {
            return shoggoth.level() instanceof ServerLevel level && resolveLeader(level) != null;
        }
        return shoggoth.isBuildingMonolith() && shoggoth.getBuildAttemptTicks() > 0
            && shoggoth.getMonolithTarget() != null && assistantsStillValid();
    }

    @Override
    public void start() {
        if (shoggoth.isAssistingMonolith()) return;
        moveGroupToTarget();
    }

    @Override
    public void tick() {
        if (shoggoth.isAssistingMonolith()) {
            tickAssistant();
            return;
        }
        BlockPos target = shoggoth.getMonolithTarget();
        if (target == null || !(shoggoth.level() instanceof ServerLevel level)) return;
        shoggoth.decrementBuildAttemptTicks();
        moveGroupToTarget();
        if (shoggoth.distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D) <= 64.0D
                && allAssistantsNearby()) {
            MonolithFeature.placeMonolith(level, target, shoggoth.getRandom(),
                BaseBlocks.MONOLITH_STONE.get().defaultBlockState());
            shoggoth.resetMonolithTimer();
            moveNearbyShoggothsAway(level, target);
            releaseGroup();
        }
    }

    @Override
    public void stop() {
        if (shoggoth.isBuildingMonolith()) {
            releaseGroup();
        } else {
            assistants.clear();
            if (shoggoth.isAssistingMonolith()) shoggoth.clearMonolithBuildState();
        }
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    private boolean claimAssistants(ServerLevel level) {
        assistants.clear();
        for (AbstractShoggoth candidate : nearbySameKind(level, 16.0D)) {
            if (candidate == shoggoth || candidate.isBuildingMonolith() || candidate.isAssistingMonolith()
                    || candidate.distanceTo(shoggoth) > 12.0F) continue;
            assistants.add(candidate);
            if (assistants.size() == REQUIRED_ASSISTANTS) break;
        }
        if (assistants.size() != REQUIRED_ASSISTANTS) {
            assistants.clear();
            return false;
        }
        for (AbstractShoggoth assistant : assistants) {
            assistant.reduceMonolithTimer();
            assistant.assistMonolithBuild(shoggoth.getUUID());
        }
        return true;
    }

    private boolean restoreAssistants(ServerLevel level) {
        assistants.clear();
        for (AbstractShoggoth candidate : nearbySameKind(level, 16.0D)) {
            if (candidate.isAssistingMonolith() && shoggoth.getUUID().equals(candidate.getMonolithLeader())) {
                assistants.add(candidate);
                if (assistants.size() == REQUIRED_ASSISTANTS) break;
            }
        }
        return assistants.size() == REQUIRED_ASSISTANTS;
    }

    private List<AbstractShoggoth> nearbySameKind(ServerLevel level, double range) {
        return level.getEntitiesOfClass(AbstractShoggoth.class, shoggoth.getBoundingBox().inflate(range),
            candidate -> candidate.getClass() == shoggoth.getClass());
    }

    private void moveGroupToTarget() {
        BlockPos target = shoggoth.getMonolithTarget();
        if (target == null) return;
        shoggoth.getNavigation().moveTo(target.getX() + (shoggoth.getRandom().nextBoolean() ? 3 : -3),
            target.getY(), target.getZ() + (shoggoth.getRandom().nextBoolean() ? 3 : -3), 0.38D);
        for (AbstractShoggoth assistant : assistants) {
            assistant.getNavigation().moveTo(shoggoth, 0.38D);
        }
    }

    private void tickAssistant() {
        if (!(shoggoth.level() instanceof ServerLevel level)) return;
        AbstractShoggoth leader = resolveLeader(level);
        if (leader == null) {
            shoggoth.clearMonolithBuildState();
            return;
        }
        shoggoth.getNavigation().moveTo(leader, 0.38D);
        shoggoth.getLookControl().setLookAt(leader, 30.0F, 30.0F);
    }

    private AbstractShoggoth resolveLeader(ServerLevel level) {
        if (shoggoth.getMonolithLeader() == null) return null;
        return level.getEntity(shoggoth.getMonolithLeader()) instanceof AbstractShoggoth leader
            && leader.isBuildingMonolith() ? leader : null;
    }

    private boolean assistantsStillValid() {
        return assistants.size() == REQUIRED_ASSISTANTS
            && assistants.stream().allMatch(assistant -> assistant.isAlive() && assistant.isAssistingMonolith()
                && shoggoth.getUUID().equals(assistant.getMonolithLeader()));
    }

    private boolean allAssistantsNearby() {
        return assistants.stream().allMatch(assistant -> assistant.distanceToSqr(shoggoth) <= 64.0D);
    }

    private void releaseGroup() {
        for (AbstractShoggoth assistant : assistants) assistant.clearMonolithBuildState();
        assistants.clear();
        shoggoth.clearMonolithBuildState();
    }

    private void moveNearbyShoggothsAway(ServerLevel level, BlockPos target) {
        AABB area = new AABB(target).inflate(32.0D);
        for (AbstractShoggoth nearby : level.getEntitiesOfClass(AbstractShoggoth.class, area,
                candidate -> candidate.distanceToSqr(target.getX(), target.getY(), target.getZ()) <= 25.0D)) {
            nearby.getNavigation().moveTo(target.getX() + (nearby.getRandom().nextBoolean() ? 7 : -7),
                nearby.getY(), target.getZ() + (nearby.getRandom().nextBoolean() ? 7 : -7), 0.7D);
        }
    }

    private BlockPos findPossibleLocation(ServerLevel level) {
        BlockPos origin = shoggoth.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos candidate = origin.offset(shoggoth.getRandom().nextInt(20) - 10,
                shoggoth.getRandom().nextInt(6) - 3, shoggoth.getRandom().nextInt(20) - 10);
            BlockState target = level.getBlockState(candidate);
            BlockState below = level.getBlockState(candidate.below());
            if (!level.isEmptyBlock(candidate.above()) || (!target.isAir() && !target.canBeReplaced())
                    || below.is(BaseBlocks.MONOLITH_STONE.get()) || below.is(ShoggothBlocks.SHOGGOTH_BIOMASS.get())
                    || below.isAir() || !below.isFaceSturdy(level, candidate.below(), Direction.UP)) continue;
            for (BlockPos nearby : BlockPos.betweenClosed(candidate.offset(-1, -1, -1),
                    candidate.offset(1, 1, 1))) {
                if (level.getBlockState(nearby).is(ShoggothBlocks.SHOGGOTH_OOZE.get())) {
                    return nearby.below().getY() == candidate.getY() && level.getBlockState(nearby.below())
                        .is(ShoggothBlocks.SHOGGOTH_OOZE.get()) ? nearby.below() : nearby.immutable();
                }
            }
            return candidate;
        }
        return null;
    }
}