package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.client.necronomicon.ClientNecroSync;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: sync the full necromancy data {@code properties} to the client (owned by PS-1).
 * Serialization is faithful (raw NBT); the client apply overwrites the local player's necrodata (PS-2)
 * so the Necronomicon shows the player's real, server-authoritative knowledge.
 */
public class SyncNecromancyDataMessage implements NetworkChannel.ACPacket {

    private final CompoundTag properties;

    public SyncNecromancyDataMessage(CompoundTag properties) {
        this.properties = properties;
    }

    public SyncNecromancyDataMessage(FriendlyByteBuf buf) {
        this.properties = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(properties);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Client: overwrite our necrodata with the server's authoritative copy (client-only classload).
        SideExecutor.runWhenClient(() -> () -> ClientNecroSync.apply(properties));
    }
}
