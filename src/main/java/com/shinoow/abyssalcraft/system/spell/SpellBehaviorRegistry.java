package com.shinoow.abyssalcraft.system.spell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Ordered behavior registry; the permanent gate requires all fourteen manifests to be covered. */
public final class SpellBehaviorRegistry {

    private static final SpellBehaviorRegistry INSTANCE = new SpellBehaviorRegistry();

    private final Map<String, SpellBehavior> behaviors = new LinkedHashMap<>();

    private SpellBehaviorRegistry() {}

    public static SpellBehaviorRegistry instance() {
        return INSTANCE;
    }

    public void register(String id, SpellBehavior behavior) {
        if (behaviors.putIfAbsent(id, behavior) != null) {
            throw new IllegalStateException("Duplicate spell behavior: " + id);
        }
    }

    public SpellBehavior get(String id) {
        return behaviors.get(id);
    }

    public int size() {
        return behaviors.size();
    }

    public Set<String> ids() {
        return Set.copyOf(behaviors.keySet());
    }
}