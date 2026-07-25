package com.shinoow.abyssalcraft.system.energy;

/**
 * A block entity that can hold Potential Energy (owned by PS-5), faithful to the 1.12.2
 * {@code api.energy.IEnergyContainer}. Modernised off the loader capability machinery (like PC-4's
 * {@code ItemTransferHost}): the energy value lives on the block entity, so the "container" is queried
 * fork-free with {@code level.getBlockEntity(pos) instanceof IEnergyContainer}.
 */
public interface IEnergyContainer {

    /** The Potential Energy currently stored. */
    float getContainedEnergy();

    /** The maximum Potential Energy this container can hold. */
    int getMaxEnergy();

    /** Set the stored Potential Energy. */
    void setEnergy(float energy);

    /** Add energy; returns the overflow that did not fit. */
    default float addEnergy(float energy) {
        return PEUtils.addEnergy(this, energy);
    }

    /** Consume energy; returns the amount actually consumed. */
    default float consumeEnergy(float energy) {
        return PEUtils.consumeEnergy(this, energy);
    }

    /** Whether this container has room for (and accepts) more energy. */
    default boolean canAcceptPE() {
        return getContainedEnergy() < getMaxEnergy();
    }

    /** Whether this container has energy it can transfer out. */
    default boolean canTransferPE() {
        return getContainedEnergy() > 0;
    }
}
