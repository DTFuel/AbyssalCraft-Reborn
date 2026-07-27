package com.shinoow.abyssalcraft.world.darklands;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import com.shinoow.abyssalcraft.platform.ACRef;

public final class DarklandsBiomes {

    public static final ResourceKey<Biome> DARKLANDS = key("darklands");
    public static final ResourceKey<Biome> FOREST = key("darklands_forest");
    public static final ResourceKey<Biome> PLAINS = key("darklands_plains");
    public static final ResourceKey<Biome> HILLS = key("darklands_hills");
    public static final ResourceKey<Biome> MOUNTAINS = key("darklands_mountains");
    public static final ResourceKey<Biome> CORALIUM_INFESTED_SWAMP = key("coralium_infested_swamp");
    public static final ResourceKey<Biome> DREADLANDS = key("dreadlands");
    public static final ResourceKey<Biome> DREADLANDS_FOREST = key("dreadlands_forest");
    public static final ResourceKey<Biome> DREADLANDS_MOUNTAINS = key("dreadlands_mountains");
    public static final ResourceKey<Biome> DREADLANDS_OCEAN = key("dreadlands_ocean");
    public static final ResourceKey<Biome> PURGED = key("purged");

    private DarklandsBiomes() {}

    private static ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, ACRef.id(id));
    }
}
