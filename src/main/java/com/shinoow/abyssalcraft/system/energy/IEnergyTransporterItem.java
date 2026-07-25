package com.shinoow.abyssalcraft.system.energy;

/**
 * An item that can also transport Potential Energy between the network's blocks and players (owned by
 * PS-5), faithful to the 1.12.2 {@code api.energy.IEnergyTransporterItem}. It adds no members over
 * {@link IEnergyContainerItem}; it marks the item as a valid carrier (the Necronomicon, charms, etc.)
 * that the energy pedestal / deity statue may draw from or feed into.
 */
public interface IEnergyTransporterItem extends IEnergyContainerItem {
}
