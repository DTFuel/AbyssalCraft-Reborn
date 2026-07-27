package com.shinoow.abyssalcraft.content.entity.legacy;

import java.util.Comparator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
//? if <1.21 {
import net.minecraft.world.entity.MobType;
//?}
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.pathfinding.ACWallClimberNavigation;
import com.shinoow.abyssalcraft.content.entity.projectile.DreadSlug;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.content.entity.behavior.ShadowEntityEffects;

public final class LegacyHostileMob extends Monster implements RangedAttackMob {

    private final LegacyMobKind kind;
    private boolean splitOnDeath = true;
    private int breathTimer = -300;
    private MeleeAttackGoal meleeGoal;
    private RangedAttackGoal rangedGoal;
    private boolean usingRangedGoal;

    LegacyHostileMob(EntityType<? extends Monster> type, Level level, LegacyMobKind kind) {
        super(type, level);
        this.kind = kind;
        if (isDreadAggregate()) {
            this.navigation = new ACWallClimberNavigation(this, level);
        }
        if (kind == LegacyMobKind.GREATER_DREAD_SPAWN || kind == LegacyMobKind.LESSER_DREADBEAST) {
            rangedGoal = new RangedAttackGoal(this, 0.4D, 20,
                kind == LegacyMobKind.LESSER_DREADBEAST ? 15.0F : 8.0F);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        meleeGoal = new MeleeAttackGoal(this, 1.0D, true);
        goalSelector.addGoal(2, meleeGoal);
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return isDreadAggregate() ? new ACWallClimberNavigation(this, level) : super.createNavigation(level);
    }

    @Override
    public boolean onClimbable() {
        return isDreadAggregate() ? horizontalCollision : super.onClimbable();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return !isDreadAggregate() && super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!super.checkSpawnRules(level, spawnType)) return false;
        if (isDreadAggregate() && level instanceof ServerLevel server) {
            return server.getEntitiesOfClass(LegacyHostileMob.class, getBoundingBox().inflate(32.0D),
                mob -> mob.isAlive() && mob.getType() == getType()).size() < 4;
        }
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (kind == LegacyMobKind.SHADOW_BEAST) {
            swing(InteractionHand.MAIN_HAND);
            swing(InteractionHand.OFF_HAND);
        }
        boolean hurt = super.doHurtTarget(target);
        if (hurt && isDread() && target instanceof LivingEntity living
            && !com.shinoow.abyssalcraft.common.handlers.EffectHooks.isDreadImmune(living)) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isShadow()) ShadowEntityEffects.tickParticles(this);
        if (kind == LegacyMobKind.SHADOW_BEAST) tickShadowBreath();
        if (level().isClientSide || !(level() instanceof ServerLevel server)) return;

        if (kind == LegacyMobKind.GREATER_DREAD_SPAWN || kind == LegacyMobKind.LESSER_DREADBEAST) {
            updateDreadCombatGoal();
        }

        switch (kind) {
            case DREAD_SPAWN -> merge(server, LegacyEntities.DREAD_SPAWN, LegacyEntities.GREATER_DREAD_SPAWN, 2.0D, 10);
            case GREATER_DREAD_SPAWN -> {
                merge(server, LegacyEntities.GREATER_DREAD_SPAWN, LegacyEntities.LESSER_DREADBEAST, 5.0D, 4);
                if (tickCount % 2000 == 0) spawnIfBelowLimit(server, LegacyEntities.DREAD_SPAWN,
                    ACConfig.dreadSpawnSpawnLimit.get());
            }
            case LESSER_DREADBEAST -> {
                if (tickCount % 400 == 0) spawnIfBelowLimit(server, LegacyEntities.DREAD_SPAWN,
                    ACConfig.dreadSpawnSpawnLimit.get());
                if (tickCount % 10000 == 0) spawnIfBelowLimit(server, LegacyEntities.GREATER_DREAD_SPAWN,
                    ACConfig.greaterDreadSpawnSpawnLimit.get());
            }
            default -> { }
        }
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType spawnType, net.minecraft.world.entity.SpawnGroupData spawnData
            //? if <1.21 {
            , CompoundTag tag
            //?}
    ) {
        var result = super.finalizeSpawn(level, difficulty, spawnType, spawnData
            //? if <1.21 {
            , tag
            //?}
        );
        if (isShadow()) ShadowEntityEffects.equipHalloweenHead(this);
        return result;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (kind != LegacyMobKind.GREATER_DREAD_SPAWN && kind != LegacyMobKind.LESSER_DREADBEAST) return;
        DreadSlug slug = ProjectileEntities.DREAD_SLUG.get().create(level());
        if (slug == null) return;
        slug.setOwner(this);
        slug.moveTo(getX(), getEyeY() - 0.1D, getZ(), getYRot(), getXRot());
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - slug.getY() + Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2D;
        slug.shoot(dx, dy, dz, 1.6F, 12.0F);
        playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(slug);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (splitOnDeath && level() instanceof ServerLevel server) {
            if (kind == LegacyMobKind.GREATER_DREAD_SPAWN) {
                spawn(server, LegacyEntities.DREAD_SPAWN);
                spawn(server, LegacyEntities.DREAD_SPAWN);
            } else if (kind == LegacyMobKind.LESSER_DREADBEAST) {
                spawn(server, LegacyEntities.GREATER_DREAD_SPAWN);
                spawn(server, LegacyEntities.GREATER_DREAD_SPAWN);
            }
        }
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SplitOnDeath", splitOnDeath);
        if (kind == LegacyMobKind.SHADOW_BEAST) tag.putInt("BreathTimer", breathTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        splitOnDeath = !tag.contains("SplitOnDeath") || tag.getBoolean("SplitOnDeath");
        if (kind == LegacyMobKind.SHADOW_BEAST && tag.contains("BreathTimer")) {
            breathTimer = tag.getInt("BreathTimer");
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (isShadow() && effect.getEffect() == MobEffects.POISON) return false;
        return super.canBeAffected(effect);
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        if (isShadow()) return ModSounds.event("shadow.hit");
        if (isDread()) return ModSounds.event("dreadspawn.hit");
        return super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (isShadow()) return ModSounds.event("shadow.death");
        if (isDread()) return ModSounds.event("dreadspawn.death");
        return super.getDeathSound();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isDread()) return ModSounds.event("dreadspawn.idle");
        return super.getAmbientSound();
    }

    static AttributeSupplier.Builder createAttributes(LegacyMobKind kind) {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, kind.followRange)
            .add(Attributes.MOVEMENT_SPEED, kind.speed)
            .add(Attributes.MAX_HEALTH, kind.health)
            .add(Attributes.ATTACK_DAMAGE, kind.damage)
            .add(Attributes.ARMOR, kind.armor)
            .add(Attributes.KNOCKBACK_RESISTANCE, kind.knockbackResistance);
    }

    //? if <1.21 {
    @Override public boolean canBreatheUnderwater() { return kind.breathesUnderwater; }
    //?}
    //? if <1.21 {
    @Override public MobType getMobType() {
        if (kind == LegacyMobKind.LESSER_DREADBEAST) return MobType.ARTHROPOD;
        return kind.undead ? MobType.UNDEAD : MobType.UNDEFINED;
    }
    //?}

    private boolean isDread() {
        return kind == LegacyMobKind.DREADLING || isDreadAggregate();
    }

    private boolean isDreadAggregate() {
        return kind == LegacyMobKind.DREAD_SPAWN || kind == LegacyMobKind.GREATER_DREAD_SPAWN
            || kind == LegacyMobKind.LESSER_DREADBEAST;
    }

    private boolean isShadow() {
        return kind == LegacyMobKind.SHADOW_CREATURE || kind == LegacyMobKind.SHADOW_MONSTER
            || kind == LegacyMobKind.SHADOW_BEAST;
    }

    private void updateDreadCombatGoal() {
        if (rangedGoal == null) return;
        LivingEntity target = getTarget();
        boolean shouldUseRanged = target != null && (distanceToSqr(target) > 225.0D
            || target.getY() > getY() + 4.0D || !target.onGround());
        if (shouldUseRanged == usingRangedGoal) return;
        usingRangedGoal = shouldUseRanged;
        if (shouldUseRanged) {
            goalSelector.removeGoal(meleeGoal);
            goalSelector.addGoal(2, rangedGoal);
        } else {
            goalSelector.removeGoal(rangedGoal);
            goalSelector.addGoal(2, meleeGoal);
        }
    }

    private void tickShadowBreath() {
        LivingEntity target = getTarget();
        if (!level().isClientSide && target != null && distanceToSqr(target) <= 64.0D && breathTimer <= -300) {
            breathTimer = 100;
        }
        if (breathTimer > 0) {
            setDeltaMovement(getDeltaMovement().multiply(0.05D, 1.0D, 0.05D));
            if (!level().isClientSide && tickCount % 5 == 0) {
                playSound(SoundEvents.GHAST_SHOOT, 0.5F + getRandom().nextFloat(),
                    getRandom().nextFloat() * 0.7F + 0.3F);
            }
            if (!level().isClientSide) damageShadowBreath();
        }
        breathTimer--;
    }

    private void damageShadowBreath() {
        double range = 4.0D + getRandom().nextDouble() * 8.0D;
        Vec3 start = getEyePosition();
        Vec3 end = start.add(getViewVector(1.0F).scale(range));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(this, start, end,
            getBoundingBox().expandTowards(getViewVector(1.0F).scale(range)).inflate(4.0D),
            entity -> entity instanceof LivingEntity && entity != this, range * range);
        if (hit == null || !(hit.getEntity() instanceof LivingEntity direct)) return;

        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class,
            direct.getBoundingBox().inflate(2.0D), entity -> entity.isAlive() && entity != this)) {
            float damage = Math.max(0.0F, (float) (4.5D - distanceTo(living)));
            if (damage > 0.0F && living.hurt(
                    com.shinoow.abyssalcraft.system.effect.ACDamageTypes.source(
                        this, com.shinoow.abyssalcraft.system.effect.ACDamageTypes.SHADOW), damage)) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, living == direct ? 200 : 100));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    living == direct ? 200 : 100, 1));
            }
        }
    }

    private void merge(ServerLevel server,
                       java.util.function.Supplier<? extends EntityType<? extends LegacyHostileMob>> source,
                       java.util.function.Supplier<? extends EntityType<? extends LegacyHostileMob>> result,
                       double radius, int resultLimit) {
        if (getType() != source.get() || tickCount % 20 != 0 || !isAlive()) return;
        AABB nearbyBox = getBoundingBox().inflate(radius);
        List<LegacyHostileMob> nearby = server.getEntitiesOfClass(LegacyHostileMob.class, nearbyBox,
            mob -> mob.isAlive() && mob.getType() == source.get()).stream()
            .sorted(Comparator.comparingInt(Entity::getId)).toList();
        if (nearby.size() < 5 || nearby.get(0) != this) return;
        long results = server.getEntitiesOfClass(LegacyHostileMob.class, getBoundingBox().inflate(32.0D),
            mob -> mob.isAlive() && mob.getType() == result.get()).size();
        if (results >= resultLimit) return;

        for (int index = 0; index < 5; index++) {
            LegacyHostileMob consumed = nearby.get(index);
            consumed.splitOnDeath = false;
            consumed.discard();
        }
        spawn(server, result);
    }

    private void spawnIfBelowLimit(ServerLevel server,
                                   java.util.function.Supplier<? extends EntityType<? extends LegacyHostileMob>> type,
                                   int limit) {
        if (limit <= 0) return;
        int nearby = server.getEntitiesOfClass(LegacyHostileMob.class, getBoundingBox().inflate(32.0D),
            mob -> mob.isAlive() && mob.getType() == type.get()).size();
        if (nearby < limit) spawn(server, type);
    }

    private void spawn(ServerLevel server,
                       java.util.function.Supplier<? extends EntityType<? extends LegacyHostileMob>> type) {
        LegacyHostileMob child = type.get().create(server);
        if (child == null) return;
        child.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        server.addFreshEntity(child);
    }
}