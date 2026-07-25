package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A lightning disruption (pilot, owned by PS-9): strikes lightning at the manipulator. Faithful to the
 * 1.12.2 {@code LIGHTNING} disruption, a representative of the "spawn an entity" disruption family
 * ({@code DisruptionSpawn}/{@code DisruptionSwarm} for unported mobs). Fork-free
 * ({@code EntityType.LIGHTNING_BOLT.create(Level)} + {@code addFreshEntity}).
 */
public final class LightningDisruption extends Disruption {

    public LightningDisruption(String name, DeityType deity) {
        super(name, deity);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            level.addFreshEntity(bolt);
        }
    }
}
