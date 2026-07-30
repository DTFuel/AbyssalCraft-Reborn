package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: draw a Purified Essence particle stream from {@code posFrom} to {@code posTo}
 * (owned by PS-1). Serialization remains the faithful pair of block positions; the client restores the
 * legacy fifteen-samples-per-block coloured stream.
 */
public class PEStreamMessage implements NetworkChannel.ACPacket {

    private final BlockPos posFrom;
    private final BlockPos posTo;

    public PEStreamMessage(BlockPos posFrom, BlockPos posTo) {
        this.posFrom = posFrom;
        this.posTo = posTo;
    }

    public PEStreamMessage(FriendlyByteBuf buf) {
        this.posFrom = buf.readBlockPos();
        this.posTo = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(posFrom);
        buf.writeBlockPos(posTo);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () ->
            com.shinoow.abyssalcraft.client.network.ClientNetworkEffects.peStream(posFrom, posTo));
    }
}
