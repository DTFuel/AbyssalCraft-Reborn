package com.shinoow.abyssalcraft.system.spell;

import net.minecraft.world.item.ItemStack;

/**
 * Marker for scroll items (owned by PS-7), faithful to the 1.12.2 {@code api.spell.IScroll}.
 * A scroll item reports the {@link ScrollType} quality it can inscribe / cast spells at. The concrete
 * scroll items are deferred content (unported); this interface lets the framework gate spells by quality.
 */
public interface IScroll {

    ScrollType getScrollType(ItemStack stack);
}
