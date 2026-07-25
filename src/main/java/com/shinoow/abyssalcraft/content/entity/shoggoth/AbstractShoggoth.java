package com.shinoow.abyssalcraft.content.entity.shoggoth;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;
import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothOozeBlock;
import com.shinoow.abyssalcraft.content.entity.base.ACMob;
import com.shinoow.abyssalcraft.content.entity.demon.DemonAnimal;
import com.shinoow.abyssalcraft.content.entity.demon.EvilAnimal;
import com.shinoow.abyssalcraft.content.entity.pathfinding.ACWallClimberNavigation;
import com.shinoow.abyssalcraft.content.entity.ai.ShoggothBuildMonolithGoal;
import com.shinoow.abyssalcraft.content.entity.ai.WorshipGoal;
import com.shinoow.abyssalcraft.content.entity.projectile.AcidProjectile;
import com.shinoow.abyssalcraft.content.entity.projectile.ProjectileEntities;
import com.shinoow.abyssalcraft.platform.FoodCompat;
import com.shinoow.abyssalcraft.platform.EntityPartCompat;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.registry.ModSounds;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.content.entity.behavior.ShadowEntityEffects;

/**
 * Shared base for the AbyssalCraft shoggoth family (owned by PD-5, Stage D2a).
 *
 * <p>Faithful successor to 1.12.2 {@code common.entity.EntityShoggothBase} (an
 * {@code EntityClimbingMobBase}). Ports the traits that stand on their own without unported
 * subsystems: wall-climbing navigation (PD-2's {@link ACWallClimberNavigation}), knockback
 * resistance, immunity to water pushing, the slow in-water glide, dimension type, food-driven
 * growth/birth and type-specific melee effects.
 *
 * <p>The remaining mechanics are layered onto this authoritative state in RR-ENTITY-BEHAVIOR:
 * <ul>
 *   <li>Acid melee + acid spit -&gt; needs {@code EntityAcidProjectile} (projectile stage) and the acid
 *       {@code DamageSource} + block-melting.</li>
 *   <li>Monolith building -&gt; needs {@code WorldGenShoggothMonolith} + the monolith/biomass blocks
 *       (Stage G worldgen).</li>
 *   <li>Shoggoth ooze trail -&gt; needs the {@code shoggoth_ooze} block.</li>
 *   <li>Multi-part hitbox, worship AI, type-specific loot and shadow particles.</li>
 * </ul>
 * Rendering lands in Stage E, so these are verified on a dedicated server with {@code /summon}.
 */
public abstract class AbstractShoggoth extends ACMob implements EntityPartCompat.Parent {

    private static final EntityDataAccessor<Integer> TYPE =
        SynchedEntityData.defineId(AbstractShoggoth.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD =
        SynchedEntityData.defineId(AbstractShoggoth.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BUILDING =
        SynchedEntityData.defineId(AbstractShoggoth.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ASSISTING =
        SynchedEntityData.defineId(AbstractShoggoth.class, EntityDataSerializers.BOOLEAN);

    private int monolithTimer;
    private int acidSpitCooldown;
    private int buildAttemptTicks;
    @Nullable
    private BlockPos monolithTarget;
    @Nullable
    private UUID monolithLeader;
    private final EntityPartCompat.Part<AbstractShoggoth> headPart;
    private final EntityPartCompat.Part<AbstractShoggoth> bodyPart;
    private final EntityPartCompat.Part<?>[] parts;

    protected AbstractShoggoth(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        PartProfile profile = partProfile();
        headPart = new EntityPartCompat.Part<>(this, "head", profile.headWidth(), profile.headHeight());
        bodyPart = new EntityPartCompat.Part<>(this, "body", profile.bodyWidth(), profile.bodyHeight());
        parts = new EntityPartCompat.Part<?>[] { headPart, bodyPart };
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, 0);
        entityData.define(FOOD, 0);
        entityData.define(BUILDING, false);
        entityData.define(ASSISTING, false);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, 0);
        builder.define(FOOD, 0);
        builder.define(BUILDING, false);
        builder.define(ASSISTING, false);
    }
    *///?}

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0D));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new ShoggothBuildMonolithGoal(this));
        goalSelector.addGoal(9, new WorshipGoal(this, ShoggothBlocks.WORSHIP_TARGETS, 0.5D, 8,
            ModSounds.event("remnant.priest.chant")));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ACWallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateParts();
        if (getShoggothType() == 4) ShadowEntityEffects.tickParticles(this);
        if (isInWater()) {
            Vec3 look = getLookAngle();
            setDeltaMovement(getDeltaMovement().add(look.x * 0.005D, look.y * 0.005D, look.z * 0.005D));
        }
        if (!level().isClientSide) {
            monolithTimer++;
            spreadOoze();
            if (tickCount % 20 == 0 && getFoodLevel() >= 8) {
                setFoodLevel(getFoodLevel() - 8);
                performFoodAction();
            }
            if (tickCount % 40 == 0 && ACConfig.consumeItems.get()) consumeDroppedItems();
            tickAcidSpit();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!hurt || !(target instanceof LivingEntity living)) return hurt;
        switch (getShoggothType()) {
            case 1 -> {
                if (!EffectHooks.isCoraliumImmune(living)) {
                    living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
                }
            }
            case 2 -> {
                if (!EffectHooks.isDreadImmune(living)) {
                    living.addEffect(MobEffectCompat.effectInstance(ACEffects.DREAD_PLAGUE, 100, 0));
                }
            }
            case 3 -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
            case 4 -> living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            default -> { }
        }
        return true;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        if (isShoggothFood(victim)) feed(victim);
        return super.killedEntity(level, victim);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnData
                                        //? if <1.21 {
                                        , CompoundTag tag
                                        //?}
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData
            //? if <1.21 {
            , tag
            //?}
        );
        setShoggothType(typeForDimension(level.getLevel().dimension()));
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ShoggothType", getShoggothType());
        tag.putInt("FoodLevel", getFoodLevel());
        tag.putInt("MonolithTimer", monolithTimer);
        tag.putBoolean("BuildingMonolith", isBuildingMonolith());
        tag.putBoolean("AssistingMonolith", isAssistingMonolith());
        tag.putInt("BuildAttemptTicks", buildAttemptTicks);
        if (monolithTarget != null) tag.putLong("MonolithTarget", monolithTarget.asLong());
        if (monolithLeader != null) tag.putUUID("MonolithLeader", monolithLeader);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ShoggothType")) setShoggothType(tag.getInt("ShoggothType"));
        if (tag.contains("FoodLevel")) setFoodLevel(tag.getInt("FoodLevel"));
        monolithTimer = Math.max(0, tag.getInt("MonolithTimer"));
        entityData.set(BUILDING, tag.getBoolean("BuildingMonolith"));
        entityData.set(ASSISTING, tag.getBoolean("AssistingMonolith"));
        buildAttemptTicks = Math.max(0, tag.getInt("BuildAttemptTicks"));
        monolithTarget = tag.contains("MonolithTarget") ? BlockPos.of(tag.getLong("MonolithTarget")) : null;
        monolithLeader = tag.hasUUID("MonolithLeader") ? tag.getUUID("MonolithLeader") : null;
        if ((isBuildingMonolith() && monolithTarget == null)
            || (isAssistingMonolith() && monolithLeader == null)) clearMonolithBuildState();
    }

    public int getShoggothType() {
        return entityData.get(TYPE);
    }

    public void setShoggothType(int type) {
        entityData.set(TYPE, Mth.clamp(type, 0, 4));
    }

    public int getFoodLevel() {
        return entityData.get(FOOD);
    }

    public void setFoodLevel(int food) {
        entityData.set(FOOD, Math.max(0, food));
    }

    public void feed(LivingEntity entity) {
        int points = Math.max(1, Mth.ceil(entity.getBbHeight() * entity.getBbWidth() * 4.0F));
        setFoodLevel(getFoodLevel() + points);
        playSound(ModSounds.event("shoggoth.consume"), 1.0F, getVoicePitch());
    }

    public int getMonolithTimer() {
        return monolithTimer;
    }

    public void reduceMonolithTimer() {
        monolithTimer = Math.max(0, monolithTimer - 200);
    }

    public void resetMonolithTimer() {
        monolithTimer = 0;
    }

    public boolean isBuildingMonolith() {
        return entityData.get(BUILDING);
    }

    public boolean isAssistingMonolith() {
        return entityData.get(ASSISTING);
    }

    @Nullable
    public BlockPos getMonolithTarget() {
        return monolithTarget;
    }

    @Nullable
    public UUID getMonolithLeader() {
        return monolithLeader;
    }

    public int getBuildAttemptTicks() {
        return buildAttemptTicks;
    }

    public void beginMonolithBuild(BlockPos target, int attemptTicks) {
        entityData.set(BUILDING, true);
        entityData.set(ASSISTING, false);
        monolithTarget = target.immutable();
        monolithLeader = null;
        buildAttemptTicks = Math.max(1, attemptTicks);
    }

    public void assistMonolithBuild(UUID leader) {
        entityData.set(BUILDING, false);
        entityData.set(ASSISTING, true);
        monolithTarget = null;
        monolithLeader = leader;
        buildAttemptTicks = 0;
    }

    public void decrementBuildAttemptTicks() {
        buildAttemptTicks = Math.max(0, buildAttemptTicks - 1);
    }

    public void clearMonolithBuildState() {
        entityData.set(BUILDING, false);
        entityData.set(ASSISTING, false);
        monolithTarget = null;
        monolithLeader = null;
        buildAttemptTicks = 0;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public EntityPartCompat.Part<?>[] getParts() {
        return parts;
    }

    @Override
    public boolean hurtPart(EntityPartCompat.Part<?> part, DamageSource source, float amount) {
        return hurt(source, amount);
    }

    protected abstract PartProfile partProfile();

    @Override
    protected String legacyLootTable() {
        String size = this instanceof LesserShoggoth ? "lesser_"
            : this instanceof GreaterShoggoth ? "greater_" : "";
        String variant = switch (getShoggothType()) {
            case 1 -> "abyssal_shoggoth";
            case 2 -> "dreaded_shoggoth";
            case 3 -> "omothol_shoggoth";
            case 4 -> "shadow_shoggoth";
            default -> "shoggoth";
        };
        return size + variant;
    }

    protected abstract void performFoodAction();

    protected boolean isLargeShoggoth() {
        return true;
    }

    protected final void launchOffspring(AbstractShoggoth offspring) {
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && getRandom().nextInt(3) == 0) {
            spitAcidAt(target, 0.8F, 8.0F, offspring);
        }
    }

    protected final <T extends AbstractShoggoth> T spawnShoggoth(EntityType<T> type, MobSpawnType reason) {
        if (!(level() instanceof ServerLevel server)) return null;
        T spawned = type.create(server);
        if (spawned == null) return null;
        spawned.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        spawned.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), reason, null
            //? if <1.21 {
            , null
            //?}
        );
        spawned.setShoggothType(getShoggothType());
        if (hasCustomName()) spawned.setCustomName(getCustomName());
        server.addFreshEntity(spawned);
        return spawned;
    }

    /** Shared shoggoth attribute baseline; concrete shoggoths add health/damage. */
    protected static AttributeSupplier.Builder shoggothAttributes() {
        return ACMob.createAttributes()
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    private void tickAcidSpit() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        if (acidSpitCooldown > 0) acidSpitCooldown--;
        int frequency = ACConfig.acidSpitFrequency.get();
        if (frequency > 0 && acidSpitCooldown <= 0 && (isLargeShoggoth() || ACConfig.hardcoreMode.get())
                && distanceToSqr(target) > 32.0D) {
            acidSpitCooldown = frequency;
            spitAcidAt(target, 1.0F, 12.0F, null);
        }
    }

    private void spitAcidAt(LivingEntity target, float velocity, float inaccuracy, Entity passenger) {
        AcidProjectile acid = ProjectileEntities.ACID_PROJECTILE.get().create(level());
        if (acid == null) return;
        acid.setOwner(this);
        acid.moveTo(getX(), getEyeY() - 0.1D, getZ(), getYRot(), getXRot());
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - acid.getY() + Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2D;
        acid.shoot(dx, dy, dz, velocity, inaccuracy);
        if (passenger != null) passenger.startRiding(acid);
        playSound(ModSounds.event("shoggoth.shoot"), 1.0F,
            1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(acid);
    }

    private void consumeDroppedItems() {
        for (ItemEntity itemEntity : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox())) {
            if (FoodCompat.isFood(itemEntity.getItem())) {
                setFoodLevel(getFoodLevel() + itemEntity.getItem().getCount());
                playSound(ModSounds.event("shoggoth.consume"), 1.0F, getVoicePitch());
            } else {
                playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
            }
            itemEntity.discard();
        }
    }

    private void spreadOoze() {
        if (!ACConfig.shoggothOoze.get()) return;
        BlockPos base = BlockPos.containing(getX() - 0.25D, getY(), getZ() - 0.25D);
        placeOoze(base);
        if (isLargeShoggoth()) {
            placeOoze(base.west());
            placeOoze(base.north());
            placeOoze(base.west().north());
        }
    }

    private void placeOoze(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (state.is(ShoggothBlocks.SHOGGOTH_OOZE.get())) {
            int layers = state.getValue(ShoggothOozeBlock.LAYERS);
            if (layers < 8 && tickCount % 10 == 0 && getRandom().nextInt(5) == 0) {
                level().setBlockAndUpdate(pos, state.setValue(ShoggothOozeBlock.LAYERS, layers + 1));
            }
            return;
        }
        BlockState ooze = ShoggothBlocks.SHOGGOTH_OOZE.get().defaultBlockState();
        if (state.canBeReplaced() && state.getFluidState().isEmpty() && ooze.canSurvive(level(), pos)) {
            level().setBlockAndUpdate(pos, ooze);
        }
    }

    private static int typeForDimension(net.minecraft.resources.ResourceKey<Level> dimension) {
        if (dimension == ACDimensions.ABYSSAL_WASTELAND) return 1;
        if (dimension == ACDimensions.DREADLANDS) return 2;
        if (dimension == ACDimensions.OMOTHOL) return 3;
        if (dimension == ACDimensions.DARK_REALM) return 4;
        return 0;
    }

    private void updateParts() {
        double angle = Math.toRadians(getYRot());
        double offsetX = -Math.sin(angle);
        double offsetZ = Math.cos(angle);
        PartProfile profile = partProfile();
        movePart(bodyPart, getX() - offsetX * profile.bodyOffset(), getY(),
            getZ() - offsetZ * profile.bodyOffset());
        movePart(headPart, getX() + offsetX * profile.headOffset(), getY() + profile.headHeightOffset(),
            getZ() + offsetZ * profile.headOffset());
    }

    private void movePart(EntityPartCompat.Part<?> part, double x, double y, double z) {
        part.setPos(x, y, z);
        part.setYRot(getYRot());
        part.setXRot(getXRot());
    }

    protected record PartProfile(float headWidth, float headHeight, float bodyWidth, float bodyHeight,
                                 double headOffset, double headHeightOffset, double bodyOffset) {}

    private static boolean isShoggothFood(LivingEntity entity) {
        return entity instanceof Animal || entity instanceof AmbientCreature || entity instanceof WaterAnimal
            || entity instanceof DemonAnimal || entity instanceof EvilAnimal || entity instanceof Spider;
    }

    // Faithful shoggoth sounds (PH-4b, wired to ModSounds); the shoot/consume/birth AI sounds land with
    // the deferred shoggoth mechanics that trigger them.
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.event("shoggoth.idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("shoggoth.hit");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("shoggoth.death");
    }
}
