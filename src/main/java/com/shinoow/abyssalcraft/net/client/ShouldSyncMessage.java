package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.client.necronomicon.ClientNecroSync;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: tell the client it should (re)sync its necromancy data, carrying the server
 * {@code time} stamp (owned by PS-1). The client responds by requesting a sync ({@code PrepareSyncMessage}).
 * The 1.12.2 {@code lastSyncTime} staleness check is simplified here to always request (the extra sync is
 * cheap); the timestamp is still carried faithfully for a future optimisation.
 */
public class ShouldSyncMessage implements NetworkChannel.ACPacket {

    private final long time;

    public ShouldSyncMessage(long time) {
        this.time = time;
    }

    public ShouldSyncMessage(FriendlyByteBuf buf) {
        this.time = buf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(time);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Client: ask the server to send our current necrodata back (client-only classload).
        SideExecutor.runWhenClient(() -> () -> ClientNecroSync.requestSync());
    }
}
