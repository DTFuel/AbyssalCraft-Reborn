package com.shinoow.abyssalcraft.content.entity.boss;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.content.entity.projectile.DreadSlug;
import com.shinoow.abyssalcraft.content.entity.projectile.DreadedCharge;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACDamageTypes;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/** Cha'garoth's persistent barf, side-head projectile and timed minion state machine. */
public final class ChagarothBoss extends BossMob {

    private static final EntityDataAccessor<Integer> BARF_TIMER =
        SynchedEntityData.defineId(ChagarothBoss.class, EntityDataSerializers.INT);

    private final int[] nextHeadUpdate = new int[2];

    public ChagarothBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossKind.CHAGAROTH);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BARF_TIMER, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BARF_TIMER, 0);
    }
    *///?}

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
        setSprinting(false);
        updateBossBarColor();
        regenerate();
        tickBarf();
        tickSideHeads();
        tickSummons();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !EffectHooks.isDreadImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
        }
        return hurt;
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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
        tag.putInt("BarfTimer", getBarfTimer());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBarfTimer(tag.getInt("BarfTimer"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event("dreadguard.idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("dreadguard.hit");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("dreadguard.death");
    }

    private int getBarfTimer() {
        return entityData.get(BARF_TIMER);
    }

    private void setBarfTimer(int timer) {
        entityData.set(BARF_TIMER, timer);
    }

    private void updateBossBarColor() {
        float fraction = getHealth() / getMaxHealth();
        setBossBarColor(fraction > 0.75F ? BossEvent.BossBarColor.BLUE
            : fraction > 0.5F ? BossEvent.BossBarColor.GREEN
            : fraction > 0.25F ? BossEvent.BossBarColor.YELLOW
            : BossEvent.BossBarColor.RED);
    }

    private void regenerate() {
        int pace = ACConfig.chagarothHealingPace.get();
        int amount = ACConfig.chagarothHealingAmount.get();
        if (amount > 0 && pace > 0 && tickCount % pace == 0) heal(amount);
    }

    private void tickBarf() {
        int timer = getBarfTimer();
        LivingEntity attackTarget = getTarget();
        if (attackTarget != null && attackTarget.isAlive() && distanceToSqr(attackTarget) <= 256.0D
                && timer <= -200) {
            timer = 150;
        }
        if (timer > 0) {
            level().broadcastEntityEvent(this, (byte) 23);
            if (tickCount % 5 == 0 && timer > 30) {
                for (int sound = 0; sound < 3; sound++) {
                    playSound(ModSounds.event("dreadguard.barf"), 0.7F + getRandom().nextFloat(),
                        0.2F + getRandom().nextFloat() * (0.6F - sound * 0.1F));
                }
            }
            LivingEntity lookedAt = findLookTarget();
            if (lookedAt != null) barfAt(lookedAt);
        }
        setBarfTimer(timer - 1);
    }

    private LivingEntity findLookTarget() {
        double range = 8.0D + getRandom().nextDouble() * 20.0D;
        Vec3 start = getEyePosition();
        Vec3 look = getLookAngle();
        Vec3 intendedEnd = start.add(look.scale(range));
        HitResult blockHit = level().clip(new ClipContext(start, intendedEnd, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, this));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? intendedEnd : blockHit.getLocation();
        AABB search = getBoundingBox().expandTowards(end.subtract(start)).inflate(8.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this, start, end, search,
            entity -> entity instanceof LivingEntity living && living.isAlive()
                && !EffectHooks.isDreadImmune(living), start.distanceToSqr(end));
        return entityHit != null && entityHit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private void barfAt(LivingEntity target) {
        for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(2.0D), living -> living.isAlive()
                    && !EffectHooks.isDreadImmune(living))) {
            if (nearby != target && getRandom().nextInt(3) != 0) continue;
            float damage = Math.max(0.0F, (float) (7.5D - distanceTo(nearby)) * 2.0F);
            if (damage > 0.0F && nearby.hurt(ACDamageTypes.source(this, ACDamageTypes.DREAD), damage)) {
                nearby.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 200, 1));
            }
        }
    }

    private void tickSideHeads() {
        if (!(level() instanceof ServerLevel)) return;
        for (int head = 0; head < nextHeadUpdate.length; head++) {
            if (tickCount < nextHeadUpdate[head]) continue;
            nextHeadUpdate[head] = tickCount + 10 + getRandom().nextInt(10);
            LivingEntity target = chooseSideHeadTarget();
            if (target != null) launchProjectileChain(head, target);
        }
    }

    private LivingEntity chooseSideHeadTarget() {
        List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(48.0D), living -> living != this && living.isAlive()
                && !(living instanceof Player player && (player.isCreative() || player.isSpectator()))
                && !EffectHooks.isDreadImmune(living) && hasLineOfSight(living));
        return candidates.isEmpty() ? null : candidates.get(getRandom().nextInt(candidates.size()));
    }

    private void launchProjectileChain(int head, LivingEntity target) {
        Vec3 origin = sideHeadPosition(head);
        Vec3 delta = target.position().add(0.0D, 0.5D, 0.0D).subtract(origin);
        launchSlug(origin, delta, 1.75F, 1.0F, null);
        switch (getRandom().nextInt(5)) {
            case 0 -> {
                launchSlug(origin, delta, 1.75F, 1.0F, null);
                nextHeadUpdate[head] = tickCount + 10;
            }
            case 1 -> {
                launchSlug(origin, delta.add(0.0D, getRandom().nextDouble() * 150.0D, 0.0D),
                    1.3F, 1.0F, LegacyEntities.DREAD_SPAWN.get());
                nextHeadUpdate[head] = tickCount + 20;
            }
            case 2 -> {
                launchSlug(origin, delta.add(0.0D, getRandom().nextDouble() * 150.0D, 0.0D),
                    1.3F, 1.0F, BossEntities.CHAGAROTH_SPAWN.get());
                nextHeadUpdate[head] = tickCount + 20;
            }
            case 3 -> {
                launchSlug(origin, delta.add(0.0D, getRandom().nextDouble() * 150.0D, 0.0D),
                    1.3F, 1.0F, BossEntities.CHAGAROTH_FIST.get());
                nextHeadUpdate[head] = tickCount + 20;
            }
            case 4 -> {
                launchCharge(origin, delta);
                nextHeadUpdate[head] = tickCount + 100;
            }
            default -> { }
        }
    }

    private Vec3 sideHeadPosition(int head) {
        float angle = (yBodyRot + 180.0F * (head + 1)) * Mth.DEG_TO_RAD;
        return new Vec3(getX() + Mth.cos(angle) * 1.4D, getY() + getEyeHeight() * 0.8D,
            getZ() + Mth.sin(angle) * 1.4D);
    }

    private void launchSlug(Vec3 origin, Vec3 delta, float velocity, float inaccuracy,
                            EntityType<? extends Mob> passengerType) {
        DreadSlug slug = ProjectileEntities.DREAD_SLUG.get().create(level());
        if (slug == null) return;
        slug.setOwner(this);
        slug.moveTo(origin.x, origin.y, origin.z, getYRot(), getXRot());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        slug.shoot(delta.x, delta.y + horizontal * 0.2D, delta.z, velocity, inaccuracy);
        double length = delta.length();
        if (length > 0.0D) slug.setDeltaMovement(slug.getDeltaMovement().add(delta.scale(0.64D / length)));
        level().addFreshEntity(slug);
        if (passengerType != null && level() instanceof ServerLevel server) {
            Mob passenger = passengerType.create(server);
            if (passenger != null) {
                passenger.moveTo(origin.x, origin.y, origin.z, getYRot(), getXRot());
                MobSpawnCompat.finalizeSpawnerSpawn(server, passenger);
                server.addFreshEntity(passenger);
                passenger.startRiding(slug, true);
            }
        }
    }

    private void launchCharge(Vec3 origin, Vec3 delta) {
        DreadedCharge charge = ProjectileEntities.DREADED_CHARGE.get().create(level());
        if (charge == null) return;
        charge.setOwner(this);
        charge.moveTo(origin.x, origin.y, origin.z, getYRot(), getXRot());
        charge.shoot(delta.x, delta.y + 0.5D, delta.z, 1.0F, 0.0F);
        level().addFreshEntity(charge);
    }

    private void tickSummons() {
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (tickCount % 600 == 0) {
            if (getRandom().nextBoolean()) {
                spawnLimited(server, LegacyEntities.DREAD_SPAWN.get(), ACConfig.dreadSpawnSpawnLimit.get() / 2);
            } else {
                spawnLimited(server, BossEntities.CHAGAROTH_SPAWN.get(), ACConfig.dreadSpawnSpawnLimit.get() / 2);
            }
        }
        if (tickCount % 1200 == 0) {
            spawnLimited(server, BossEntities.CHAGAROTH_FIST.get(), ACConfig.greaterDreadSpawnSpawnLimit.get() / 2);
        }
        if (tickCount % 2400 == 0) {
            spawnLimited(server, BossEntities.DREADGUARD.get(), ACConfig.greaterDreadSpawnSpawnLimit.get() / 2);
        }
        if (tickCount % 4800 == 0) {
            spawnLimited(server, LegacyEntities.GREATER_DREAD_SPAWN.get(),
                ACConfig.greaterDreadSpawnSpawnLimit.get() / 2);
        }
    }

    private void spawnLimited(ServerLevel level, EntityType<? extends Mob> type, int limit) {
        if (limit <= 0 || level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(32.0D),
                mob -> mob.getType() == type).size() >= limit) return;
        Mob mob = type.create(level);
        if (mob == null) return;
        mob.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        MobSpawnCompat.finalizeSpawnerSpawn(level, mob);
        level.addFreshEntity(mob);
    }
}