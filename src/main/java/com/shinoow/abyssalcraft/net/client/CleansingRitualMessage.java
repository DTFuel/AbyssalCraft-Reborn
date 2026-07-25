package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: notify a cleansing-ritual biome change to {@code biomeID} at column
 * {@code (x, z)}, optionally {@code batched} with neighbours (owned by PS-1). Serialization is faithful;
 * the client-side biome refresh is deferred until the cleansing ritual is ported.
 */
public class CleansingRitualMessage implements NetworkChannel.ACPacket {

    private final int x;
    private final int z;
    private final int biomeID;
    private final boolean batched;

    public CleansingRitualMessage(int x, int z, int biomeID, boolean batched) {
        this.x = x;
        this.z = z;
        this.biomeID = biomeID;
        this.batched = batched;
    }

    public CleansingRitualMessage(FriendlyByteBuf buf) {
        this.x = buf.readVarInt();
        this.z = buf.readVarInt();
        this.biomeID = buf.readVarInt();
        this.batched = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(z);
        buf.writeVarInt(biomeID);
        buf.writeBoolean(batched);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the cleansing ritual + client biome refresh are not yet ported (system stage).
    }
}
