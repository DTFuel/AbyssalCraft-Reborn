package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: apply spirit-tablet menu settings -- the two mode selectors plus the
 * open-filter / clear-path toggles (owned by PS-1). Serialization is faithful; applying the settings is
 * deferred until the spirit-tablet menu is ported.
 */
public class SpiritTabletMessage implements NetworkChannel.ACPacket {

    private final int mode1;
    private final int mode2;
    private final boolean openFilter;
    private final boolean clearPath;

    public SpiritTabletMessage(int mode1, int mode2, boolean openFilter, boolean clearPath) {
        this.mode1 = mode1;
        this.mode2 = mode2;
        this.openFilter = openFilter;
        this.clearPath = clearPath;
    }

    public SpiritTabletMessage(FriendlyByteBuf buf) {
        this.mode1 = buf.readVarInt();
        this.mode2 = buf.readVarInt();
        this.openFilter = buf.readBoolean();
        this.clearPath = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(mode1);
        buf.writeVarInt(mode2);
        buf.writeBoolean(openFilter);
        buf.writeBoolean(clearPath);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the spirit-tablet menu is not yet ported (system stage).
    }
}
