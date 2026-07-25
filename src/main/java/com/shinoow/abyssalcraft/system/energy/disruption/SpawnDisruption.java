package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;
import java.util.function.Supplier;

import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Spawn a fixed number of one mob type around the disrupted manipulator. */
public final class SpawnDisruption extends Disruption {

    private final Supplier<? extends EntityType<? extends Mob>> entityType;
    private final int count;

    public SpawnDisruption(String name, DeityType deity,
                             Supplier<? extends EntityType<? extends Mob>> entityType, int count) {
        super(name, deity);
        this.entityType = entityType;
        this.count = count;
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (int index = 0; index < count; index++) {
            MobSpawnCompat.spawnNear(server, pos, entityType.get());
        }
    }

    public int count() {
        return count;
    }
}