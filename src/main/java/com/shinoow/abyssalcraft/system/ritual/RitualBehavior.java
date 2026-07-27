package com.shinoow.abyssalcraft.system.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Server-authoritative precondition and completion behavior for one specialized ritual. */
public interface RitualBehavior {

    default boolean canStart(ManifestRitual ritual, Level level, BlockPos altar,
                             Player player, RitualHost host) {
        return true;
    }

    void complete(ManifestRitual ritual, Level level, BlockPos altar, Player player, RitualHost host);
}