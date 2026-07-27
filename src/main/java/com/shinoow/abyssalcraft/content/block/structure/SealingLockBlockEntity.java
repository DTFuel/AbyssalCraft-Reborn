package com.shinoow.abyssalcraft.content.block.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.platform.BlockEntityCompat;

/** Persistent host for a structure sealing lock's one-way unlocked state. */
public final class SealingLockBlockEntity extends BlockEntityCompat {

    private boolean unlocked;
    private String markerTarget = "locked";

    public SealingLockBlockEntity(BlockPos pos, BlockState state) {
        super(StructureContent.SEALING_LOCK_BE.get(), pos, state);
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public String markerTarget() {
        return markerTarget;
    }

    public void configureMarker(String metadata) {
        int separator = metadata.indexOf(':');
        markerTarget = separator >= 0 && separator + 1 < metadata.length()
            ? metadata.substring(separator + 1) : "locked";
        unlocked = "unlocked".equals(markerTarget);
        if (level != null && getBlockState().hasProperty(SealingLockBlock.LOCKED)) {
            level.setBlock(worldPosition, getBlockState().setValue(SealingLockBlock.LOCKED, !unlocked), 3);
        }
        setChanged();
    }

    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            setChanged();
        }
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        saveMarkerData(tag);
    }

    public void saveMarkerData(CompoundTag tag) {
        tag.putBoolean("Unlocked", unlocked);
        tag.putString("MarkerTarget", markerTarget);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        loadMarkerData(tag);
    }

    public void loadMarkerData(CompoundTag tag) {
        unlocked = tag.getBoolean("Unlocked");
        markerTarget = tag.contains("MarkerTarget") ? tag.getString("MarkerTarget") : "locked";
    }
}