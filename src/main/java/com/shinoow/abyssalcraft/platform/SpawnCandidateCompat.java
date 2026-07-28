package com.shinoow.abyssalcraft.platform;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.config.ContentConfigMatrix;
import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

//? if forge {
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.ForgeEventFactory;
//?} else {
/*import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.EventHooks;
*///?}

public final class SpawnCandidateCompat {

    public record Candidate(String entityId, int weight, int minCount, int maxCount) {}

    public record PotentialSpawnsObservation(ResourceKey<Level> dimension, ResourceKey<Biome> biome,
                                             int y, List<Candidate> candidates,
                                             boolean candidatesPresentInEventList) {}

    public record ConfiguredSpawnContext(boolean evilAnimalBiome, boolean netherBiome,
                                         boolean dictionaryAquaticBiome, boolean vanillaAquaticBiome,
                                         boolean forestBiome, boolean darkForestBiome) {}

    private static final TagKey<Biome> EVIL_ANIMAL_BIOMES = biomeTag("evil_animal_spawns");
    private static final TagKey<Biome> DICTIONARY_AQUATIC_BIOMES = biomeTag("dictionary_aquatic_spawns");
    private static final TagKey<Biome> VANILLA_AQUATIC_BIOMES = biomeTag("vanilla_aquatic_spawns");
    private static final TagKey<Biome> FOREST_BIOMES = biomeTag("dark_offspring_spawns");
    private static final TagKey<Biome> DARK_FOREST_BIOMES = biomeTag("dark_offspring_double_spawns");

    private static final List<String> EVIL_ANIMALS = List.of(
        "evil_pig", "evil_cow", "evil_chicken", "evil_sheep");
    private static final List<String> DEMON_ANIMALS = List.of(
        "demon_pig", "demon_cow", "demon_chicken", "demon_sheep");

    private static final List<Candidate> SHADOW_REALM = List.of(
        candidate("shadowcreature", 60, 1, 5),
        candidate("shadowmonster", 40, 1, 3),
        candidate("shadowbeast", 10, 1, 1),
        candidate("shadow_ghoul", 1, 1, 1));

    private static final List<Candidate> DREADLANDS_OVERRIDE = List.of(
        candidate("dreadspawn", 30, 1, 2),
        candidate("dreadling", 40, 1, 2),
        candidate("chagarothfist", 2, 1, 1),
        candidate("demon_pig", 5, 1, 2),
        candidate("demon_cow", 5, 1, 2),
        candidate("demon_chicken", 5, 1, 2),
        candidate("demon_sheep", 5, 1, 2),
        candidate("greaterdreadspawn", 5, 1, 1),
        candidate("dreadguard", 8, 1, 1),
        candidate("lesserdreadbeast", 1, 1, 1),
        candidate("dreaded_ghoul", 1, 1, 3),
        candidate("shadowcreature", 70, 3, 3),
        candidate("shadowmonster", 50, 2, 2),
        candidate("shadowbeast", 20, 1, 1));

    private static List<SpawnerData> shadowRealmSpawners;
    private static List<SpawnerData> dreadlandsOverrideSpawners;
    private static final Object OBSERVATION_LOCK = new Object();
    private static final List<PotentialSpawnsObservation> OBSERVATIONS = new ArrayList<>();

    private SpawnCandidateCompat() {}

    public static void attach() {
        EventBuses.game().addListener((LevelEvent.PotentialSpawns event) -> onPotentialSpawns(event));
    }

    private static void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER || !(event.getLevel() instanceof ServerLevel level)) return;

        boolean abyssalWasteland = level.dimension() == ACDimensions.ABYSSAL_WASTELAND;
        boolean dreadlands = level.dimension() == ACDimensions.DREADLANDS;
        if (abyssalWasteland && ACConfig.no_spectral_dragons.get()) {
            EntityType<?> spectralDragon = BossEntities.DRAGON_MINION.get();
            for (SpawnerData spawner : List.copyOf(event.getSpawnerDataList())) {
                if (spawner.type == spectralDragon) event.removeSpawnerData(spawner);
            }
        }
        Holder<Biome> biome = level.getBiome(event.getPos());
        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);
        List<Candidate> snapshot = candidateSnapshot(level.dimension(), biomeKey, event.getPos().getY());
        List<SpawnerData> addedSnapshotSpawners = List.of();
        if (abyssalWasteland || dreadlands) {
            if (!snapshot.isEmpty()) {
                if (ContentConfigMatrix.purgeMobSpawns()) {
                    for (SpawnerData spawner : List.copyOf(event.getSpawnerDataList())) {
                        event.removeSpawnerData(spawner);
                    }
                }
                addedSnapshotSpawners = spawners(snapshot);
                for (SpawnerData spawner : addedSnapshotSpawners) event.addSpawnerData(spawner);
            }
        }

        ConfiguredSpawnContext context = new ConfiguredSpawnContext(
            biome.is(EVIL_ANIMAL_BIOMES), biome.is(BiomeTags.IS_NETHER),
            biome.is(DICTIONARY_AQUATIC_BIOMES), biome.is(VANILLA_AQUATIC_BIOMES),
            biome.is(FOREST_BIOMES), biome.is(DARK_FOREST_BIOMES));
        for (Candidate candidate : configuredCandidateSnapshot(context,
                ACConfig.evilAnimalSpawnWeight.get(), ACConfig.demonAnimalSpawnWeight.get(),
                ACConfig.depthsGhoulBiomeDictSpawn.get(), ACConfig.abyssalZombieBiomeDictSpawn.get(),
                ACConfig.darkOffspringSpawnWeight.get())) {
            event.addSpawnerData(spawner(candidate));
        }
        observePotentialSpawns(level.dimension(), biomeKey, event.getPos().getY(), snapshot,
            event.getSpawnerDataList().containsAll(addedSnapshotSpawners));
    }

    public static void resetPotentialSpawnsObservations() {
        synchronized (OBSERVATION_LOCK) {
            OBSERVATIONS.clear();
        }
    }

    public static List<PotentialSpawnsObservation> potentialSpawnsObservations() {
        synchronized (OBSERVATION_LOCK) {
            return List.copyOf(OBSERVATIONS);
        }
    }

    /** Query the loader's real PotentialSpawns hook against the biome's current monster list. */
    public static List<SpawnerData> queryPotentialSpawns(ServerLevel level, BlockPos pos) {
        WeightedRandomList<SpawnerData> base = level.getBiome(pos).value().getMobSettings()
            .getMobs(MobCategory.MONSTER);
        //? if forge {
        return ForgeEventFactory.getPotentialSpawns(level, MobCategory.MONSTER, pos, base).unwrap();
        //?} else {
        /*return EventHooks.getPotentialSpawns(level, MobCategory.MONSTER, pos, base).unwrap();
        *///?}
    }

    private static void observePotentialSpawns(ResourceKey<Level> dimension, ResourceKey<Biome> biome,
            int y, List<Candidate> candidates, boolean candidatesPresentInEventList) {
        if (!Boolean.getBoolean("abyssalcraft.rrServerMatrix")) return;
        PotentialSpawnsObservation observation = new PotentialSpawnsObservation(
            dimension, biome, y, List.copyOf(candidates), candidatesPresentInEventList);
        synchronized (OBSERVATION_LOCK) {
            OBSERVATIONS.add(observation);
        }
    }

    public static List<Candidate> configuredCandidateSnapshot(ConfiguredSpawnContext context,
            int evilAnimalWeight, int demonAnimalWeight, boolean depthsGhoulDictionary,
            boolean abyssalZombieDictionary, int darkOffspringWeight) {
        List<Candidate> candidates = new ArrayList<>();
        if (context.evilAnimalBiome() && evilAnimalWeight > 0) {
            EVIL_ANIMALS.forEach(id -> candidates.add(candidate(id, evilAnimalWeight, 1, 3)));
        }
        if (context.netherBiome() && demonAnimalWeight > 0) {
            DEMON_ANIMALS.forEach(id -> candidates.add(candidate(id, demonAnimalWeight, 1, 3)));
        }
        boolean depthsGhoulBiome = depthsGhoulDictionary
            ? context.dictionaryAquaticBiome() : context.vanillaAquaticBiome();
        if (depthsGhoulBiome) candidates.add(candidate("depths_ghoul", 1, 1, 3));
        boolean abyssalZombieBiome = abyssalZombieDictionary
            ? context.dictionaryAquaticBiome() : context.vanillaAquaticBiome();
        if (abyssalZombieBiome) candidates.add(candidate("abyssalzombie", 10, 1, 3));
        if ((context.forestBiome() || context.darkForestBiome()) && darkOffspringWeight > 0) {
            int weight = context.darkForestBiome() ? darkOffspringWeight * 2 : darkOffspringWeight;
            candidates.add(candidate("shuboffspring", weight, 1, 3));
        }
        return List.copyOf(candidates);
    }

    public static List<Candidate> candidateSnapshot(ResourceKey<Level> dimension,
                                                    ResourceKey<Biome> biome, int y) {
        if (dimension != ACDimensions.ABYSSAL_WASTELAND && dimension != ACDimensions.DREADLANDS) {
            return List.of();
        }
        if (y <= 5) return SHADOW_REALM;
        if (dimension == ACDimensions.DREADLANDS && biome == DarklandsBiomes.DARKLANDS) {
            return DREADLANDS_OVERRIDE;
        }
        return List.of();
    }

    private static Candidate candidate(String entityId, int weight, int min, int max) {
        return new Candidate(entityId, weight, min, max);
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.BIOME, ACRef.id(path));
    }

    private static SpawnerData spawner(Candidate candidate) {
        var id = ACRef.id(candidate.entityId());
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            throw new IllegalStateException("spawn candidate references missing entity: " + candidate.entityId());
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        return new SpawnerData(type, candidate.weight(), candidate.minCount(), candidate.maxCount());
    }

    private static List<SpawnerData> spawners(List<Candidate> candidates) {
        if (candidates == SHADOW_REALM) {
            if (shadowRealmSpawners == null) shadowRealmSpawners = createSpawners(candidates);
            return shadowRealmSpawners;
        }
        if (candidates == DREADLANDS_OVERRIDE) {
            if (dreadlandsOverrideSpawners == null) dreadlandsOverrideSpawners = createSpawners(candidates);
            return dreadlandsOverrideSpawners;
        }
        throw new IllegalArgumentException("unknown spawn candidate snapshot");
    }

    private static List<SpawnerData> createSpawners(List<Candidate> candidates) {
        return candidates.stream().map(SpawnCandidateCompat::spawner).toList();
    }
}
