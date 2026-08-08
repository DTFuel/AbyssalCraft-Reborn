package com.shinoow.abyssalcraft.client.necronomicon;

import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.PrepareSyncMessage;
import com.shinoow.abyssalcraft.system.cap.necrodata.NecroDataCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side necrodata sync helpers (owned by PH-5 / knowledge wiring). Isolated in a client class so the
 * common network messages ({@code net/**}) reach it only through {@code SideExecutor}, keeping {@code Minecraft}
 * off the dedicated server. Together with the server-side handlers this completes the 1.12.2 sync dance
 * (book-open &rarr; {@code ShouldSync} &rarr; {@code PrepareSync} &rarr; {@code SyncNecromancyData}) so the
 * Necronomicon reads the player's real, server-authoritative knowledge.
 */
public final class ClientNecroSync {

    private ClientNecroSync() {}

    /** Overwrite the local player's necrodata from a server sync ({@code SyncNecromancyDataMessage}). */
    public static void apply(CompoundTag tag) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            NecroDataCapability.apply(player, tag);
        }
    }

    /** Apply an incremental unlock while the authoritative full snapshot is in flight. */
    public static void applyUnlock(int type, String value) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var data = NecroDataCapability.get(player);
        switch (type) {
            case 0 -> data.triggerBiomeUnlock(value);
            case 1 -> data.triggerEntityUnlock(value);
            case 2 -> data.triggerDimensionUnlock(value);
            case 3 -> data.triggerArtifactUnlock(value);
            case 4 -> data.triggerPageUnlock(value);
            case 5 -> data.triggerWhisperUnlock(value);
            case 6 -> data.triggerMiscUnlock(value);
            case 7 -> data.triggerAdvancementUnlock(value);
            default -> {
            }
        }
    }

    /** Ask the server to (re)send our necrodata ({@code ShouldSyncMessage} response). */
    public static void requestSync() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ACNetwork.sendToServer(new PrepareSyncMessage(player.getUUID()));
        }
    }
}
