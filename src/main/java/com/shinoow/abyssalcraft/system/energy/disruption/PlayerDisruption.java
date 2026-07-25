package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;
import java.util.function.BiConsumer;

import com.shinoow.abyssalcraft.system.energy.DeityType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A player-targeted disruption (owned by PS-9): runs an action against every player caught in the pull.
 * Faithful to the 1.12.2 player-list disruptions ({@code DisruptionFire}/{@code DisruptionFreeze}/
 * {@code DisruptionFamine}/{@code DisruptionTeleportRandomly}), which each iterate the affected players.
 * The action is a fork-free {@link BiConsumer} so each concrete disruption is a one-liner in
 * {@link Disruptions}.
 */
public final class PlayerDisruption extends Disruption {

    private final BiConsumer<Player, Level> action;

    public PlayerDisruption(String name, DeityType deity, BiConsumer<Player, Level> action) {
        super(name, deity);
        this.action = action;
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (level.isClientSide) {
            return;
        }
        for (Player player : players) {
            action.accept(player, level);
        }
    }
}
