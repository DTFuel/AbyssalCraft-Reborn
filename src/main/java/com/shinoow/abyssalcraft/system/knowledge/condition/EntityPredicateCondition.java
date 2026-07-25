package com.shinoow.abyssalcraft.system.knowledge.condition;

/** Unlock condition backed by a stable entity predicate key. */
public final class EntityPredicateCondition extends UnlockCondition {

    public EntityPredicateCondition(KnowledgePredicate predicate) {
        super(6, predicate);
        if (!predicate.isEntityPredicate()) {
            throw new IllegalArgumentException("Expected entity predicate: " + predicate);
        }
    }
}