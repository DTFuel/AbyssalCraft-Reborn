package com.shinoow.abyssalcraft.content.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Reusable creeper-style swell goal (ported from 1.12.2 {@code EntityAIAntiCreeperSwell}, Stage D1 /
 * PD-2). Vanilla's {@code SwellGoal} only accepts a {@code Creeper}; this generic version works on any
 * {@link Mob} that also implements {@link SwellingMob}, so AbyssalCraft's anti-creeper (a non-vanilla
 * base) -- or any other custom swelling mob -- can reuse it.
 *
 * <p>The mob starts/keeps swelling while its target is within ~3 blocks and, once engaged, while the
 * target stays within ~7 blocks and in line of sight; otherwise the swell winds back down. Fork-free.
 *
 * @param <T> the swelling mob type (a {@link Mob} implementing {@link SwellingMob})
 */
public class SwellGoal<T extends Mob & SwellingMob> extends Goal {

    private final T swellingMob;
    private LivingEntity target;

    public SwellGoal(T swellingMob) {
        this.swellingMob = swellingMob;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity current = swellingMob.getTarget();
        return swellingMob.getSwellState() > 0
            || (current != null && swellingMob.distanceToSqr(current) < 9.0D);
    }

    @Override
    public void start() {
        swellingMob.getNavigation().stop();
        this.target = swellingMob.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null
            || swellingMob.distanceToSqr(target) > 49.0D
            || !swellingMob.getSensing().hasLineOfSight(target)) {
            swellingMob.setSwellState(-1);
        } else {
            swellingMob.setSwellState(1);
        }
    }
}
