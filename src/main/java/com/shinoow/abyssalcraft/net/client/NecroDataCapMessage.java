package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.client.necronomicon.ClientNecroSync;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: sync the necromancy data capability {@code properties} to the client (owned by
 * PS-1). Serialization is faithful (raw NBT); the client replaces its local copy with this
 * server-authoritative snapshot.
 */
public class NecroDataCapMessage implements NetworkChannel.ACPacket {

    private final CompoundTag properties;

    public NecroDataCapMessage(CompoundTag properties) {
        this.properties = properties;
    }

    public NecroDataCapMessage(FriendlyByteBuf buf) {
        this.properties = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(properties);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () -> ClientNecroSync.apply(properties));
    }
}
