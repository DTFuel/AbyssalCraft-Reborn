package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.client.ritual.ClientRitualEffects;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: report the outcome of the ritual {@code id} at {@code pos} (with the caused
 * {@code disruption} deity and a {@code failed} flag) for client feedback (owned by PS-1). Serialization
 * is faithful; the client feedback (particles/sound/messages) is deferred until the ritual system is
 * ported (PS-6).
 */
public class RitualMessage implements NetworkChannel.ACPacket {

    private final String id;
    private final String disruption;
    private final BlockPos pos;
    private final boolean failed;

    public RitualMessage(String id, String disruption, BlockPos pos, boolean failed) {
        this.id = id;
        this.disruption = disruption;
        this.pos = pos;
        this.failed = failed;
    }

    public RitualMessage(FriendlyByteBuf buf) {
        this.id = buf.readUtf(128);
        this.disruption = buf.readUtf(128);
        this.pos = buf.readBlockPos();
        this.failed = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(disruption);
        buf.writeBlockPos(pos);
        buf.writeBoolean(failed);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () -> ClientRitualEffects.finish(pos, id, disruption, failed));
    }
}
