package com.shinoow.abyssalcraft.content.entity.ghoul;

import com.shinoow.abyssalcraft.common.handlers.EffectHooks;
import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.system.effect.ACEffects;
import com.shinoow.abyssalcraft.registry.ModSounds;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The Depths Ghoul (owned by PD-5, Stage D2a).
 *
 * <p>Faithful port of 1.12.2 {@code EntityDepthsGhoul}: 35 health, 6 attack damage, four named
 * variants, a rare baby form, underwater breathing and coralium plague on hit.
 */
public class DepthsGhoul extends AbstractGhoul {

    private static final EntityDataAccessor<Integer> TYPE =
        SynchedEntityData.defineId(DepthsGhoul.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BABY =
        SynchedEntityData.defineId(DepthsGhoul.class, EntityDataSerializers.BOOLEAN);
    //? if <1.21 {
    private static final UUID BABY_SPEED_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED = new AttributeModifier(
        BABY_SPEED_ID, "Depths Ghoul baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
    //?} else {
    /*private static final net.minecraft.resources.ResourceLocation BABY_SPEED_ID =
        com.shinoow.abyssalcraft.platform.ACRef.id("depths_ghoul_baby_speed");
    private static final AttributeModifier BABY_SPEED = new AttributeModifier(
        BABY_SPEED_ID, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    *///?}

    public DepthsGhoul(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    //? if <1.21 {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, 0);
        entityData.define(BABY, false);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, 0);
        builder.define(BABY, false);
    }
    *///?}

    public int getGhoulType() {
        return entityData.get(TYPE);
    }

    public void setGhoulType(int type) {
        entityData.set(TYPE, Math.max(0, Math.min(3, type)));
    }

    @Override
    public boolean isBaby() {
        return entityData.get(BABY);
    }

    @Override
    public void setBaby(boolean baby) {
        entityData.set(BABY, baby);
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            //? if <1.21 {
            speed.removeModifier(BABY_SPEED_ID);
            //?} else {
            /*speed.removeModifier(BABY_SPEED_ID);
            *///?}
            if (baby) speed.addTransientModifier(BABY_SPEED);
        }
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        burnInSunlightWhenConfigured();
        super.aiStep();
    }

    @Override
    public Component getName() {
        return switch (getGhoulType()) {
            case 1 -> Component.literal("Pete");
            case 2 -> Component.literal("Mr. Wilson");
            case 3 -> Component.literal("Dr. Orange");
            default -> super.getName();
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (getGhoulType()) {
            case 1 -> ModSounds.event("ghoul.pete.idle");
            case 2 -> ModSounds.event("ghoul.wilson.idle");
            case 3 -> ModSounds.event("ghoul.orange.idle");
            default -> super.getAmbientSound();
        };
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
        if (getRandom().nextFloat() < 0.2F) setGhoulType(getRandom().nextInt(4));
        setBaby(getRandom().nextFloat() < 0.05F);
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GhoulType", getGhoulType());
        tag.putBoolean("IsBaby", isBaby());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GhoulType")) setGhoulType(tag.getInt("GhoulType"));
        setBaby(tag.getBoolean("IsBaby"));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ghoulAttributes()
            .add(Attributes.MAX_HEALTH, 35.0D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected String legacyLootTable() {
        return switch (getGhoulType()) {
            case 1 -> "depths_ghoul_pete";
            case 2 -> "depths_ghoul_wilson";
            case 3 -> "depths_ghoul_orange";
            default -> "depths_ghoul";
        };
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && !EffectHooks.isCoraliumImmune(living)
                && (ACConfig.shouldInfect.get() || isInAbyssalWasteland())) {
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.CORALIUM_PLAGUE, 100, 0));
        }
        return hurt;
    }

    private boolean isInAbyssalWasteland() {
        return level().dimension().location().getNamespace().equals("abyssalcraft")
            && level().dimension().location().getPath().equals("abyssal_wasteland");
    }
}
