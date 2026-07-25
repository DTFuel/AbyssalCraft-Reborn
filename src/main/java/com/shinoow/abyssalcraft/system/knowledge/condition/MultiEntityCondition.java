package com.shinoow.abyssalcraft.system.knowledge.condition;

/** Unlocked by encountering any entity in the supplied set. */
public final class MultiEntityCondition extends UnlockCondition {

    public MultiEntityCondition(String... entityIds) {
        super(4, entityIds);
    }
}