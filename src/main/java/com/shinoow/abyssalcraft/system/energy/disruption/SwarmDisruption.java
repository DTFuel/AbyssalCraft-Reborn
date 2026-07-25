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

/** Spawn four members of every configured mob type, matching the legacy swarm contract. */
public final class SwarmDisruption extends Disruption {

    private static final int PER_TYPE = 4;

    private final List<Supplier<? extends EntityType<? extends Mob>>> entityTypes;

    @SafeVarargs
    public SwarmDisruption(String name, DeityType deity,
                             Supplier<? extends EntityType<? extends Mob>>... entityTypes) {
        super(name, deity);
        this.entityTypes = List.of(entityTypes);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (Supplier<? extends EntityType<? extends Mob>> entityType : entityTypes) {
            for (int index = 0; index < PER_TYPE; index++) {
                MobSpawnCompat.spawnNear(server, pos, entityType.get());
            }
        }
    }

    public int totalCount() {
        return entityTypes.size() * PER_TYPE;
    }
}