package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;
import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.structure.IPlaceOfPower;
import com.shinoow.abyssalcraft.system.energy.structure.IStructureBase;
import com.shinoow.abyssalcraft.system.energy.structure.StructureHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent master state for an activated Place of Power. */
public final class PlaceOfPowerBaseBlockEntity extends ACBlockEntity
    implements IStructureBase, TickingBlockEntity {

    private static final int VALIDATION_INTERVAL = 100;

    private IPlaceOfPower place;

    public PlaceOfPowerBaseBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlocks.PLACE_OF_POWER_BASE_BE.get(), pos, state);
    }

    @Override
    public void serverTick() {
        if (level != null && place != null && level.getGameTime() % VALIDATION_INTERVAL == 0) {
            place.validate(level, worldPosition);
            int ambientCooldown = place.getAmbientEffectCooldown();
            if (ambientCooldown > 0 && level.getGameTime() % ambientCooldown == 0) {
                place.triggerAmbientEffect(level, worldPosition);
            }
        }
    }

    @Override
    public IPlaceOfPower getMultiblock() {
        return place;
    }

    @Override
    public void setMultiblock(IPlaceOfPower multiblock) {
        place = multiblock;
        setChanged();
    }

    @Override
    public float getAmplifier(AmplifierType type) {
        return place == null ? 0.0F : place.getAmplifier(type);
    }

    public void detachComponents() {
        if (level != null && place != null) {
            place.detach(level, worldPosition);
        }
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (place != null) {
            tag.putString("Structure", place.getIdentifier());
        }
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        place = StructureHandler.instance().getStructureByName(tag.getString("Structure"));
    }
}