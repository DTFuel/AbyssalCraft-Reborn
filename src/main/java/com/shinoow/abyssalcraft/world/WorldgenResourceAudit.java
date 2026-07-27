package com.shinoow.abyssalcraft.world;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.platform.ACRef;

/** Fail-closed resource, registry and placement fixture for config-owned world generation. */
public final class WorldgenResourceAudit {

    private static final List<String> FEATURE_IDS = List.of(
        "abyssal_wasteland_pillars", "ore_nitre", "ore_abyssalnite", "ore_abyssal_coralium",
        "ore_dreadlands_abyssalnite", "ore_dreaded_abyssalnite", "ore_abyssal_iron",
        "ore_abyssal_gold", "ore_abyssal_diamond", "ore_abyssal_nitre",
        "ore_pearlescent_coralium", "ore_liquified_coralium");
    private static final List<String> ORE_BLOCKS = List.of(
        "nitre_ore", "abyssalnite_ore", "abyssal_coralium_ore", "dreadlands_abyssalnite_ore",
        "dreaded_abyssalnite_ore", "abyssal_iron_ore", "abyssal_gold_ore", "abyssal_diamond_ore",
        "abyssal_nitre_ore", "pearlescent_coralium_ore", "liquified_coralium_ore");
    private static final List<String> MODIFIERS = List.of(
        "feature_world_ores", "feature_abyssal_ores", "feature_dreadlands_ores",
        "feature_abyssal_wasteland_pillars");

    private WorldgenResourceAudit() {}

    public static void validate() {
        for (String id : FEATURE_IDS) {
            readJson("worldgen/configured_feature/" + id + ".json");
            JsonObject placed = readJson("worldgen/placed_feature/" + id + ".json");
            require(ACRef.id(id).toString().equals(placed.get("feature").getAsString()),
                "placed feature does not reference its configured feature: " + id);
        }
        for (String block : ORE_BLOCKS) {
            ResourceLocation id = ACRef.id(block);
            require(BuiltInRegistries.BLOCK.containsKey(id), "worldgen references unregistered block " + id);
        }
        validateLoaderPlacement("forge");
        validateLoaderPlacement("neoforge");
        validateStructure("dark_ritual_grounds");
        validateStructure("shoggoth_pit_river");
        System.out.printf("RR_WORLD_RESOURCE_AUDIT_OK features=%d blocks=%d loaders=2 structures=2%n",
            FEATURE_IDS.size(), ORE_BLOCKS.size());
    }

    private static void validateLoaderPlacement(String loader) {
        StringBuilder placement = new StringBuilder();
        for (String modifier : MODIFIERS) {
            placement.append(readResource(loader + "/biome_modifier/" + modifier + ".json"));
        }
        for (String id : FEATURE_IDS) {
            require(placement.indexOf(ACRef.id(id).toString()) >= 0,
                loader + " placement is missing " + ACRef.id(id));
        }
    }

    private static void validateStructure(String id) {
        readJson("worldgen/structure/" + id + ".json");
        readJson("worldgen/structure_set/" + id + ".json");
        readJson("tags/worldgen/biome/has_structure/" + id + ".json");
    }

    private static JsonObject readJson(String path) {
        try {
            return JsonParser.parseString(readResource(path)).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("invalid worldgen JSON data/abyssalcraft/" + path, exception);
        }
    }

    private static String readResource(String path) {
        String fullPath = "/data/abyssalcraft/" + path;
        try (InputStream stream = WorldgenResourceAudit.class.getResourceAsStream(fullPath)) {
            require(stream != null, "missing worldgen resource " + fullPath);
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                StringBuilder content = new StringBuilder();
                char[] buffer = new char[2048];
                for (int read; (read = reader.read(buffer)) >= 0;) content.append(buffer, 0, read);
                return content.toString();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("unable to read worldgen resource " + fullPath, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}