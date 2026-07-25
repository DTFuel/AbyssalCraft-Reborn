package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: start the client-side animation of ritual {@code id} at {@code pos} with the
 * given {@code sacrifice} count and {@code timerMax} duration (owned by PS-1). Serialization is
 * faithful; the animation is deferred until the ritual system is ported (PS-6).
 */
public class RitualStartMessage implements NetworkChannel.ACPacket {

    private final BlockPos pos;
    private final String id;
    private final int sacrifice;
    private final int timerMax;

    public RitualStartMessage(BlockPos pos, String id, int sacrifice, int timerMax) {
        this.pos = pos;
        this.id = id;
        this.sacrifice = sacrifice;
        this.timerMax = timerMax;
    }

    public RitualStartMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readUtf();
        this.sacrifice = buf.readVarInt();
        this.timerMax = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(id);
        buf.writeVarInt(sacrifice);
        buf.writeVarInt(timerMax);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the ritual altar system (PS-6) is not yet ported -- the animation lands with it.
    }
}
