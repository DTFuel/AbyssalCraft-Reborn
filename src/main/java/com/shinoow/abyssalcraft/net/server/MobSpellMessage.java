package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client &rarr; server: cast the spell {@code spellID} (of the given scroll/quality {@code scrollType})
 * at the entity with network {@code id} (owned by PS-1). Serialization is faithful; casting is deferred
 * until the spell system is ported.
 */
public class MobSpellMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final String spellID;
    private final int scrollType;

    public MobSpellMessage(int id, String spellID, int scrollType) {
        this.id = id;
        this.spellID = spellID;
        this.scrollType = scrollType;
    }

    public MobSpellMessage(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.spellID = buf.readUtf();
        this.scrollType = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeUtf(spellID);
        buf.writeVarInt(scrollType);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        // Deferred: the spell system (spell registry + casting) is not yet ported (system stage).
    }
}
