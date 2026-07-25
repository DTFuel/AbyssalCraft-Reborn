package com.shinoow.abyssalcraft.system.knowledge.condition;

/** Requires opening a Necronomicon of at least the supplied tier. */
public final class NecronomiconCondition extends UnlockCondition {

    public static final int TYPE = 12;

    public NecronomiconCondition(int bookType) {
        super(TYPE, bookType);
    }

    public int requiredBookType() {
        return (Integer) getConditionObject();
    }
}