package com.shinoow.abyssalcraft.platform;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.content.entity.boss.BossEntities;
import com.shinoow.abyssalcraft.content.entity.demon.DemonEntities;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.world.ACDimensions;
import com.shinoow.abyssalcraft.world.darklands.DarklandsBiomes;

//? if forge {
import net.minecraftforge.event.level.LevelEvent;
//?} else {
/*import net.neoforged.neoforge.event.level.LevelEvent;
*///?}

public final class SpawnCandidateCompat {

    public record Candidate(String entityId, int weight, int minCount, int maxCount) {}

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

    private SpawnCandidateCompat() {}

    public static void attach() {
        EventBuses.game().addListener((LevelEvent.PotentialSpawns event) -> onPotentialSpawns(event));
    }

    private static void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER || !(event.getLevel() instanceof ServerLevel level)) return;

        boolean abyssalWasteland = level.dimension() == ACDimensions.ABYSSAL_WASTELAND;
        boolean dreadlands = level.dimension() == ACDimensions.DREADLANDS;
        if (!abyssalWasteland && !dreadlands) return;

        ResourceKey<Biome> biome = level.getBiome(event.getPos()).unwrapKey().orElse(null);
        List<Candidate> snapshot = candidateSnapshot(level.dimension(), biome, event.getPos().getY());
        if (snapshot.isEmpty()) return;
        List<SpawnerData> spawners = event.getSpawnerDataList();
        spawners.clear();
        for (Candidate candidate : snapshot) add(spawners, candidate);
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

    private static void add(List<SpawnerData> list, Candidate candidate) {
        var id = ACRef.id(candidate.entityId());
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            throw new IllegalStateException("spawn candidate references missing entity: " + candidate.entityId());
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        list.add(new SpawnerData(type, candidate.weight(), candidate.minCount(), candidate.maxCount()));
    }
}
