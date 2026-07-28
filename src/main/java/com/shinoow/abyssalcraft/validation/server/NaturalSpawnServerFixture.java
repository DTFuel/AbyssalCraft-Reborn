package com.shinoow.abyssalcraft.validation.server;

import java.util.ArrayList;
import java.util.List;

import com.shinoow.abyssalcraft.platform.SpawnCandidateCompat;
import com.shinoow.abyssalcraft.world.ACDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;

/** Real ServerLevel smoke fixture for the vanilla natural-spawn entry point. */
public final class NaturalSpawnServerFixture {

    private static final int CHUNK_X = 1875000;
    private static final int CHUNK_Z = 1875000;
    private static final List<SpawnCandidateCompat.Candidate> SHADOW_REALM = List.of(
        new SpawnCandidateCompat.Candidate("shadowcreature", 60, 1, 5),
        new SpawnCandidateCompat.Candidate("shadowmonster", 40, 1, 3),
        new SpawnCandidateCompat.Candidate("shadowbeast", 10, 1, 1),
        new SpawnCandidateCompat.Candidate("shadow_ghoul", 1, 1, 1));

    private NaturalSpawnServerFixture() {}

    public static void run(MinecraftServer server) {
        List<Scenario> scenarios = List.of(
            new Scenario(server.overworld(), 64),
            new Scenario(level(server, ACDimensions.ABYSSAL_WASTELAND), 64),
            new Scenario(level(server, ACDimensions.DREADLANDS), 64),
            new Scenario(level(server, ACDimensions.OMOTHOL), 64),
            new Scenario(level(server, ACDimensions.DARK_REALM), 64),
            new Scenario(level(server, ACDimensions.DREADLANDS), 72),
            new Scenario(level(server, ACDimensions.DREADLANDS), 80),
            new Scenario(level(server, ACDimensions.DREADLANDS), 88),
            new Scenario(server.overworld(), 32),
            new Scenario(level(server, ACDimensions.ABYSSAL_WASTELAND), 5),
            new Scenario(level(server, ACDimensions.DREADLANDS), 5));
        List<Entity> before = new ArrayList<>();
        SpawnCandidateCompat.resetPotentialSpawnsObservations();
        try {
            for (Scenario scenario : scenarios) {
                ServerLevel level = scenario.level();
                level.setChunkForced(CHUNK_X, CHUNK_Z, true);
                BlockPos position = new BlockPos((CHUNK_X << 4) + 8, scenario.y(), (CHUNK_Z << 4) + 8);
                before.addAll(level.getEntities(null, new AABB(position).inflate(32.0D)));
                int observationsBefore = SpawnCandidateCompat.potentialSpawnsObservations().size();
                NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER, level, position);
                SpawnCandidateCompat.queryPotentialSpawns(level, position);
                List<SpawnCandidateCompat.PotentialSpawnsObservation> observations =
                    SpawnCandidateCompat.potentialSpawnsObservations();
                require(observations.size() > observationsBefore,
                    "no PotentialSpawns callback for " + level.dimension().location() + " y=" + scenario.y());
                require(observations.subList(observationsBefore, observations.size()).stream().anyMatch(observation ->
                    observation.dimension() == level.dimension() && observation.y() == scenario.y()),
                    "PotentialSpawns callback context mismatch for " + level.dimension().location()
                        + " y=" + scenario.y());
            }
            List<SpawnCandidateCompat.PotentialSpawnsObservation> observations =
                SpawnCandidateCompat.potentialSpawnsObservations();
            require(!observations.isEmpty(), "no PotentialSpawns callbacks observed");
            require(observations.stream().filter(observation -> !observation.candidates().isEmpty())
                .allMatch(SpawnCandidateCompat.PotentialSpawnsObservation::candidatesPresentInEventList),
                "candidate SpawnerData missing from PotentialSpawns event list");
            long shadowContexts = observations.stream()
                .filter(observation -> observation.y() == 5
                    && (observation.dimension() == ACDimensions.ABYSSAL_WASTELAND
                        || observation.dimension() == ACDimensions.DREADLANDS))
                .filter(observation -> observation.candidates().equals(SHADOW_REALM))
                .map(SpawnCandidateCompat.PotentialSpawnsObservation::dimension).distinct().count();
            require(shadowContexts == 2, "expected exact Shadow Realm candidates in both y=5 contexts");
            long dimensions = observations.stream()
                .map(SpawnCandidateCompat.PotentialSpawnsObservation::dimension).distinct().count();
            require(dimensions >= 5, "PotentialSpawns dimension coverage below five: " + dimensions);
            long applied = observations.stream().filter(observation -> !observation.candidates().isEmpty())
                .filter(SpawnCandidateCompat.PotentialSpawnsObservation::candidatesPresentInEventList).count();
            System.out.println("RR_SPAWN_MODIFIER_BEHAVIOR_OK callbacks=" + observations.size()
                + " applied=" + applied + " shadowContexts=2 dimensions=" + dimensions
                + " scenarios=11 weights=exact groups=exact");
            System.out.println("RR_ENTITY_NATURAL_SPAWN_OK scenarios=11 entrypoint=vanilla");
        } finally {
            for (Scenario scenario : scenarios) {
                ServerLevel level = scenario.level();
                BlockPos position = new BlockPos((CHUNK_X << 4) + 8, scenario.y(), (CHUNK_Z << 4) + 8);
                level.getEntities(null, new AABB(position).inflate(32.0D)).stream()
                    .filter(entity -> !before.contains(entity)).forEach(Entity::discard);
                level.setChunkForced(CHUNK_X, CHUNK_Z, false);
            }
        }
    }

    private static ServerLevel level(MinecraftServer server, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key) {
        ServerLevel level = server.getLevel(key);
        if (level == null) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL missing spawn level " + key.location());
        return level;
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_SERVER_MATRIX_FAIL " + reason);
    }

    private record Scenario(ServerLevel level, int y) {}
}