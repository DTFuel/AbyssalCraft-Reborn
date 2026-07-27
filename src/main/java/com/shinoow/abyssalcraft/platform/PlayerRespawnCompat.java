package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Teleport a player to the authoritative respawn dimension/position. */
public final class PlayerRespawnCompat {

    private PlayerRespawnCompat() {}

    public static boolean teleportHome(ServerPlayer player) {
        BlockPos respawn = player.getRespawnPosition();
        if (respawn == null) return false;
        ServerLevel destination = player.getServer().getLevel(player.getRespawnDimension());
        if (destination == null) return false;
        Entity moved = TeleportCompat.teleport(player, destination,
            respawn.getX() + 0.5D, respawn.getY(), respawn.getZ() + 0.5D);
        return moved != null;
    }
}