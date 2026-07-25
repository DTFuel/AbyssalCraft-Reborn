package com.shinoow.abyssalcraft.net.server;

import java.util.UUID;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.SyncNecromancyDataMessage;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client &rarr; server: request the server (re)send the requesting player's necromancy data (owned by
 * PS-1). The server answers by pushing a {@link SyncNecromancyDataMessage} with the player's authoritative
 * necrodata (PS-2). The sender is taken from the connection ({@code ctx.player()}); the carried UUID is
 * kept for faithful serialization.
 */
public class PrepareSyncMessage implements NetworkChannel.ACPacket {

    private final UUID playerUUID;

    public PrepareSyncMessage(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public PrepareSyncMessage(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Server: push the sender's authoritative necrodata back to them.
        if (ctx.player() instanceof ServerPlayer sender) {
            ACNetwork.sendToPlayer(sender, new SyncNecromancyDataMessage(NecroDataCapability.save(sender)));
        }
    }
}
