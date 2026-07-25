package com.shinoow.abyssalcraft.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shinoow.abyssalcraft.AbyssalCraft;
import com.shinoow.abyssalcraft.platform.ACRef;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/** Validated views over the legacy non-scalar config entries. */
public final class ComplexConfig {

    public record DemonTransformation(int demonType, float chance) {}
    public record DimensionBookMapping(ResourceLocation dimension, int bookType, String name) {}

    private static volatile Snapshot snapshot;

    private ComplexConfig() {}

    public static synchronized void reload() {
        snapshot = parse();
    }

    public static Set<ResourceLocation> dreadImmunity() {
        return current().dreadImmunity();
    }

    public static Set<ResourceLocation> dreadCarriers() {
        return current().dreadCarriers();
    }

    public static Set<ResourceLocation> coraliumImmunity() {
        return current().coraliumImmunity();
    }

    public static Set<ResourceLocation> coraliumCarriers() {
        return current().coraliumCarriers();
    }

    public static Map<ResourceLocation, DemonTransformation> demonTransformations() {
        return current().demonTransformations();
    }

    public static Set<ResourceLocation> interdimensionalCageBlacklist() {
        return current().cageBlacklist();
    }

    public static Set<ResourceLocation> itemTransportBlacklist() {
        return current().itemTransportBlacklist();
    }

    public static Set<ResourceLocation> mobItemPickupBlacklist() {
        return current().mobItemPickupBlacklist();
    }

    public static Set<ResourceLocation> blackHoleDimensionBlacklist() {
        return current().blackHoleDimensions();
    }

    public static Set<ResourceLocation> oreGenerationDimensionBlacklist() {
        return current().oreDimensions();
    }

    public static Set<ResourceLocation> structureGenerationDimensionBlacklist() {
        return current().structureDimensions();
    }

    public static List<DimensionBookMapping> dimensionBookTypeMappings() {
        return current().dimensionBookMappings();
    }

    private static Snapshot current() {
        Snapshot value = snapshot;
        if (value == null) {
            synchronized (ComplexConfig.class) {
                if (snapshot == null) snapshot = parse();
                value = snapshot;
            }
        }
        return value;
    }

    private static Snapshot parse() {
        Set<ResourceLocation> dreadCarriers = entityIds(ACConfig.dreadPlagueCarrierList.get());
        Set<ResourceLocation> coraliumCarriers = entityIds(ACConfig.coraliumPlagueCarrierList.get());
        Set<ResourceLocation> dreadImmunity = unionEntities(ACConfig.dreadPlagueImmunityList.get(), ACConfig.dreadPlagueCarrierList.get());
        Set<ResourceLocation> coraliumImmunity = unionEntities(ACConfig.coraliumPlagueImmunityList.get(), ACConfig.coraliumPlagueCarrierList.get());
        Map<ResourceLocation, DemonTransformation> result = new HashMap<>();
        for (String row : ACConfig.demonAnimalTransformations.get()) {
            String[] fields = row.split(";");
            if (fields.length < 2 || fields.length > 3) {
                warn("Invalid demon transformation", row);
                continue;
            }
            ResourceLocation entity = parseEntity(fields[0]);
            try {
                int type = Integer.parseInt(fields[1]);
                float chance = fields.length == 3 ? Float.parseFloat(fields[2]) : 1.0F;
                if (entity == null || type < 0 || type > 3 || chance < 0.0F || chance > 1.0F) {
                    warn("Invalid demon transformation", row);
                } else {
                    result.put(entity, new DemonTransformation(type, chance));
                }
            } catch (NumberFormatException ex) {
                warn("Invalid demon transformation", row);
            }
        }
        return new Snapshot(dreadImmunity, dreadCarriers, coraliumImmunity, coraliumCarriers,
            Map.copyOf(result), resourceIds(ACConfig.interdimensionalCageBlacklist.get()),
            resourceIds(ACConfig.itemTransportBlacklist.get()), resourceIds(ACConfig.mobItemPickupBlacklist.get()),
            resourceIds(ACConfig.blackHoleDimensionBlacklist.get()), resourceIds(ACConfig.oreGenerationDimensionBlacklist.get()),
            resourceIds(ACConfig.structureGenerationDimensionBlacklist.get()), parseDimensionMappings());
    }

    public static int[] portalColor() {
        return parsePortalColor(ACConfig.startDimensionColors.get());
    }

    public static int[] parsePortalColor(List<? extends Integer> values) {
        if (values.size() != 3) return new int[] {255, 255, 255};
        return new int[] {clampColor(values.get(0)), clampColor(values.get(1)), clampColor(values.get(2))};
    }

    public static int[] coraliumOre() {
        return parseCoraliumOre(ACConfig.coraliumOreGeneration.get());
    }

    public static int[] parseCoraliumOre(List<? extends Integer> values) {
        if (values.size() != 3) return new int[] {12, 8, 40};
        return new int[] {Math.max(0, values.get(0)), Math.max(0, values.get(1)), Math.max(0, values.get(2))};
    }

    private static Set<ResourceLocation> unionEntities(List<? extends String> first, List<? extends String> second) {
        Set<ResourceLocation> result = new HashSet<>(entityIds(first));
        result.addAll(entityIds(second));
        return Set.copyOf(result);
    }

    private static Set<ResourceLocation> entityIds(List<? extends String> values) {
        Set<ResourceLocation> result = new HashSet<>();
        for (String value : values) {
            ResourceLocation id = parseEntity(value);
            if (id != null) result.add(id);
        }
        return Set.copyOf(result);
    }

    private static Set<ResourceLocation> resourceIds(List<? extends String> values) {
        Set<ResourceLocation> result = new HashSet<>();
        for (String value : values) {
            try {
                result.add(ACRef.parse(value));
            } catch (RuntimeException ex) {
                warn("Invalid resource id", value);
            }
        }
        return Set.copyOf(result);
    }

    private static List<DimensionBookMapping> parseDimensionMappings() {
        java.util.ArrayList<DimensionBookMapping> result = new java.util.ArrayList<>();
        for (String row : ACConfig.dimensionBookTypeMappings.get()) {
            String[] fields = row.split(";", 3);
            try {
                ResourceLocation dimension = ACRef.parse(fields[0]);
                int tier = fields.length >= 2 ? Integer.parseInt(fields[1]) : -1;
                if (fields.length < 2 || tier < 0 || tier > 4) {
                    warn("Invalid dimension book mapping", row);
                    continue;
                }
                result.add(new DimensionBookMapping(dimension, tier, fields.length == 3 ? fields[2] : ""));
            } catch (RuntimeException ex) {
                warn("Invalid dimension book mapping", row);
            }
        }
        return List.copyOf(result);
    }

    private static ResourceLocation parseEntity(String value) {
        ResourceLocation id;
        try {
            id = ACRef.parse(value);
        } catch (RuntimeException ex) {
            warn("Invalid entity id", value);
            return null;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (type == null || type == EntityType.PIG && !id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) {
            warn("Unknown entity id", value);
            return null;
        }
        return id;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void warn(String message, String value) {
        AbyssalCraft.LOGGER.warn("[RR-KNOWLEDGE] {}: {}", message, value);
    }

    private record Snapshot(
        Set<ResourceLocation> dreadImmunity,
        Set<ResourceLocation> dreadCarriers,
        Set<ResourceLocation> coraliumImmunity,
        Set<ResourceLocation> coraliumCarriers,
        Map<ResourceLocation, DemonTransformation> demonTransformations,
        Set<ResourceLocation> cageBlacklist,
        Set<ResourceLocation> itemTransportBlacklist,
        Set<ResourceLocation> mobItemPickupBlacklist,
        Set<ResourceLocation> blackHoleDimensions,
        Set<ResourceLocation> oreDimensions,
        Set<ResourceLocation> structureDimensions,
        List<DimensionBookMapping> dimensionBookMappings
    ) {}
}