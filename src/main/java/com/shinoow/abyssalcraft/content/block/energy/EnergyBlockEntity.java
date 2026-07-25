package com.shinoow.abyssalcraft.content.block.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainer;

/** Shared persistent PE storage for inventory-free energy block entities. */
public abstract class EnergyBlockEntity extends ACBlockEntity implements IEnergyContainer {

    private final int maxEnergy;
    private float energy;

    protected EnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxEnergy) {
        super(type, pos, state);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public final float getContainedEnergy() {
        return energy;
    }

    @Override
    public final int getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public void setEnergy(float energy) {
        float clamped = Mth.clamp(energy, 0.0F, maxEnergy);
        if (this.energy != clamped) {
            this.energy = clamped;
            setChanged();
        }
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putFloat("PotEnergy", energy);
        saveEnergyData(tag, registries);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        energy = Mth.clamp(tag.getFloat("PotEnergy"), 0.0F, maxEnergy);
        loadEnergyData(tag, registries);
    }

    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {}

    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {}
}