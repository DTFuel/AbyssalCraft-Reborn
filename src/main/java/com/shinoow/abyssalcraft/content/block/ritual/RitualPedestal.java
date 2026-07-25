package com.shinoow.abyssalcraft.content.block.ritual;

import net.minecraft.world.item.ItemStack;

/**
 * A ritual pedestal that holds a single offering for a nearby {@link RitualAltarBlock} (owned by
 * content/block/ritual). The altar scans the eight ring positions for block entities implementing this
 * interface, gathers their offerings, and consumes them when a ritual completes. Implemented by the
 * pedestal block entity (built alongside the altar).
 */
public interface RitualPedestal {

    /** The offering currently on this pedestal (may be {@link ItemStack#EMPTY}). */
    ItemStack getOffering();

    /** Clear this pedestal's offering (called when a ritual consumes it). */
    void consumeOffering();
}
