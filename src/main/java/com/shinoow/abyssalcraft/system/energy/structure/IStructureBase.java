package com.shinoow.abyssalcraft.system.energy.structure;

import com.shinoow.abyssalcraft.system.energy.AmplifierType;

/**
 * Marker for the master block entity of a Place of Power multiblock (owned by PS-10), faithful to the 1.12.2
 * {@code api.energy.structure.IStructureBase}. Holds the {@link IPlaceOfPower} it controls and bridges its
 * amplifier bonuses to the components (PS-5 manipulators).
 */
public interface IStructureBase {

    IPlaceOfPower getMultiblock();

    void setMultiblock(IPlaceOfPower multiblock);

    /** Bridge to {@link IPlaceOfPower#getAmplifier(AmplifierType)}. */
    float getAmplifier(AmplifierType type);
}
