package com.shinoow.abyssalcraft.system.knowledge.condition;

/**
 * Unlocked by visiting a given dimension (owned by PS-8), faithful to the 1.12.2 {@code DimensionCondition}.
 * Processor type {@code 2}: met when the dimension id is in the necrodata's dimension triggers. Modernised
 * from the 1.12.2 numeric dimension id to a dimension {@link net.minecraft.resources.ResourceLocation} string
 * (matching PS-2's necrodata dimension triggers).
 */
public class DimensionCondition extends UnlockCondition {

    public DimensionCondition(String dimensionId) {
        super(2, dimensionId);
    }
}
