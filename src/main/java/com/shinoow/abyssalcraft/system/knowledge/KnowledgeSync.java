package com.shinoow.abyssalcraft.system.knowledge;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.KnowledgeUnlockMessage;
import com.shinoow.abyssalcraft.net.client.NecroDataCapMessage;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;

import net.minecraft.server.level.ServerPlayer;

/** Server-authoritative synchronization for player knowledge mutations. */
public final class KnowledgeSync {

    private KnowledgeSync() {}

    public static void trigger(ServerPlayer player, int type, String value) {
        unlock(player, type, value);
        full(player);
    }

    public static void unlock(ServerPlayer player, int type, String value) {
        ACNetwork.sendToPlayer(player, new KnowledgeUnlockMessage(type, value));
    }

    public static void full(ServerPlayer player) {
        ACNetwork.sendToPlayer(player, new NecroDataCapMessage(NecroDataCapability.save(player)));
    }
}