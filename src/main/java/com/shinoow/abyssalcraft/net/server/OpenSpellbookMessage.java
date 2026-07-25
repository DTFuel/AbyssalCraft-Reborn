package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: request opening the spellbook GUI for the sender (owned by PS-1). Carries no
 * payload. Opening the menu is deferred until the spellbook GUI is ported.
 */
public class OpenSpellbookMessage implements NetworkChannel.ACPacket {

    public OpenSpellbookMessage() {}

    public OpenSpellbookMessage(FriendlyByteBuf buf) {}

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the spellbook GUI/menu is not yet ported (system stage).
    }
}
