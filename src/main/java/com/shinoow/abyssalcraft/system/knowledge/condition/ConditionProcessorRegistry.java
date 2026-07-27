package com.shinoow.abyssalcraft.system.knowledge.condition;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * Registry of {@link IConditionProcessor}s keyed by {@link IUnlockCondition#getType()} (owned by PS-8),
 * faithful to the 1.12.2 {@code api.knowledge.condition.ConditionProcessorRegistry}. The built-in processors
 * for the string-list condition types are registered on construction (modernised out of the 1.12.2
 * {@code MiscHandler} into the registry itself, so PS-8 needs no mod-init hook).
 *
 * <p>The two predicate processors resolve persisted resource ids through the live registries before applying
 * their catalog predicates. This keeps stale or malformed ids from satisfying a condition after reconnect.
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
            if (p == null) {
                return false;
            }
            KnowledgePredicate predicate = (KnowledgePredicate) c.getConditionObject();
            Registry<Biome> biomes = p.level().registryAccess().registryOrThrow(Registries.BIOME);
            for (String trigger : d.getBiomeTriggers()) {
                if (matchesRegistered(predicate, trigger, biomes)) {
                    return true;
                }
            }
            return false;
        });
        processors.put(6, (c, d, p) -> {
            KnowledgePredicate predicate = (KnowledgePredicate) c.getConditionObject();
            for (String trigger : d.getEntityTriggers()) {
                if (matchesRegistered(predicate, trigger, BuiltInRegistries.ENTITY_TYPE)) {
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

    public static <T> boolean matchesRegistered(KnowledgePredicate predicate, String trigger, Registry<T> registry) {
        ResourceLocation id = ResourceLocation.tryParse(trigger);
        if (id == null || !registry.containsKey(id)) {
            return false;
        }
        T value = registry.get(id);
        return value != null && predicate.matches(registry.getKey(value).toString());
    }
}
