package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Rain large fireballs from the eight legacy offsets around the disrupted manipulator. */
public final class FireRainDisruption extends Disruption {

    private static final double[][] OFFSETS = {
        {2.5, 2.5}, {-2.5, 2.5}, {2.5, -2.5}, {-2.5, -2.5},
        {2.5, 0.0}, {-2.5, 0.0}, {0.0, 2.5}, {0.0, -2.5}
    };

    public FireRainDisruption() {
        super("fireRain", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (double[] offset : OFFSETS) {
            if (!server.random.nextBoolean()) {
                continue;
            }
            LargeFireball fireball = EntityType.FIREBALL.create(server);
            if (fireball != null) {
                fireball.setPos(pos.getX() + offset[0], pos.getY() + 10.0, pos.getZ() + offset[1]);
                fireball.setDeltaMovement(new Vec3(0.0, -0.1, 0.0));
                server.addFreshEntity(fireball);
            }
        }
    }

    public static int candidateCount() {
        return OFFSETS.length;
    }
}