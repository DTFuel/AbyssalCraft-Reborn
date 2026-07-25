package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;

/** Sacthoth's climbing, blindness aura, tracking teleport and shadow-breath state machine. */
public final class SacthothBoss extends BossMob {

    private int breathTimer;

    public SacthothBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossKind.SACTHOTH);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        updateBossBarColor();
        regenerate();
        if (getACDeathTime() > 0) return;
        applyBlindnessAura();
        trackDistantPlayer();
        tickShadowBreath();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60));
        }
        return hurt;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            teleportRandomly();
            return false;
        }
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)
                || source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) && getY() <= 0.0D && !level().isClientSide) {
            discard();
            return false;
        }
        if (amount > 30.0F) amount = 10.0F + getRandom().nextInt(10);
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BreathTimer", breathTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        breathTimer = tag.getInt("BreathTimer");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("sacthoth.death");
    }

    @Override
    protected void tickACDeath(int deathTick) {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel server)) return;
        if (deathTick == 100) spawnShadow(server, LegacyEntities.SHADOW_CREATURE.get());
        if (deathTick == 160) spawnShadow(server, LegacyEntities.SHADOW_MONSTER.get());
        if (deathTick == 200) spawnShadow(server, LegacyEntities.SHADOW_BEAST.get());
    }

    private void spawnShadow(net.minecraft.server.level.ServerLevel level,
                             EntityType<? extends net.minecraft.world.entity.Mob> type) {
        net.minecraft.world.entity.Mob shadow = type.create(level);
        if (shadow == null) return;
        shadow.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        MobSpawnCompat.finalizeSpawnerSpawn(level, shadow);
        level.addFreshEntity(shadow);
    }

    private void updateBossBarColor() {
        float fraction = getHealth() / getMaxHealth();
        setBossBarColor(fraction > 0.75F ? BossEvent.BossBarColor.BLUE
            : fraction > 0.5F ? BossEvent.BossBarColor.GREEN
            : fraction > 0.25F ? BossEvent.BossBarColor.YELLOW
            : BossEvent.BossBarColor.RED);
    }

    private void regenerate() {
        int pace = ACConfig.sacthothHealingPace.get();
        int amount = ACConfig.sacthothHealingAmount.get();
        if (amount > 0 && pace > 0 && tickCount % pace == 0) heal(amount);
    }

    private void applyBlindnessAura() {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(30.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator())) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40));
        }
    }

    private void trackDistantPlayer() {
        Player player = level().getNearestPlayer(this, 160.0D);
        if (player == null || player.isCreative() || player.isSpectator() || distanceTo(player) < 50.0F) return;
        double dx = player.getX() - getX();
        double dy = player.getY() - getY();
        double dz = player.getZ() - getZ();
        if (dx > 50.0D) attemptTeleport(player.getX() + 30.0D, player.getY(), player.getZ());
        else if (dx < -50.0D) attemptTeleport(player.getX() - 30.0D, player.getY(), player.getZ());
        else if (dz > 50.0D) attemptTeleport(player.getX(), player.getY(), player.getZ() - 30.0D);
        else if (dz < -50.0D) attemptTeleport(player.getX(), player.getY(), player.getZ() + 30.0D);
        else if (Math.abs(dy) > 50.0D) attemptTeleport(player.getX(), player.getY(), player.getZ());
    }

    private void tickShadowBreath() {
        LivingEntity attackTarget = getTarget();
        if (attackTarget != null && attackTarget.isAlive() && distanceToSqr(attackTarget) <= 64.0D
                && breathTimer <= -300) {
            breathTimer = 100;
        }
        if (breathTimer > 0) {
            setDeltaMovement(getDeltaMovement().multiply(0.05D, 1.0D, 0.05D));
            level().broadcastEntityEvent(this, (byte) 23);
            if (tickCount % 5 == 0) {
                playSound(SoundEvents.GHAST_SHOOT, 0.5F + getRandom().nextFloat(),
                    0.3F + getRandom().nextFloat() * 0.7F);
            }
            LivingEntity target = findBreathTarget();
            if (target != null) applyShadowBreath(target);
        }
        breathTimer--;
    }

    private LivingEntity findBreathTarget() {
        double range = 4.0D + getRandom().nextDouble() * 8.0D;
        Vec3 start = getEyePosition();
        Vec3 intendedEnd = start.add(getLookAngle().scale(range));
        HitResult blockHit = level().clip(new ClipContext(start, intendedEnd, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, this));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? intendedEnd : blockHit.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(this, start, end,
            getBoundingBox().expandTowards(end.subtract(start)).inflate(4.0D),
            entity -> entity instanceof LivingEntity living && living.isAlive() && living != this,
            start.distanceToSqr(end));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private void applyShadowBreath(LivingEntity target) {
        for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(2.0D), LivingEntity::isAlive)) {
            if (nearby == this || nearby != target && getRandom().nextInt(3) != 0) continue;
            float damage = Math.max(0.0F, (float) (7.5D - distanceTo(nearby)));
            if (damage <= 0.0F || !nearby.hurt(ACDamageTypes.source(this, ACDamageTypes.ANTIMATTER), damage)) continue;
            nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, nearby == target ? 200 : 100));
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                nearby == target ? 200 : 100, 1));
        }
    }

    private boolean teleportRandomly() {
        return attemptTeleport(getX() + (getRandom().nextDouble() - 0.5D) * 64.0D,
            getY() + getRandom().nextInt(64) - 32.0D,
            getZ() + (getRandom().nextDouble() - 0.5D) * 64.0D);
    }

    private boolean attemptTeleport(double x, double y, double z) {
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();
        if (!randomTeleport(x, y, z, true)) return false;
        level().playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT,
            getSoundSource(), 1.0F, 1.0F);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }
}