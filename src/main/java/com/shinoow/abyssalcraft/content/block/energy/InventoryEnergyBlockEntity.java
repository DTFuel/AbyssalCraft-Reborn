package com.shinoow.abyssalcraft.content.block.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.InventoryBlockEntity;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainer;

/** Shared persistent PE storage for energy block entities that also own an inventory. */
public abstract class InventoryEnergyBlockEntity extends InventoryBlockEntity implements IEnergyContainer {

    private final int maxEnergy;
    private float energy;

    protected InventoryEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         int inventorySize, int maxEnergy) {
        super(type, pos, state, inventorySize);
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
        super.saveData(tag, registries);
        tag.putFloat("PotEnergy", energy);
        saveEnergyData(tag, registries);
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadData(tag, registries);
        energy = Mth.clamp(tag.getFloat("PotEnergy"), 0.0F, maxEnergy);
        loadEnergyData(tag, registries);
    }

    protected void saveEnergyData(CompoundTag tag, HolderLookup.Provider registries) {}

    protected void loadEnergyData(CompoundTag tag, HolderLookup.Provider registries) {}
}