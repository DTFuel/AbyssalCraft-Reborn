package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Client &rarr; server: activate the Interdimensional Cage held in {@code hand} on the entity with
 * network {@code id} (owned by PS-1). Serialization is faithful; the capture effect is deferred until
 * the energy-container API + cage item are ported.
 */
public class InterdimensionalCageMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final InteractionHand hand;

    public InterdimensionalCageMessage(int id, InteractionHand hand) {
        this.id = id;
        this.hand = hand;
    }

    public InterdimensionalCageMessage(FriendlyByteBuf buf) {
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
        // Deferred: the energy-container item API + Interdimensional Cage item are not yet ported.
    }
}
