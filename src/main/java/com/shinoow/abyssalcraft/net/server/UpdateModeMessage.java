package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: set the mode of the open state-transformer ({@code container==0}) or spirit
 * tablet ({@code container==1}) menu (owned by PS-1). Serialization is faithful; the effect is deferred
 * until those menus are ported.
 */
public class UpdateModeMessage implements NetworkChannel.ACPacket {

    private final int mode;
    private final int container;

    public UpdateModeMessage(int mode, int container) {
        this.mode = mode;
        this.container = container;
    }

    public UpdateModeMessage(FriendlyByteBuf buf) {
        this.mode = buf.readVarInt();
        this.container = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(mode);
        buf.writeVarInt(container);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the state-transformer / spirit-tablet menus are not yet ported (system stage).
    }
}
