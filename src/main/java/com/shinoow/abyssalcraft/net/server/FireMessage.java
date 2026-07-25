package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: extinguish a mimic-fire block at {@code pos} (owned by PS-1). Serialization is
 * faithful; the effect (extinguish + sound) is deferred until the mimic-fire block is ported.
 */
public class FireMessage implements NetworkChannel.ACPacket {

    private final BlockPos pos;

    public FireMessage(BlockPos pos) {
        this.pos = pos;
    }

    public FireMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the mimic_fire block is not yet ported (block content stage). When it lands, this
        // should verify the block at pos is mimic_fire and extinguish it (setBlockToAir + sound).
    }
}
