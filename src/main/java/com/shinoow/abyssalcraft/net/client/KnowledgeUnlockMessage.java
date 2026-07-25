package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.client.necronomicon.ClientNecroSync;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: unlock a piece of necronomicon knowledge on the client (owned by PS-1). The
 * The legacy numeric dimension payload is modernised to the same namespaced string representation used
 * by every other trigger.
 */
public class KnowledgeUnlockMessage implements NetworkChannel.ACPacket {

    private final int type;
    private final String data;

    public KnowledgeUnlockMessage(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public KnowledgeUnlockMessage(FriendlyByteBuf buf) {
        this.type = buf.readVarInt();
        this.data = buf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(type);
        buf.writeUtf(data);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () -> ClientNecroSync.applyUnlock(type, data));
    }
}
