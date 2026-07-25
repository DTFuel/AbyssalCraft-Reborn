package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: send the set of energy-pedestal routes (encoded as {@code nbt}) for the client
 * to draw as particle streams (owned by PS-1). Serialization is faithful (raw NBT); the client-side
 * route rendering is deferred until the energy-pedestal system is ported (PS-5).
 */
public class DisplayRoutesMessage implements NetworkChannel.ACPacket {

    private final CompoundTag nbt;

    public DisplayRoutesMessage(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public DisplayRoutesMessage(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the energy-pedestal route rendering (PS-5) is not yet ported -- it lands with it.
    }
}
