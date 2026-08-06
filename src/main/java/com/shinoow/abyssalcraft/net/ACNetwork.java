package com.shinoow.abyssalcraft.net;

import com.shinoow.abyssalcraft.net.client.CleansingRitualMessage;
import com.shinoow.abyssalcraft.net.client.DisplayRoutesMessage;
import com.shinoow.abyssalcraft.net.client.DisruptionMessage;
import com.shinoow.abyssalcraft.net.client.EvilSheepMessage;
import com.shinoow.abyssalcraft.net.client.KnowledgeUnlockMessage;
import com.shinoow.abyssalcraft.net.client.LineEffectMessage;
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
import com.shinoow.abyssalcraft.net.server.NecronomiconPageActionMessage;
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
 * NeoForge payloads, hidden in {@code platform/NetworkChannel}) plus the frozen registration of 23
 * legacy messages and modern extensions. This proves its send/receive path on both
 * loaders. {@link #bootstrap(Object)} is called once from the main class with the MOD event bus.
 *
 * <p>Ids 0-10 and 23 are server-bound (client&rarr;server), 11-22 client-bound (server&rarr;client); ids
 * are stable (the wire uses the numeric id, not the class name). Every message is a fork-free
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
        CHANNEL.register(0, FireMessage.class, NetworkChannel.Direction.SERVER_BOUND, FireMessage::new);
        CHANNEL.register(1, UpdateModeMessage.class, NetworkChannel.Direction.SERVER_BOUND, UpdateModeMessage::new);
        CHANNEL.register(2, ToggleStateMessage.class, NetworkChannel.Direction.SERVER_BOUND, ToggleStateMessage::new);
        CHANNEL.register(3, StaffOfRendingMessage.class, NetworkChannel.Direction.SERVER_BOUND, StaffOfRendingMessage::new);
        CHANNEL.register(4, StaffModeMessage.class, NetworkChannel.Direction.SERVER_BOUND, StaffModeMessage::new);
        CHANNEL.register(5, SpiritTabletMessage.class, NetworkChannel.Direction.SERVER_BOUND, SpiritTabletMessage::new);
        CHANNEL.register(6, PrepareSyncMessage.class, NetworkChannel.Direction.SERVER_BOUND, PrepareSyncMessage::new);
        CHANNEL.register(7, OpenSpellbookMessage.class, NetworkChannel.Direction.SERVER_BOUND, OpenSpellbookMessage::new);
        CHANNEL.register(8, MobSpellMessage.class, NetworkChannel.Direction.SERVER_BOUND, MobSpellMessage::new);
        CHANNEL.register(9, InterdimensionalCageMessage.class, NetworkChannel.Direction.SERVER_BOUND, InterdimensionalCageMessage::new);
        CHANNEL.register(10, TransferStackMessage.class, NetworkChannel.Direction.SERVER_BOUND, TransferStackMessage::new);
        // --- client-bound (server -> client), ids 11-22 ---
        CHANNEL.register(11, WindowPropertyMessage.class, NetworkChannel.Direction.CLIENT_BOUND, WindowPropertyMessage::new);
        CHANNEL.register(12, RitualMessage.class, NetworkChannel.Direction.CLIENT_BOUND, RitualMessage::new);
        CHANNEL.register(13, RitualStartMessage.class, NetworkChannel.Direction.CLIENT_BOUND, RitualStartMessage::new);
        CHANNEL.register(14, CleansingRitualMessage.class, NetworkChannel.Direction.CLIENT_BOUND, CleansingRitualMessage::new);
        CHANNEL.register(15, DisruptionMessage.class, NetworkChannel.Direction.CLIENT_BOUND, DisruptionMessage::new);
        CHANNEL.register(16, EvilSheepMessage.class, NetworkChannel.Direction.CLIENT_BOUND, EvilSheepMessage::new);
        CHANNEL.register(17, KnowledgeUnlockMessage.class, NetworkChannel.Direction.CLIENT_BOUND, KnowledgeUnlockMessage::new);
        CHANNEL.register(18, NecroDataCapMessage.class, NetworkChannel.Direction.CLIENT_BOUND, NecroDataCapMessage::new);
        CHANNEL.register(19, PEStreamMessage.class, NetworkChannel.Direction.CLIENT_BOUND, PEStreamMessage::new);
        CHANNEL.register(20, ShouldSyncMessage.class, NetworkChannel.Direction.CLIENT_BOUND, ShouldSyncMessage::new);
        CHANNEL.register(21, SyncNecromancyDataMessage.class, NetworkChannel.Direction.CLIENT_BOUND, SyncNecromancyDataMessage::new);
        CHANNEL.register(22, DisplayRoutesMessage.class, NetworkChannel.Direction.CLIENT_BOUND, DisplayRoutesMessage::new);
        // --- modern extension (not part of the frozen 23-message legacy catalog) ---
        CHANNEL.register(23, NecronomiconPageActionMessage.class, NetworkChannel.Direction.SERVER_BOUND,
            NecronomiconPageActionMessage::new);
        CHANNEL.register(24, LineEffectMessage.class, NetworkChannel.Direction.CLIENT_BOUND,
            LineEffectMessage::new);
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
