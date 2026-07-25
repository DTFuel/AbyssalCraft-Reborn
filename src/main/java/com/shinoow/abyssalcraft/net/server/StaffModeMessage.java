package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: cycle the mode of the Staff of the Gatekeeper held by the sender (owned by
 * PS-1). Carries no payload. The mode-cycle effect is deferred until the staff item is ported.
 */
public class StaffModeMessage implements NetworkChannel.ACPacket {

    public StaffModeMessage() {}

    public StaffModeMessage(FriendlyByteBuf buf) {}

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the Staff of the Gatekeeper item is not yet ported (item stage).
    }
}
