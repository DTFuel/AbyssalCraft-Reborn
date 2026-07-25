package com.shinoow.abyssalcraft.system.knowledge.condition;

/** Unlocked by encountering any biome in the supplied set. */
public final class MultiBiomeCondition extends UnlockCondition {

    public MultiBiomeCondition(String... biomeIds) {
        super(3, biomeIds);
    }
}