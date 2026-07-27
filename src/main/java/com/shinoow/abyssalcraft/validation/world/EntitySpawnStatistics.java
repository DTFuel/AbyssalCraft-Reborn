package com.shinoow.abyssalcraft.validation.world;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.SpawnCandidateCompat;

/** Runtime registry audit for the natural-spawn ecology gate (T5.8d). */
public final class EntitySpawnStatistics {

    private static final List<String> TARGET_BIOMES = List.of(
        "abyssal_wastelands", "abyssal_swamp", "abyssal_desert", "abyssal_plateau", "coralium_lake",
        "dreadlands", "dreadlands_forest", "dreadlands_mountains", "dreadlands_ocean", "darklands"
    );

    private EntitySpawnStatistics() {}

    public static String sampleSpawnData(ServerLevel level) {
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        int registrySpawners = 0;

        for (String path : TARGET_BIOMES) {
            ResourceLocation biomeId = ACRef.id(path);
            Biome biome = biomes.get(biomeId);
            if (biome == null) return "RR_WORLD_SPAWN_STATS_FAIL missingBiome=" + biomeId;
            for (SpawnerData spawner : biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap()) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(spawner.type);
                if (entityId != null && entityId.getNamespace().equals("abyssalcraft")) registrySpawners++;
            }
        }

        List<SpawnCandidateCompat.Candidate> candidates =
            SpawnCandidateCompat.candidateSnapshot(level.dimension(), null, 4);
        for (SpawnCandidateCompat.Candidate candidate : candidates) {
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ACRef.id(candidate.entityId()))) {
                return "RR_WORLD_SPAWN_STATS_FAIL missingCandidateEntity=" + candidate.entityId();
            }
        }

        int total = registrySpawners + candidates.size();
        if (total == 0) {
            return "RR_WORLD_SPAWN_STATS_FAIL noAcSpawnSources=true dimension="
                + level.dimension().location();
        }
        return String.format(
            "RR_WORLD_SPAWN_STATS_OK biomesChecked=%d registrySpawners=%d candidateSpawners=%d",
            TARGET_BIOMES.size(), registrySpawners, candidates.size());
    }
}