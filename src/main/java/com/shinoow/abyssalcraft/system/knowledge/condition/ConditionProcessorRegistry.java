package com.shinoow.abyssalcraft.system.knowledge.condition;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/**
 * Registry of {@link IConditionProcessor}s keyed by {@link IUnlockCondition#getType()} (owned by PS-8),
 * faithful to the 1.12.2 {@code api.knowledge.condition.ConditionProcessorRegistry}. The built-in processors
 * for the string-list condition types are registered on construction (modernised out of the 1.12.2
 * {@code MiscHandler} into the registry itself, so PS-8 needs no mod-init hook).
 *
 * <p><b>Deferred:</b> the two predicate processors — type {@code 5} (biome {@code Predicate}) and type
 * {@code 6} (entity-class {@code Predicate}) — resolve a trigger name back to a live {@code Biome} /
 * {@code EntityType} via registries and are left out here (loader-registry-sensitive, and their conditions
 * reference unported biomes/entities); content registers them alongside those predicate conditions.
 */
public final class ConditionProcessorRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConditionProcessorRegistry INSTANCE = new ConditionProcessorRegistry();

    private final Map<Integer, IConditionProcessor> processors = new HashMap<>();

    private ConditionProcessorRegistry() {
        registerBuiltins();
    }

    public static ConditionProcessorRegistry instance() {
        return INSTANCE;
    }

    public void registerProcessor(int type, IConditionProcessor processor) {
        if (type < 0) {
            LOGGER.error("Invalid processor type: {}", type);
            return;
        }
        if (processors.putIfAbsent(type, processor) != null) {
            LOGGER.error("Processor already registered for type {}", type);
        }
    }

    public IConditionProcessor getProcessor(int type) {
        return processors.getOrDefault(type, (condition, data, player) -> false);
    }

    public boolean hasProcessor(int type) {
        return processors.containsKey(type);
    }

    private void registerBuiltins() {
        // 0 biome / 1 entity / 2 dimension / 7 artifact / 8 page / 9 whisper / 10 misc: single-string contains.
        processors.put(0, (c, d, p) -> d.getBiomeTriggers().contains(c.getConditionObject()));
        processors.put(1, (c, d, p) -> d.getEntityTriggers().contains(c.getConditionObject()));
        processors.put(2, (c, d, p) -> d.getDimensionTriggers().contains(c.getConditionObject()));
        processors.put(7, (c, d, p) -> d.getArtifactTriggers().contains(c.getConditionObject()));
        processors.put(8, (c, d, p) -> d.getPageTriggers().contains(c.getConditionObject()));
        processors.put(9, (c, d, p) -> d.getWhisperTriggers().contains(c.getConditionObject()));
        processors.put(10, (c, d, p) -> d.getMiscTriggers().contains(c.getConditionObject()));
        // 3 multi-biome (any) / 4 multi-entity (any): any of the String[] is triggered.
        processors.put(3, (c, d, p) -> {
            for (String name : (String[]) c.getConditionObject()) {
                if (d.getBiomeTriggers().contains(name)) {
                    return true;
                }
            }
            return false;
        });
        processors.put(4, (c, d, p) -> {
            for (String name : (String[]) c.getConditionObject()) {
                if (d.getEntityTriggers().contains(name)) {
                    return true;
                }
            }
            return false;
        });
        processors.put(5, (c, d, p) -> {
            KnowledgePredicate predicate = (KnowledgePredicate) c.getConditionObject();
            for (String trigger : d.getBiomeTriggers()) {
                if (predicate.matches(trigger)) {
                    return true;
                }
            }
            return false;
        });
        processors.put(6, (c, d, p) -> {
            KnowledgePredicate predicate = (KnowledgePredicate) c.getConditionObject();
            for (String trigger : d.getEntityTriggers()) {
                if (predicate.matches(trigger)) {
                    return true;
                }
            }
            return false;
        });
        // 11 mandatory multi-entity (all): every String[] entry must be triggered.
        processors.put(11, (c, d, p) -> {
            for (String name : (String[]) c.getConditionObject()) {
                if (!d.getEntityTriggers().contains(name)) {
                    return false;
                }
            }
            return true;
        });
    }
}
