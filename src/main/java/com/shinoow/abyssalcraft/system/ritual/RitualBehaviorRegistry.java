package com.shinoow.abyssalcraft.system.ritual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Ordered specialized-behavior registry. */
public final class RitualBehaviorRegistry {

    private static final RitualBehaviorRegistry INSTANCE = new RitualBehaviorRegistry();

    private final Map<String, RitualBehavior> behaviors = new LinkedHashMap<>();

    private RitualBehaviorRegistry() {
        RitualBehaviors.bootstrap(this);
    }

    public static RitualBehaviorRegistry instance() {
        return INSTANCE;
    }

    public void register(String ritualId, RitualBehavior behavior) {
        if (behaviors.putIfAbsent(ritualId, behavior) != null) {
            throw new IllegalStateException("Duplicate ritual behavior: " + ritualId);
        }
    }

    public RitualBehavior get(String ritualId) {
        return behaviors.get(ritualId);
    }

    public int size() {
        return behaviors.size();
    }

    public Set<String> ids() {
        return Set.copyOf(behaviors.keySet());
    }
}