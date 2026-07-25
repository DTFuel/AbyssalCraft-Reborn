package com.shinoow.abyssalcraft.platform;

/**
 * Compat: datapack leaf directory names and biome-modifier location.
 *
 * <p>Vanilla axis - 1.21 singularized {@code loot_tables/recipes/advancements/...}. Loader axis -
 * biome modifiers live under {@code forge/} or {@code neoforge/}. Datagen emits the correct dir
 * automatically; use these constants only for hand-written data paths.
 */
public final class DataDirs {

    private DataDirs() {}

    //? if >=1.21 {
    /*public static final String LOOT_TABLE = "loot_table";
    public static final String RECIPE = "recipe";
    public static final String ADVANCEMENT = "advancement";
    public static final String PREDICATE = "predicate";
    public static final String ITEM_MODIFIER = "item_modifier";
    public static final String STRUCTURE = "structure";
    public static final String TAG_BLOCK = "tags/block";
    public static final String TAG_ITEM = "tags/item";
    public static final String TAG_ENTITY_TYPE = "tags/entity_type";
    public static final String TAG_FLUID = "tags/fluid";
    *///?} else {
    public static final String LOOT_TABLE = "loot_tables";
    public static final String RECIPE = "recipes";
    public static final String ADVANCEMENT = "advancements";
    public static final String PREDICATE = "predicates";
    public static final String ITEM_MODIFIER = "item_modifiers";
    public static final String STRUCTURE = "structures";
    public static final String TAG_BLOCK = "tags/blocks";
    public static final String TAG_ITEM = "tags/items";
    public static final String TAG_ENTITY_TYPE = "tags/entity_types";
    public static final String TAG_FLUID = "tags/fluids";
    //?}

    /** Loader id, used for the biome_modifier directory and type namespace. */
    //? if forge {
    public static final String LOADER = "forge";
    //?} else {
    /*public static final String LOADER = "neoforge";
    *///?}

    /** Directory for loader-specific biome_modifier JSON: {@code <loader>/biome_modifier}. */
    public static final String BIOME_MODIFIER = LOADER + "/biome_modifier";
}
