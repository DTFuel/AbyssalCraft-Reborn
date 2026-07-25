package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Client &rarr; server: use the Staff of Rending held in {@code hand} on the entity with network
 * {@code id} (owned by PS-1). Serialization is faithful; the rending effect is deferred until the
 * rending API + staff item are ported.
 */
public class StaffOfRendingMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final InteractionHand hand;

    public StaffOfRendingMessage(int id, InteractionHand hand) {
        this.id = id;
        this.hand = hand;
    }

    public StaffOfRendingMessage(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.hand = buf.readVarInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeVarInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the rending API + Staff of Rending item are not yet ported (system/item stage).
    }
}
