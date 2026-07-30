package com.shinoow.abyssalcraft.world;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    private static final List<String> ABYSSAL_WASTELAND_BIOMES = List.of(
        "abyssal_wastelands", "abyssal_swamp", "abyssal_desert", "abyssal_plateau", "coralium_lake");
    private static final List<String> ABYSSAL_WASTELAND_MONSTERS = List.of(
        "minecraft:zombie", "minecraft:skeleton", "abyssalcraft:depths_ghoul",
        "abyssalcraft:abyssalzombie", "abyssalcraft:gskeleton", "abyssalcraft:dragonminion",
        "abyssalcraft:lesser_shoggoth", "abyssalcraft:shoggoth", "abyssalcraft:greater_shoggoth");

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
        validateAbyssalWastelandSpawns();
        validateTerrainDensityGuards();
        validateStructure("dark_ritual_grounds");
        validateStructure("shoggoth_pit");
        validateStructure("shoggoth_pit_river");
        validateShoggothPlacement("shoggoth_pit");
        validateShoggothPlacement("shoggoth_pit_river");
        System.out.printf("RR_WORLD_RESOURCE_AUDIT_OK features=%d blocks=%d loaders=2 structures=3 biomeSpawns=5x10 terrainBounds=2%n",
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

    private static void validateShoggothPlacement(String id) {
        JsonObject placement = readJson("worldgen/structure_set/" + id + ".json")
            .getAsJsonObject("placement");
        JsonArray targets = readJson("tags/worldgen/structure/" + id + ".json")
            .getAsJsonArray("values");
        require(placement.get("spacing").getAsInt() == 1
            && placement.get("separation").getAsInt() == 0,
            id + " must expose every chunk to the bounded config placement grid");
        require(targets.size() == 1 && ACRef.id(id).toString().equals(targets.get(0).getAsString()),
            id + " locator tag does not resolve exactly one structure");
        require(WorldgenConfigGate.lairChunkInterval(35, 100) == 7
            && WorldgenConfigGate.passesLairPlacement(35, 100, 14, -7)
            && !WorldgenConfigGate.passesLairPlacement(35, 100, 13, -7)
            && !WorldgenConfigGate.passesLairPlacement(0, 100, 14, -7),
            "Shoggoth Lair default placement grid changed");
    }

    private static void validateAbyssalWastelandSpawns() {
        for (String biome : ABYSSAL_WASTELAND_BIOMES) {
            JsonObject spawners = readJson("worldgen/biome/" + biome + ".json").getAsJsonObject("spawners");
            JsonArray monsters = spawners.getAsJsonArray("monster");
            JsonArray waterCreatures = spawners.getAsJsonArray("water_creature");
            require(monsters.size() == ABYSSAL_WASTELAND_MONSTERS.size(),
                biome + " monster spawner count changed: " + monsters.size());
            for (String entity : ABYSSAL_WASTELAND_MONSTERS) {
                require(hasSpawner(monsters, entity), biome + " is missing monster spawner " + entity);
            }
            require(waterCreatures.size() == 1 && hasSpawner(waterCreatures, "abyssalcraft:coraliumsquid"),
                biome + " must contain exactly one Coralium Squid spawner");
        }
        for (String loader : List.of("forge", "neoforge")) {
            String shoggothModifier = readResource(loader + "/biome_modifier/spawn_shoggoth.json");
            for (String biome : ABYSSAL_WASTELAND_BIOMES) {
                require(!shoggothModifier.contains(ACRef.id(biome).toString()),
                    loader + " Shoggoth modifier duplicates biome-owned spawns for " + biome);
            }
            requireMissingResource(loader + "/biome_modifier/spawn_abyssal_wasteland.json");
            requireMissingResource(loader + "/biome_modifier/spawn_coralium_squid.json");
            requireMissingResource(loader + "/biome_modifier/feature_chains.json");
        }
        requireMissingResource("worldgen/configured_feature/chains.json");
        requireMissingResource("worldgen/placed_feature/chains.json");
        JsonObject pillars = readJson("worldgen/configured_feature/abyssal_wasteland_pillars.json");
        require("abyssalcraft:chains".equals(pillars.get("type").getAsString()),
            "Abyssal Wasteland pillar toggle must own the sole Chains feature");
    }

    private static boolean hasSpawner(JsonArray spawners, String entity) {
        for (JsonElement element : spawners) {
            if (entity.equals(element.getAsJsonObject().get("type").getAsString())) return true;
        }
        return false;
    }

    private static void validateTerrainDensityGuards() {
        JsonObject wasteland = readJson("worldgen/noise_settings/abyssal_wasteland.json")
            .getAsJsonObject("noise_router").getAsJsonObject("final_density");
        require("minecraft:min".equals(wasteland.get("type").getAsString()),
            "Abyssal Wasteland final density must have a top cap");
        requireGradient(wasteland.getAsJsonObject("argument2"), 128, 192, 1.0D, -4.0D,
            "Abyssal Wasteland top cap");

        JsonObject dreadlands = readJson("worldgen/noise_settings/dreadlands.json")
            .getAsJsonObject("noise_router").getAsJsonObject("final_density");
        require("minecraft:max".equals(dreadlands.get("type").getAsString()),
            "Dreadlands final density must have a solid floor");
        JsonObject cappedTerrain = dreadlands.getAsJsonObject("argument1");
        require("minecraft:min".equals(cappedTerrain.get("type").getAsString()),
            "Dreadlands terrain must have a top cap");
        requireGradient(cappedTerrain.getAsJsonObject("argument2"), 160, 224, 1.0D, -4.0D,
            "Dreadlands top cap");
        requireGradient(dreadlands.getAsJsonObject("argument2"), 55, 64, 1.0D, -64.0D,
            "Dreadlands solid floor");
    }

    private static void requireGradient(JsonObject gradient, int fromY, int toY,
                                        double fromValue, double toValue, String owner) {
        require("minecraft:y_clamped_gradient".equals(gradient.get("type").getAsString())
            && gradient.get("from_y").getAsInt() == fromY
            && gradient.get("to_y").getAsInt() == toY
            && gradient.get("from_value").getAsDouble() == fromValue
            && gradient.get("to_value").getAsDouble() == toValue,
            owner + " changed");
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

    private static void requireMissingResource(String path) {
        String fullPath = "/data/abyssalcraft/" + path;
        try (InputStream stream = WorldgenResourceAudit.class.getResourceAsStream(fullPath)) {
            require(stream == null, "obsolete worldgen resource still present " + fullPath);
        } catch (IOException exception) {
            throw new IllegalStateException("unable to inspect worldgen resource " + fullPath, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}