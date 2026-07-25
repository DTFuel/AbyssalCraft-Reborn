package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.platform.IgniteCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ignite every nearby player for twenty seconds. */
public final class FireDisruption extends Disruption {

    public FireDisruption() {
        super("fire", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!level.isClientSide) {
            players.forEach(player -> IgniteCompat.ignite(player, 20));
        }
    }
}