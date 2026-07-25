package com.shinoow.abyssalcraft.system.knowledge.condition;

/** Unlock condition backed by a stable biome predicate key. */
public final class BiomePredicateCondition extends UnlockCondition {

    public BiomePredicateCondition(KnowledgePredicate predicate) {
        super(5, predicate);
        if (predicate.isEntityPredicate()) {
            throw new IllegalArgumentException("Expected biome predicate: " + predicate);
        }
    }
}