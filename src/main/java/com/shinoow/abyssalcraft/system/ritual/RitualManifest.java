package com.shinoow.abyssalcraft.system.ritual;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Immutable legacy ritual declaration used by registration, the Necronomicon and validation. */
public record RitualManifest(
    int order,
    String id,
    String legacyId,
    Kind kind,
    int bookType,
    ResourceKey<Level> dimension,
    float requiredEnergy,
    boolean requiresSacrifice,
    RitualIngredient center,
    List<RitualIngredient> offeringLayout,
    ResourceLocation result,
    List<ResourceLocation> actionTargets,
    boolean strictOfferings,
    boolean strictCenterData,
    boolean copyCenterData,
    Set<String> copiedDataKeys,
    ResourceLocation research,
    boolean hidden
) {

    public static final int PEDESTAL_COUNT = 8;

    public RitualManifest {
        if (order < 1) throw new IllegalArgumentException("Ritual order must be positive");
        if (id == null || !id.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid ritual id: " + id);
        }
        if (legacyId == null || legacyId.isBlank()) {
            throw new IllegalArgumentException("Missing legacy ritual id: " + id);
        }
        Objects.requireNonNull(kind, "kind");
        if (bookType < 0 || bookType > 4) {
            throw new IllegalArgumentException("Ritual book type must be between 0 and 4: " + id);
        }
        if (requiredEnergy < 0) throw new IllegalArgumentException("Negative ritual PE: " + id);
        Objects.requireNonNull(center, "center");
        offeringLayout = List.copyOf(offeringLayout);
        if (offeringLayout.size() != PEDESTAL_COUNT) {
            throw new IllegalArgumentException("Ritual " + id + " must declare exactly 8 pedestal slots");
        }
        if (offeringLayout.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Ritual " + id + " uses null instead of an empty ingredient");
        }
        actionTargets = List.copyOf(actionTargets);
        if (actionTargets.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Ritual " + id + " has a null action target");
        }
        copiedDataKeys = Set.copyOf(new LinkedHashSet<>(copiedDataKeys));
        if (!copyCenterData && !copiedDataKeys.isEmpty()) {
            throw new IllegalArgumentException("Ritual " + id + " declares copied data without center copying");
        }
        if (hidden && kind != Kind.HOUSE) {
            throw new IllegalArgumentException("Only the legacy house ritual may be hidden");
        }
    }

    public List<RitualIngredient> offerings() {
        return offeringLayout.stream().filter(ingredient -> !ingredient.isEmpty()).toList();
    }

    public Set<ResourceLocation> referencedItems() {
        Set<ResourceLocation> references = new LinkedHashSet<>(center.referencedItems());
        for (RitualIngredient ingredient : offeringLayout) references.addAll(ingredient.referencedItems());
        if (result != null) references.add(result);
        return Set.copyOf(references);
    }

    public enum Kind {
        INFUSION,
        CREATION,
        TRANSFORMATION,
        PORTAL,
        SUMMON,
        RESPAWN_JZAHAR,
        BREEDING,
        DREAD_SPAWN,
        POTION_AOE,
        RESURRECTION,
        CLEANSING,
        CORRUPTION,
        INFESTING,
        CURING,
        PURGING,
        MASS_ENCHANTING,
        WEATHER,
        HOUSE
    }
}