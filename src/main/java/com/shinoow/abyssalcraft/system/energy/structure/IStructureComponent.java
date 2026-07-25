package com.shinoow.abyssalcraft.system.energy.structure;

import com.shinoow.abyssalcraft.system.energy.AmplifierType;

import net.minecraft.core.BlockPos;

/**
 * Marker for block entities that gain stat boosts while part of a Place of Power multiblock (owned by PS-10),
 * faithful to the 1.12.2 {@code api.energy.structure.IStructureComponent}. Energy manipulators/containers
 * (PS-5) implement this to track their membership in a {@link IPlaceOfPower}.
 */
public interface IStructureComponent {

    boolean isInMultiblock();

    void setInMultiblock(boolean inMultiblock);

    /** The base block position of the Place of Power this component belongs to, or {@code null}. */
    BlockPos getBasePosition();

    void setBasePosition(BlockPos pos);
}
