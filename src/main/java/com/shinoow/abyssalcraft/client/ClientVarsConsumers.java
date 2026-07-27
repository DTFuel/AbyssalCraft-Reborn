package com.shinoow.abyssalcraft.client;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.shinoow.abyssalcraft.client.hud.ClientVars;
import com.shinoow.abyssalcraft.client.hud.ClientVarsManager;

import net.minecraft.resources.ResourceLocation;

public final class ClientVarsConsumers {

    private record BiomeColors(String grass, String foliage, String water, String sky) {}

    private static final Map<String, BiomeColors> BIOMES = biomeColors();
    private static final Map<String, String> EFFECTS = Map.of(
        "coralium_plague", "coraliumPlaguePotionColor",
        "dread_plague", "dreadPlaguePotionColor",
        "antimatter", "antimatterPotionColor",
        "coralium_antidote", "coraliumAntidotePotionColor",
        "dread_antidote", "dreadAntidotePotionColor");
    private static final Set<String> SKY = Set.of(
        "abyssalWastelandR", "abyssalWastelandG", "abyssalWastelandB",
        "dreadlandsR", "dreadlandsG", "dreadlandsB",
        "omotholR", "omotholG", "omotholB",
        "darkRealmR", "darkRealmG", "darkRealmB");
    private static final Set<String> FX = Set.of(
        "asorahDeathR", "asorahDeathG", "asorahDeathB",
        "jzaharDeathR", "jzaharDeathG", "jzaharDeathB",
        "implosionR", "implosionG", "implosionB");

    private ClientVarsConsumers() {}

    public static int biomeColor(ResourceLocation biome, String channel, int fallback) {
        if (biome == null || !"abyssalcraft".equals(biome.getNamespace())) return fallback;
        BiomeColors colors = BIOMES.get(biome.getPath());
        if (colors == null) return fallback;
        String field = switch (channel) {
            case "grass" -> colors.grass();
            case "foliage" -> colors.foliage();
            case "water" -> colors.water();
            case "sky" -> colors.sky();
            default -> null;
        };
        return field == null ? fallback : ClientVarsManager.get().color(field);
    }

    public static int effectColor(ResourceLocation effect, int fallback) {
        if (effect == null || !"abyssalcraft".equals(effect.getNamespace())) return fallback;
        String field = EFFECTS.get(effect.getPath());
        return field == null ? fallback : ClientVarsManager.get().color(field);
    }

    public static ConsumerStats validate() {
        Set<String> consumed = new LinkedHashSet<>();
        consumed.add("crystalColors");
        consumed.addAll(SKY);
        for (BiomeColors colors : BIOMES.values()) {
            if (colors.grass() != null) consumed.add(colors.grass());
            if (colors.foliage() != null) consumed.add(colors.foliage());
            if (colors.water() != null) consumed.add(colors.water());
            if (colors.sky() != null) consumed.add(colors.sky());
        }
        consumed.addAll(EFFECTS.values());
        consumed.addAll(FX);
        Set<String> blocked = new LinkedHashSet<>(ClientVars.contractKeys());
        blocked.removeAll(consumed);
        Set<String> unknown = new LinkedHashSet<>(consumed);
        unknown.removeAll(ClientVars.contractKeys());
        if (!unknown.isEmpty()) throw new IllegalStateException("unknown clientvars consumers " + unknown);
        return new ConsumerStats(ClientVars.contractKeys().size(), consumed.size(), blocked);
    }

    private static Map<String, BiomeColors> biomeColors() {
        Map<String, BiomeColors> colors = new LinkedHashMap<>();
        put(colors, "darklands", "darklands", true);
        put(colors, "darklands_plains", "darklandsPlains", true);
        put(colors, "darklands_forest", "darklandsForest", true);
        put(colors, "darklands_hills", "darklandsHighlands", true);
        put(colors, "darklands_mountains", "darklandsMountains", true);
        colors.put("coralium_infested_swamp", new BiomeColors("coraliumInfestedSwampGrassColor",
            "coraliumInfestedSwampFoliageColor", "coraliumInfestedSwampWaterColor", null));
        put(colors, "abyssal_wastelands", "abyssalWasteland", true);
        put(colors, "dreadlands", "dreadlands", false);
        put(colors, "dreadlands_forest", "dreadlandsForest", false);
        put(colors, "dreadlands_mountains", "dreadlandsMountains", false);
        put(colors, "omothol", "omothol", true);
        put(colors, "dark_realm", "darkRealm", true);
        put(colors, "purged", "purged", true);
        put(colors, "abyssal_desert", "abyssalDesert", true);
        put(colors, "abyssal_plateau", "abyssalPlateau", true);
        put(colors, "abyssal_swamp", "abyssalSwamp", true);
        put(colors, "coralium_lake", "coraliumLake", true);
        put(colors, "dreadlands_ocean", "dreadlandsOcean", false);
        return Map.copyOf(colors);
    }

    private static void put(Map<String, BiomeColors> colors, String biome, String prefix, boolean water) {
        colors.put(biome, new BiomeColors(prefix + "GrassColor", prefix + "FoliageColor",
            water ? prefix + "WaterColor" : null, prefix + "SkyColor"));
    }

    public record ConsumerStats(int defined, int consumed, Set<String> blocked) {}
}