package com.shinoow.abyssalcraft.content.entity.legacy;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import com.shinoow.abyssalcraft.content.entity.projectile.InkProjectile;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

public final class CoraliumSquid extends Squid implements RangedAttackMob {

    public CoraliumSquid(EntityType<? extends Squid> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.6D, 80, 8.0F));
    }

    @Override
    public void push(Entity entity) {
        if (!level().isClientSide && entity instanceof LivingEntity living && !EffectHooks.isCoraliumImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
        }
        super.push(entity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        LivingEntity target = getTarget();
        if (target == null) {
            Player player = level().getNearestPlayer(this, 7.0D);
            if (player != null && !player.isCreative() && !player.isSpectator()) setTarget(player);
        } else if (distanceToSqr(target) > 64.0D || !target.isAlive()) {
            setTarget(null);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        InkProjectile ink = ProjectileEntities.INK_PROJECTILE.get().create(level());
        if (ink == null) return;
        ink.setOwner(this);
        ink.moveTo(getX(), getEyeY() - 0.1D, getZ(), getYRot(), getXRot());
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - ink.getY() + Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2D;
        ink.shoot(dx, dy, dz, 1.6F, 12.0F);
        playSound(SoundEvents.SQUID_AMBIENT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(ink);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return getY() > 45.0D && getY() < level.getSeaLevel() && super.checkSpawnRules(level, spawnType);
    }
}