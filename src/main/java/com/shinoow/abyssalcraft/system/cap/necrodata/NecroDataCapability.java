package com.shinoow.abyssalcraft.system.cap.necrodata;

import com.shinoow.abyssalcraft.platform.PlayerDataCompat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Entry point to the per-player necrodata capability (owned by PS-2), faithful to the 1.12.2
 * {@code NecroDataCapability.getCap(player)} accessor. The data lives in the player's neutral tag
 * (attached by {@code platform/PlayerDataCompat}); this returns a {@link NecroData} view over it.
 *
 * <p>{@link #save(Player)} / {@link #apply(Player, CompoundTag)} are the serialize / client-apply hooks
 * the network layer (PS-1 necrodata messages) and the knowledge subsystem (PS-8) use to sync the
 * capability server&rarr;client.
 */
public final class NecroDataCapability {

    private NecroDataCapability() {}

    /** The necrodata for {@code player} (server or client side); mutations persist automatically. */
    public static NecroData get(Player player) {
        return new NecroData(PlayerDataCompat.getTag(player));
    }

    /** A copy of the player's raw necrodata tag, for sending to the client. */
    public static CompoundTag save(Player player) {
        return PlayerDataCompat.getTag(player).copy();
    }

    /** Overwrite the player's necrodata tag from a synced copy (client side). */
    public static void apply(Player player, CompoundTag tag) {
        PlayerDataCompat.setTag(player, tag);
    }
}
