package com.shinoow.abyssalcraft.net.client;

import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.platform.SideExecutor;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server &rarr; client: update data slot {@code property} of the open menu {@code windowId} to
 * {@code value} (owned by PS-1). Serialization is faithful; applying it is deferred -- modern menus sync
 * {@code ContainerData} automatically, and a client-side handler needs the client player (see
 * {@link NetworkChannel.Context#player()}), so any remaining manual use lands with the owning menu.
 */
public class WindowPropertyMessage implements NetworkChannel.ACPacket {

    private final int windowId;
    private final int property;
    private final int value;

    public WindowPropertyMessage(int windowId, int property, int value) {
        this.windowId = windowId;
        this.property = property;
        this.value = value;
    }

    public WindowPropertyMessage(FriendlyByteBuf buf) {
        this.windowId = buf.readVarInt();
        this.property = buf.readVarInt();
        this.value = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(windowId);
        buf.writeVarInt(property);
        buf.writeVarInt(value);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        SideExecutor.runWhenClient(() -> () ->
            com.shinoow.abyssalcraft.client.network.ClientNetworkEffects.updateWindow(windowId, property, value));
    }
}
