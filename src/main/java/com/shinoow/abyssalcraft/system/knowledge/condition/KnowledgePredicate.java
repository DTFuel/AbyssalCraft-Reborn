package com.shinoow.abyssalcraft.system.knowledge.condition;

import java.util.Set;

/** Stable predicate keys used by the legacy type 5/6 unlock processors. */
public enum KnowledgePredicate {
    DARKLANDS_BIOMES(false, Set.of(
        "abyssalcraft:darklands", "abyssalcraft:darklands_forest",
        "abyssalcraft:darklands_plains", "abyssalcraft:darklands_hills",
        "abyssalcraft:darklands_mountains")),
    CORALIUM_BIOMES(false, Set.of(
        "abyssalcraft:abyssal_wastelands", "abyssalcraft:abyssal_swamp",
        "abyssalcraft:abyssal_desert", "abyssalcraft:abyssal_plateau",
        "abyssalcraft:coralium_lake", "abyssalcraft:coralium_infested_swamp")),
    DREAD_ENTITIES(true, Set.of(
        "abyssalcraft:dreadling", "abyssalcraft:dreadspawn", "abyssalcraft:greaterdreadspawn",
        "abyssalcraft:lesserdreadbeast", "abyssalcraft:dreaded_ghoul", "abyssalcraft:dreadguard",
        "abyssalcraft:chagaroth", "abyssalcraft:chagarothfist", "abyssalcraft:chagarothspawn",
        "abyssalcraft:demon_chicken", "abyssalcraft:demon_cow", "abyssalcraft:demon_pig",
        "abyssalcraft:demon_sheep")),
    ANTI_ENTITIES(true, Set.of(
        "abyssalcraft:antizombie", "abyssalcraft:antiabyssalzombie", "abyssalcraft:anticreeper",
        "abyssalcraft:antiskeleton", "abyssalcraft:antispider", "abyssalcraft:antighoul",
        "abyssalcraft:antiplayer", "abyssalcraft:anticow", "abyssalcraft:antipig",
        "abyssalcraft:antichicken", "abyssalcraft:antibat")),
    EVIL_ANIMALS(true, Set.of(
        "abyssalcraft:evil_chicken", "abyssalcraft:evil_cow",
        "abyssalcraft:evil_pig", "abyssalcraft:evil_sheep")),
    SHOGGOTHS(true, Set.of(
        "abyssalcraft:lesser_shoggoth", "abyssalcraft:shoggoth", "abyssalcraft:greater_shoggoth")),
    DEMON_ANIMALS(true, Set.of(
        "abyssalcraft:demon_chicken", "abyssalcraft:demon_cow",
        "abyssalcraft:demon_pig", "abyssalcraft:demon_sheep"));

    private final boolean entityPredicate;
    private final Set<String> ids;

    KnowledgePredicate(boolean entityPredicate, Set<String> ids) {
        this.entityPredicate = entityPredicate;
        this.ids = ids;
    }

    public boolean isEntityPredicate() {
        return entityPredicate;
    }

    public boolean matches(String id) {
        return ids.contains(id);
    }

    public Set<String> ids() {
        return ids;
    }
}