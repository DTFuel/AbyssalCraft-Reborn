package com.shinoow.abyssalcraft.net.client;

import java.util.UUID;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: link the evil-sheep entity {@code id} to its owner ({@code playerUUID} /
 * {@code playerName}) on the client (owned by PS-1). Serialization is faithful; the client-side link is
 * deferred until the evil-sheep entity is ported.
 */
public class EvilSheepMessage implements NetworkChannel.ACPacket {

    private final UUID playerUUID;
    private final String playerName;
    private final int id;

    public EvilSheepMessage(UUID playerUUID, String playerName, int id) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.id = id;
    }

    public EvilSheepMessage(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.playerName = buf.readUtf();
        this.id = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeUtf(playerName);
        buf.writeVarInt(id);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the evil-sheep entity + its client-side owner link are not yet ported (entity stage).
    }
}
