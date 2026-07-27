package com.shinoow.abyssalcraft.system.spell;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

/** Immutable source-derived declaration shared by spell registration, casting and Spellbook UI. */
public record SpellManifest(
    int order,
    String id,
    Set<String> aliases,
    int bookType,
    float requiredEnergy,
    ScrollType scrollType,
    TargetType targetType,
    boolean requiresCharging,
    boolean canOthersCast,
    int color,
    List<SpellIngredient> reagentLayout,
    String parentId,
    ResourceLocation research,
    ResourceLocation glyph
) {

    public static final int REAGENT_SLOTS = 5;

    public SpellManifest {
        if (order < 1) throw new IllegalArgumentException("Spell order must be positive");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Spell id is required");
        aliases = Set.copyOf(new LinkedHashSet<>(aliases));
        if (aliases.contains(id)) throw new IllegalArgumentException("Spell alias repeats canonical id: " + id);
        if (bookType < 0 || bookType > 4) throw new IllegalArgumentException("Invalid spell book type: " + id);
        if (requiredEnergy < 0) throw new IllegalArgumentException("Negative spell PE: " + id);
        Objects.requireNonNull(scrollType, "scrollType");
        Objects.requireNonNull(targetType, "targetType");
        reagentLayout = List.copyOf(reagentLayout);
        if (reagentLayout.size() != REAGENT_SLOTS || reagentLayout.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Spell " + id + " must declare exactly five reagent slots");
        }
    }

    public List<SpellIngredient> reagents() {
        return reagentLayout.stream().filter(reagent -> !reagent.isEmpty()).toList();
    }

    public Set<ResourceLocation> referencedItems() {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        for (SpellIngredient reagent : reagentLayout) result.addAll(reagent.referencedItems());
        return Set.copyOf(result);
    }

    public enum TargetType {
        ENTITY,
        ENTITY_OR_SELF,
        BLOCK,
        SELF
    }
}