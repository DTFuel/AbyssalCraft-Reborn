package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: toggle the running state of the item-transfer host block-entity at {@code pos}
 * (owned by PS-1). Targets the ported PC-4 {@code ItemTransferHost}; the handler wiring (fetch the BE,
 * flip running) is left to the transfer-host consumer task so this message stays free of a BE lookup.
 */
public class ToggleStateMessage implements NetworkChannel.ACPacket {

    private final BlockPos pos;

    public ToggleStateMessage(BlockPos pos) {
        this.pos = pos;
    }

    public ToggleStateMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    /** The targeted block position. */
    public BlockPos pos() {
        return pos;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Target ported (PC-4 ItemTransferHost). Deferred: fetch the BE at pos on the server and, if it
        // is an ItemTransferHost, flip its running flag -- wired when a transfer-host block consumes it.
    }
}
