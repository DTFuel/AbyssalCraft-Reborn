package com.shinoow.abyssalcraft.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Compat base for non-living AbyssalCraft entities that extend vanilla {@link Entity} directly
 * (black holes, portals, primed explosives, trackers, ...): it absorbs the single loader/version
 * fork on {@link Entity}'s abstract {@code defineSynchedData}. Forge declares {@code defineSynchedData()}
 * (no-arg) while NeoForge 1.21 declares {@code defineSynchedData(SynchedEntityData.Builder)}; both are
 * abstract, so any raw {@code extends Entity} subclass must implement the forked signature.
 *
 * <p>These misc entities carry no synched data (their transient state lives in plain fields), so the
 * implementation is empty on both sides. The two abstract NBT hooks are stable across versions and are
 * left empty here (subclasses override as needed). Business subclasses in {@code content/entity/**} stay
 * fork-free by extending this base.
 */
public abstract class ACSimpleEntity extends Entity {

    @FunctionalInterface
    protected interface SyncedDataBuilder {
        <T> void define(EntityDataAccessor<T> accessor, T initialValue);
    }

    protected ACSimpleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    //? if <1.21 {
    @Override
    protected final void defineSynchedData() {
        defineSimpleSyncedData(new SyncedDataBuilder() {
            @Override
            public <T> void define(EntityDataAccessor<T> accessor, T initialValue) {
                entityData.define(accessor, initialValue);
            }
        });
    }
    //?} else {
    /*@Override
    protected final void defineSynchedData(SynchedEntityData.Builder builder) {
        defineSimpleSyncedData(new SyncedDataBuilder() {
            @Override
            public <T> void define(EntityDataAccessor<T> accessor, T initialValue) {
                builder.define(accessor, initialValue);
            }
        });
    }
    *///?}

    /** Subclasses register their synchronized fields here. */
    protected void defineSimpleSyncedData(SyncedDataBuilder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
