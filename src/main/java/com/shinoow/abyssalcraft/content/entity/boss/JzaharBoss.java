package com.shinoow.abyssalcraft.content.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ContentConfigMatrix;
import com.shinoow.abyssalcraft.content.entity.behavior.EldritchEntities;
import com.shinoow.abyssalcraft.content.entity.misc.BlackHole;
import com.shinoow.abyssalcraft.content.entity.misc.Implosion;
import com.shinoow.abyssalcraft.content.entity.misc.MiscEntities;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.portal.DimensionTeleport;
import com.shinoow.abyssalcraft.platform.WitherSkullCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;

/** J'zahar's persistent five-cooldown combat state machine. */
public final class JzaharBoss extends BossMob implements RangedAttackMob {

    private static final EntityDataAccessor<Integer> EARTHQUAKE_TIMER = timerAccessor();
    private static final EntityDataAccessor<Integer> BLACK_HOLE_TIMER = timerAccessor();
    private static final EntityDataAccessor<Integer> IMPLOSION_TIMER = timerAccessor();
    private static final EntityDataAccessor<Integer> SHOUT_TIMER = timerAccessor();
    private static final EntityDataAccessor<Integer> GLOBAL_COOLDOWN = timerAccessor();

    private int shoutTicks;
    private int iframes;
    private int creativeDialogCooldown;
    private boolean chargingShout;
    private MeleeAttackGoal meleeGoal;
    private RangedAttackGoal rangedGoal;
    private boolean usingRangedGoal;

    public JzaharBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossKind.JZAHAR);
    }

    private static EntityDataAccessor<Integer> timerAccessor() {
        return SynchedEntityData.defineId(JzaharBoss.class, EntityDataSerializers.INT);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EARTHQUAKE_TIMER, 0);
        entityData.define(BLACK_HOLE_TIMER, 0);
        entityData.define(IMPLOSION_TIMER, 0);
        entityData.define(SHOUT_TIMER, 0);
        entityData.define(GLOBAL_COOLDOWN, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EARTHQUAKE_TIMER, 0);
        builder.define(BLACK_HOLE_TIMER, 0);
        builder.define(IMPLOSION_TIMER, 0);
        builder.define(SHOUT_TIMER, 0);
        builder.define(GLOBAL_COOLDOWN, 0);
    }
    *///?}

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        meleeGoal = new MeleeAttackGoal(this, 0.35D, true);
        rangedGoal = new RangedAttackGoal(this, 0.4D, 40, 20.0F);
        goalSelector.addGoal(2, meleeGoal);
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.35D));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        updateBossBarColor();
        regenerate();
        if (iframes > 0) iframes--;
        if (creativeDialogCooldown > 0) creativeDialogCooldown--;
        if (getACDeathTime() > 0) return;
        tickFourthWallDialog();
        decrementTimers();
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        updateCombatGoal(target);
        tickShout(target);
        if (getTimer(EARTHQUAKE_TIMER) < 0 && getTimer(GLOBAL_COOLDOWN) < 0 && target.onGround()
                && getRandom().nextInt(Math.max(1, 400 / timerStep())) == 0) {
            beginEarthquake();
        }
        if (getTimer(IMPLOSION_TIMER) < 0 && getTimer(GLOBAL_COOLDOWN) < 0) {
            spawnImplosion(target);
        }
        if (getTimer(BLACK_HOLE_TIMER) < 0 && getTimer(GLOBAL_COOLDOWN) < 0
                && !ACConfig.no_black_holes.get() && getRandom().nextInt(800) == 0) {
            spawnBlackHole(target);
        }
        if (getTimer(EARTHQUAKE_TIMER) > 600) applyEarthquake();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        Vec3 direction = new Vec3(target.getX() - getX(),
            target.getY() + target.getEyeHeight() * 0.35D - (getY() + 3.0D), target.getZ() - getZ());
        WitherSkull skull = WitherSkullCompat.create(level(), this, direction);
        skull.setDangerous(getRandom().nextFloat() < 0.001F);
        skull.moveTo(getX(), getY() + 3.0D, getZ(), getYRot(), getXRot());
        level().levelEvent(null, 1014, blockPosition(), 0);
        level().addFreshEntity(skull);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) && getY() <= 0.0D && !level().isClientSide) {
            discard();
            return false;
        }
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA) || source.is(DamageTypes.EXPLOSION)
                || source.is(DamageTypes.PLAYER_EXPLOSION) || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)) return false;
        if (iframes > 10) return false;
        if (amount >= 20.0F) amount = 5.0F + getRandom().nextInt(5);
        if (amount > 0.0F) iframes = 30;
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("EarthquakeTimer", getTimer(EARTHQUAKE_TIMER));
        tag.putInt("BlackHoleTimer", getTimer(BLACK_HOLE_TIMER));
        tag.putInt("ImplosionTimer", getTimer(IMPLOSION_TIMER));
        tag.putInt("ShoutTimer", getTimer(SHOUT_TIMER));
        tag.putInt("GlobalCooldown", getTimer(GLOBAL_COOLDOWN));
        tag.putInt("ShoutTicks", shoutTicks);
        tag.putInt("IFrames", iframes);
        tag.putInt("CreativeDialogCooldown", creativeDialogCooldown);
        tag.putBoolean("ChargingShout", chargingShout);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTimer(EARTHQUAKE_TIMER, tag.getInt("EarthquakeTimer"));
        setTimer(BLACK_HOLE_TIMER, tag.getInt("BlackHoleTimer"));
        setTimer(IMPLOSION_TIMER, tag.getInt("ImplosionTimer"));
        setTimer(SHOUT_TIMER, tag.getInt("ShoutTimer"));
        setTimer(GLOBAL_COOLDOWN, tag.getInt("GlobalCooldown"));
        shoutTicks = tag.getInt("ShoutTicks");
        iframes = Math.max(0, tag.getInt("IFrames"));
        creativeDialogCooldown = Math.max(0, tag.getInt("CreativeDialogCooldown"));
        chargingShout = tag.getBoolean("ChargingShout");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void tickACDeath(int deathTick) {
        if (!(level() instanceof ServerLevel server)) return;
        if (deathTick == 410) playSound(ModSounds.event("jzahar.charge"), 1.0F, 1.0F);
        if (deathTick > 400 && deathTick < 800) {
            double speed = 0.05D + (deathTick - 400) * 0.0001D;
            for (Entity entity : server.getEntities(this, getBoundingBox().inflate(32.0D))) {
                Vec3 direction = entity.position().subtract(position()).normalize();
                double scale = Math.max(0.0D, (32.0D - entity.distanceTo(this)) / 32.0D);
                entity.push(-direction.x * speed * scale, -direction.y * speed * scale,
                    -direction.z * speed * scale);
            }
        }
        if (deathTick == 790) finishGatekeeperDeath(server);
    }

    private void finishGatekeeperDeath(ServerLevel server) {
        if (server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            net.minecraft.core.BlockPos center = blockPosition();
            for (int x = -9; x <= 9; x++) {
                for (int y = -9; y <= 9; y++) {
                    for (int z = -9; z <= 9; z++) {
                        net.minecraft.core.BlockPos pos = center.offset(x, y, z);
                        var state = server.getBlockState(pos);
                        if (!state.isAir() && !state.hasBlockEntity()
                                && state.getBlock().getExplosionResistance() < 600000.0F) {
                            server.destroyBlock(pos, false, this, 512);
                        }
                    }
                }
            }
        }
        for (Entity entity : server.getEntities(this, getBoundingBox().inflate(3.0D, 1.0D, 3.0D))) {
            if (entity instanceof ServerPlayer player) {
                player.setHealth(1.0F);
                addDeathEffects(player);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 255));
                DimensionTeleport.teleport(player, ACDimensions.DARK_REALM);
            } else if (entity instanceof LivingEntity || entity instanceof net.minecraft.world.entity.item.ItemEntity) {
                entity.discard();
            }
        }
    }

    private void addDeathEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 2400, 5));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 2400, 5));
    }

    private void updateBossBarColor() {
        float fraction = getHealth() / getMaxHealth();
        setBossBarColor(fraction > 0.75F ? BossEvent.BossBarColor.BLUE
            : fraction > 0.5F ? BossEvent.BossBarColor.GREEN
            : fraction > 0.25F ? BossEvent.BossBarColor.YELLOW
            : BossEvent.BossBarColor.RED);
    }

    private void updateCombatGoal(LivingEntity target) {
        boolean ranged = distanceToSqr(target) > 28.0D || !target.onGround()
            || target.getY() > getY() + 4.0D;
        if (ranged == usingRangedGoal) return;
        usingRangedGoal = ranged;
        if (ranged) {
            goalSelector.removeGoal(meleeGoal);
            goalSelector.addGoal(2, rangedGoal);
        } else {
            goalSelector.removeGoal(rangedGoal);
            goalSelector.addGoal(2, meleeGoal);
        }
    }

    private void regenerate() {
        int pace = ACConfig.jzaharHealingPace.get();
        int amount = ACConfig.jzaharHealingAmount.get();
        if (amount > 0 && pace > 0 && tickCount % pace == 0) heal(amount);
    }

    private void tickFourthWallDialog() {
        if (creativeDialogCooldown > 0 || !fourthWallDialogEnabled(
            ContentConfigMatrix.showBossDialogs(), ACConfig.jzaharBreaksFourthWall.get())) return;
        Player player = level().getNearestPlayer(this, 5.0D);
        if (player == null || !player.isCreative()) return;
        creativeDialogCooldown = 1200;
        player.sendSystemMessage(Component.translatable("message.abyssalcraft.jzahar.creative.1", player.getName()));
        player.sendSystemMessage(Component.translatable("message.abyssalcraft.jzahar.creative.2"));
    }

    public static boolean fourthWallDialogEnabled(boolean showBossDialogs, boolean breaksFourthWall) {
        return showBossDialogs && breaksFourthWall;
    }

    private int timerStep() {
        return ACConfig.hardcoreMode.get() ? 2 : 1;
    }

    private void decrementTimers() {
        int step = timerStep();
        setTimer(EARTHQUAKE_TIMER, getTimer(EARTHQUAKE_TIMER) - step);
        setTimer(BLACK_HOLE_TIMER, getTimer(BLACK_HOLE_TIMER) - step);
        setTimer(IMPLOSION_TIMER, getTimer(IMPLOSION_TIMER) - step);
        setTimer(SHOUT_TIMER, getTimer(SHOUT_TIMER) - step);
        setTimer(GLOBAL_COOLDOWN, getTimer(GLOBAL_COOLDOWN) - step);
    }

    private int getTimer(EntityDataAccessor<Integer> accessor) {
        return entityData.get(accessor);
    }

    private void setTimer(EntityDataAccessor<Integer> accessor, int value) {
        entityData.set(accessor, value);
    }

    private void tickShout(LivingEntity target) {
        if (!chargingShout && getTimer(SHOUT_TIMER) < 0 && getTimer(GLOBAL_COOLDOWN) < 0
                && distanceToSqr(target) <= 9216.0D && getRandom().nextInt(20) == 0) {
            playSound(ModSounds.event("jzahar.shout"), 5.0F, 1.0F);
            shoutTicks = getTimer(SHOUT_TIMER) - 30;
            chargingShout = true;
            setTimer(GLOBAL_COOLDOWN, 100);
        }
        if (chargingShout && getTimer(SHOUT_TIMER) < shoutTicks) {
            chargingShout = false;
            setTimer(SHOUT_TIMER, 400);
            playSound(ModSounds.event("jzahar.blast"), 5.0F, 1.0F);
            level().broadcastEntityEvent(this, (byte) 23);
            applyShout();
        }
    }

    private void applyShout() {
        Vec3 look = getLookAngle();
        AABB area = getBoundingBox().inflate(64.0D).move(look.scale(32.0D));
        for (Entity entity : level().getEntities(this, area)) {
            if (entity instanceof LivingEntity living && EldritchEntities.isEldritch(living)) continue;
            double velocity = 2.0D + getRandom().nextDouble() * 8.0D;
            Vec3 force = look.add(getRandom().nextGaussian() * 0.075D,
                getRandom().nextGaussian() * 0.075D, getRandom().nextGaussian() * 0.075D);
            entity.push(force.x * velocity, force.y * velocity * 0.25D, force.z * velocity);
            entity.hurt(damageSources().flyIntoWall(), (float) velocity * 2.0F * timerStep());
        }
    }

    private void beginEarthquake() {
        setTimer(EARTHQUAKE_TIMER, 1000);
        setTimer(GLOBAL_COOLDOWN, 100);
        playSound(ModSounds.event("jzahar.earthquake"), 5.0F, 1.0F);
    }

    private void applyEarthquake() {
        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(64.0D),
                target -> target != this && target.onGround() && !EldritchEntities.isEldritch(target))) {
            living.push(getRandom().nextDouble() * 0.1D - 0.05D,
                getRandom().nextDouble() * 0.1D - 0.05D,
                getRandom().nextDouble() * 0.1D - 0.05D);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3));
            if (getRandom().nextInt(5) == 0) {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20, 3));
                living.push(0.0D, 0.5D, 0.0D);
                living.hurt(damageSources().hotFloor(), timerStep());
            }
        }
    }

    private void spawnImplosion(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return;
        Implosion implosion = MiscEntities.IMPLOSION.get().create(server);
        if (implosion == null) return;
        implosion.setOwner(this);
        implosion.moveTo(target.getX() + signedOffset(0, 9), target.getY() + 2.0D,
            target.getZ() + signedOffset(0, 9));
        server.levelEvent(null, 3000, implosion.blockPosition(), 0);
        server.addFreshEntity(implosion);
        setTimer(IMPLOSION_TIMER, 1200);
        setTimer(GLOBAL_COOLDOWN, 100);
        playSound(ModSounds.event("jzahar.implosion"), 5.0F, 1.0F);
    }

    private void spawnBlackHole(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return;
        BlackHole blackHole = MiscEntities.BLACK_HOLE.get().create(server);
        if (blackHole == null) return;
        blackHole.setOwner(this);
        blackHole.moveTo(target.getX() + signedOffset(5, 14), target.getY() + 2.0D,
            target.getZ() + signedOffset(5, 14));
        server.levelEvent(null, 3000, blackHole.blockPosition(), 0);
        server.addFreshEntity(blackHole);
        setTimer(BLACK_HOLE_TIMER, 1600);
        setTimer(GLOBAL_COOLDOWN, 100);
        playSound(ModSounds.event("jzahar.black_hole"), 5.0F, 1.0F);
    }

    private int signedOffset(int min, int max) {
        int value = min + getRandom().nextInt(max - min + 1);
        return getRandom().nextBoolean() ? value : -value;
    }
}