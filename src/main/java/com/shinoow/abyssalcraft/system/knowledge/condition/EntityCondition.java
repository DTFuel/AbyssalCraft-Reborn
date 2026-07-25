package com.shinoow.abyssalcraft.system.knowledge.condition;

/**
 * Unlocked by encountering (killing) a given entity (owned by PS-8), faithful to the 1.12.2
 * {@code EntityCondition}. Processor type {@code 1}: met when the entity id is in the necrodata's entity
 * triggers.
 */
public class EntityCondition extends UnlockCondition {

    public EntityCondition(String entityId) {
        super(1, entityId);
    }
}
