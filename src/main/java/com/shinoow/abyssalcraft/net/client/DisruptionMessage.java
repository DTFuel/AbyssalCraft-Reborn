package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: announce a disruption caused by {@code deity} (display {@code name}) at
 * {@code pos} for client feedback (owned by PS-1). Serialization is faithful; the feedback is deferred
 * until the disruption system is ported (PS-9).
 */
public class DisruptionMessage implements NetworkChannel.ACPacket {

    private final String deity;
    private final String name;
    private final BlockPos pos;

    public DisruptionMessage(String deity, String name, BlockPos pos) {
        this.deity = deity;
        this.name = name;
        this.pos = pos;
    }

    public DisruptionMessage(FriendlyByteBuf buf) {
        this.deity = buf.readUtf();
        this.name = buf.readUtf();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(deity);
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () ->
            com.shinoow.abyssalcraft.client.network.ClientNetworkEffects.disruption(deity, name, pos));
    }
}
