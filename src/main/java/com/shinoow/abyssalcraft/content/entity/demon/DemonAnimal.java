package com.shinoow.abyssalcraft.content.entity.demon;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.base.ACMob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;

/**
 * Demon animal (owned by PD-4, Stage D2a) -- the corrupted, hostile counterpart of a vanilla farm
 * animal (ported from 1.12.2 {@code EntityDemonAnimal} + its four subclasses). A fire-immune
 * {@link Monster} that wanders, watches players and attacks in melee; species-specific looks/sounds come
 * from {@link AnimalKind} (baked into the {@code EntityType} factory in {@link DemonEntities}).
 */
public class DemonAnimal extends ACMob {

    private final AnimalKind kind;
    private boolean canBurn;

    public DemonAnimal(EntityType<? extends Monster> type, Level level, AnimalKind kind) {
        super(type, level);
        this.kind = kind;
    }

    public AnimalKind kind() {
        return kind;
    }

    public boolean canBurn() {
        return canBurn;
    }

    @Override
    public void tick() {
        super.tick();
        if (isOnFire() && fireEnabled()) canBurn = true;
        if (canBurn && isInWaterRainOrBubble()) canBurn = false;
        if (!level().isClientSide && fireEnabled()) {
            if (!canBurn && hasFlammableBlock()) canBurn = true;
            if (canBurn) spreadFire();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (fireEnabled() && held.is(Items.FLINT_AND_STEEL) && !canBurn) {
            player.playSound(SoundEvents.FLINTANDSTEEL_USE, 1.0F, getRandom().nextFloat() * 0.4F + 0.8F);
            canBurn = true;
            if (!level().isClientSide) {
                //? if <1.21 {
                held.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
                //?} else {
                /*held.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel) level(), player,
                    item -> player.onEquippedItemBroken(item, hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
                *///?}
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
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
        if (level.getLevel().dimension() == Level.OVERWORLD && ACConfig.demonAnimalFire.get()
                && getRandom().nextInt(3) == 0
            || level.getLevel().dimension() == Level.NETHER && getRandom().nextBoolean()) {
            canBurn = true;
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("CanBurn", canBurn);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        canBurn = tag.getBoolean("CanBurn");
    }

    private boolean fireEnabled() {
        return ACConfig.demonAnimalFire.get() || ACConfig.hardcoreMode.get();
    }

    private boolean hasFlammableBlock() {
        BlockPos min = BlockPos.containing(getBoundingBox().minX, getBoundingBox().minY, getBoundingBox().minZ);
        BlockPos max = BlockPos.containing(getBoundingBox().maxX, getBoundingBox().maxY, getBoundingBox().maxZ);
        return BlockPos.betweenClosedStream(min, max)
            .anyMatch(pos -> level().getBlockState(pos).ignitedByLava());
    }

    private void spreadFire() {
        for (int corner = 0; corner < 4; corner++) {
            double offsetX = (corner % 2 * 2 - 1) * 0.25D;
            double offsetZ = (corner / 2 % 2 * 2 - 1) * 0.25D;
            BlockPos pos = BlockPos.containing(getX() + offsetX, getY(), getZ() + offsetZ);
            if (level().isEmptyBlock(pos) && BaseFireBlock.canBePlacedAt(level(), pos, Direction.UP)) {
                level().setBlockAndUpdate(pos, BaseFireBlock.getState(level(), pos));
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attribute template for demon/evil animals: monster baseline + faithful health. */
    public static AttributeSupplier.Builder createAttributes(double maxHealth) {
        return ACMob.createAttributes()
            .add(Attributes.MAX_HEALTH, maxHealth)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return kind.ambient();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind.death();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_HURT;
    }
}
