package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.content.entity.ghoul.DepthsGhoul;
import com.shinoow.abyssalcraft.content.entity.ghoul.GhoulEntities;
import com.shinoow.abyssalcraft.platform.MobSpawnCompat;
import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Replace every legacy sacrifice target in range with a Depths Ghoul. */
public final class SacrificeCorruptionDisruption extends Disruption {

    public SacrificeCorruptionDisruption(String name, DeityType deity) {
        super(name, deity);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (Mob target : server.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(64.0),
            CorruptionRegistry::isSacrifice)) {
            DepthsGhoul ghoul = GhoulEntities.DEPTHS_GHOUL.get().create(server);
            if (ghoul == null) {
                continue;
            }
            ghoul.copyPosition(target);
            if (target.hasCustomName()) {
                ghoul.setCustomName(target.getCustomName());
            }
            ghoul.setPersistenceRequired();
            MobSpawnCompat.finalizeSpawnerSpawn(server, ghoul);
            if (server.addFreshEntity(ghoul)) {
                target.discard();
            }
        }
    }
}