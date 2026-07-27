package com.shinoow.abyssalcraft.validation.server;

import java.util.ArrayList;
import java.util.List;

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
        try {
            for (Scenario scenario : scenarios) {
                ServerLevel level = scenario.level();
                level.setChunkForced(CHUNK_X, CHUNK_Z, true);
                BlockPos position = new BlockPos((CHUNK_X << 4) + 8, scenario.y(), (CHUNK_Z << 4) + 8);
                before.addAll(level.getEntities(null, new AABB(position).inflate(32.0D)));
                NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER, level, position);
            }
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

    private record Scenario(ServerLevel level, int y) {}
}