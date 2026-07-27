package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.content.entity.legacy.LegacyEntities;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

/** Weighted spawn-list disruption used by the legacy random spawn and random swarm entries. */
public final class RandomMobDisruption extends Disruption {

    private final int count;

    public RandomMobDisruption(String name, int count) {
        super(name, null);
        this.count = count;
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) return;
        var monsters = server.getBiome(pos).value().getMobSettings().getMobs(MobCategory.MONSTER);
        for (int index = 0; index < count; index++) {
            SpawnerData selected = null;
            for (int attempt = 0; attempt < 10 && selected == null; attempt++) {
                SpawnerData candidate = monsters.getRandom(server.random).orElse(null);
                if (candidate != null && candidate.type != LegacyEntities.LESSER_DREADBEAST.get()) selected = candidate;
            }
            if (selected == null) continue;
            EntityType<?> type = selected.type;
            MobSpawnCompat.spawnNear(server, pos, type);
        }
    }

    public int count() {
        return count;
    }
}