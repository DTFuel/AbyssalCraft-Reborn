package com.shinoow.abyssalcraft.platform;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Compat: world {@link SavedData} persistence + access (vanilla axis).
 *
 * <p>1.21 added a {@code HolderLookup.Provider} parameter to {@code SavedData.save} and changed
 * {@code DimensionDataStorage.computeIfAbsent} to take a {@code SavedData.Factory}. Subclasses implement the
 * version-neutral {@link #saveData}; callers use {@link #getOrCreate} to fetch their data off a server level.
 */
public abstract class SavedDataCompat extends SavedData {

    //? if >=1.21 {
    /*@Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return saveData(tag);
    }
    *///?} else {
    @Override
    public CompoundTag save(CompoundTag tag) {
        return saveData(tag);
    }
    //?}

    /** Write persistent data (version-neutral). */
    protected abstract CompoundTag saveData(CompoundTag tag);

    /**
     * Get-or-create a {@link SavedData} stored on {@code level}'s server data storage under {@code name}.
     * {@code create} builds a fresh instance; {@code load} rebuilds one from saved NBT.
     */
    public static <T extends SavedData> T getOrCreate(ServerLevel level, String name, Supplier<T> create, Function<CompoundTag, T> load) {
        //? if >=1.21 {
        /*return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(create, (tag, registries) -> load.apply(tag)), name);
        *///?} else {
        return level.getDataStorage().computeIfAbsent(load, create, name);
        //?}
    }
}
