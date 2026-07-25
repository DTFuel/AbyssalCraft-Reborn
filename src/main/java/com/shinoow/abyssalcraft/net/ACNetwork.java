package com.shinoow.abyssalcraft.net;

import com.shinoow.abyssalcraft.net.client.CleansingRitualMessage;
import com.shinoow.abyssalcraft.net.client.DisplayRoutesMessage;
import com.shinoow.abyssalcraft.net.client.DisruptionMessage;
import com.shinoow.abyssalcraft.net.client.EvilSheepMessage;
import com.shinoow.abyssalcraft.net.client.KnowledgeUnlockMessage;
import com.shinoow.abyssalcraft.net.client.NecroDataCapMessage;
import com.shinoow.abyssalcraft.net.client.PEStreamMessage;
import com.shinoow.abyssalcraft.net.client.RitualMessage;
import com.shinoow.abyssalcraft.net.client.RitualStartMessage;
import com.shinoow.abyssalcraft.net.client.ShouldSyncMessage;
import com.shinoow.abyssalcraft.net.client.SyncNecromancyDataMessage;
import com.shinoow.abyssalcraft.net.client.WindowPropertyMessage;
import com.shinoow.abyssalcraft.net.server.FireMessage;
import com.shinoow.abyssalcraft.net.server.InterdimensionalCageMessage;
import com.shinoow.abyssalcraft.net.server.MobSpellMessage;
import com.shinoow.abyssalcraft.net.server.OpenSpellbookMessage;
import com.shinoow.abyssalcraft.net.server.PrepareSyncMessage;
import com.shinoow.abyssalcraft.net.server.SpiritTabletMessage;
import com.shinoow.abyssalcraft.net.server.StaffModeMessage;
import com.shinoow.abyssalcraft.net.server.StaffOfRendingMessage;
import com.shinoow.abyssalcraft.net.server.ToggleStateMessage;
import com.shinoow.abyssalcraft.net.server.TransferStackMessage;
import com.shinoow.abyssalcraft.net.server.UpdateModeMessage;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.server.level.ServerPlayer;

/**
 * AbyssalCraft network layer (owned by PS-1, Stage S-A, Maps T7.1).
 *
 * <p>The single {@link NetworkChannel} (multiplexed envelope over Forge {@code SimpleChannel} /
 * NeoForge payloads, hidden in {@code platform/NetworkChannel}) plus the registration of all 23 mod
 * messages. This is the first runtime consumer of the compat, proving its send/receive path on both
 * loaders. {@link #bootstrap(Object)} is called once from the main class with the MOD event bus.
 *
 * <p>Ids 0-10 are server-bound (client&rarr;server), 11-22 client-bound (server&rarr;client); ids are
 * stable (the wire uses the numeric id, not the class name). Every message is a fork-free
 * {@link NetworkChannel.ACPacket} whose serialization uses only version-stable {@code FriendlyByteBuf}
 * primitives (varint / UTF / BlockPos / UUID / long / boolean / NBT; the lone ItemStack is written as
 * item-id + count to dodge the 1.21 {@code RegistryFriendlyByteBuf} requirement).
 *
 * <p><b>Handlers:</b> each message deserializes faithfully now, but most {@code handle} bodies are
 * documented stubs deferred to the Stage-S task that owns the target system (ritual &rarr; PS-6, spell
 * &rarr; PS-7, PE &rarr; PS-5, necromancy/knowledge &rarr; PS-8, disruption &rarr; PS-9, plus not-yet-
 * ported items/blocks/menus). <b>Note for those tasks:</b> {@link NetworkChannel.Context#player()}
 * returns the sending player (server side) -- correct for server-bound handlers; a client-bound handler
 * that needs the receiving client player must fetch it client-side (via {@code SideExecutor}).
 */
public final class ACNetwork {

    private ACNetwork() {}

    /** The mod's single multiplexed channel. */
    public static final NetworkChannel CHANNEL = NetworkChannel.create("main");

    static {
        // --- server-bound (client -> server), ids 0-10 ---
        CHANNEL.register(0, FireMessage.class, FireMessage::new);
        CHANNEL.register(1, UpdateModeMessage.class, UpdateModeMessage::new);
        CHANNEL.register(2, ToggleStateMessage.class, ToggleStateMessage::new);
        CHANNEL.register(3, StaffOfRendingMessage.class, StaffOfRendingMessage::new);
        CHANNEL.register(4, StaffModeMessage.class, StaffModeMessage::new);
        CHANNEL.register(5, SpiritTabletMessage.class, SpiritTabletMessage::new);
        CHANNEL.register(6, PrepareSyncMessage.class, PrepareSyncMessage::new);
        CHANNEL.register(7, OpenSpellbookMessage.class, OpenSpellbookMessage::new);
        CHANNEL.register(8, MobSpellMessage.class, MobSpellMessage::new);
        CHANNEL.register(9, InterdimensionalCageMessage.class, InterdimensionalCageMessage::new);
        CHANNEL.register(10, TransferStackMessage.class, TransferStackMessage::new);
        // --- client-bound (server -> client), ids 11-22 ---
        CHANNEL.register(11, WindowPropertyMessage.class, WindowPropertyMessage::new);
        CHANNEL.register(12, RitualMessage.class, RitualMessage::new);
        CHANNEL.register(13, RitualStartMessage.class, RitualStartMessage::new);
        CHANNEL.register(14, CleansingRitualMessage.class, CleansingRitualMessage::new);
        CHANNEL.register(15, DisruptionMessage.class, DisruptionMessage::new);
        CHANNEL.register(16, EvilSheepMessage.class, EvilSheepMessage::new);
        CHANNEL.register(17, KnowledgeUnlockMessage.class, KnowledgeUnlockMessage::new);
        CHANNEL.register(18, NecroDataCapMessage.class, NecroDataCapMessage::new);
        CHANNEL.register(19, PEStreamMessage.class, PEStreamMessage::new);
        CHANNEL.register(20, ShouldSyncMessage.class, ShouldSyncMessage::new);
        CHANNEL.register(21, SyncNecromancyDataMessage.class, SyncNecromancyDataMessage::new);
        CHANNEL.register(22, DisplayRoutesMessage.class, DisplayRoutesMessage::new);
    }

    /** Wire the channel to the MOD bus. Triggers the static registration first. */
    public static void bootstrap(Object modBus) {
        CHANNEL.bootstrap(modBus);
    }

    public static void sendToServer(NetworkChannel.ACPacket msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(ServerPlayer target, NetworkChannel.ACPacket msg) {
        CHANNEL.sendToPlayer(target, msg);
    }

    public static void sendToAll(NetworkChannel.ACPacket msg) {
        CHANNEL.sendToAll(msg);
    }
}
