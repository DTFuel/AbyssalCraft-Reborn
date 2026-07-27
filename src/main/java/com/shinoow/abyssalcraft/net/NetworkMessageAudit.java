package com.shinoow.abyssalcraft.net;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.shinoow.abyssalcraft.net.server.NecronomiconPageActionMessage;
import com.shinoow.abyssalcraft.net.server.PrepareSyncMessage;
import com.shinoow.abyssalcraft.net.server.SpiritTabletMessage;
import com.shinoow.abyssalcraft.net.server.StaffModeMessage;
import com.shinoow.abyssalcraft.net.server.StaffOfRendingMessage;
import com.shinoow.abyssalcraft.net.server.ToggleStateMessage;
import com.shinoow.abyssalcraft.net.server.TransferStackMessage;
import com.shinoow.abyssalcraft.net.server.UpdateModeMessage;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

/** Frozen migration state for 23 legacy messages plus one modern extension. */
public final class NetworkMessageAudit {

    public enum Direction { SERVER_BOUND, CLIENT_BOUND }
    public enum Status { MIGRATED, REPLACED, BLOCKED }

    public record Entry(int id, Class<? extends NetworkChannel.ACPacket> type,
                        Direction direction, Status status) {}

    public static final List<Entry> ALL = List.of(
        migrated(0, FireMessage.class, Direction.SERVER_BOUND),
        replaced(1, UpdateModeMessage.class, Direction.SERVER_BOUND),
        migrated(2, ToggleStateMessage.class, Direction.SERVER_BOUND),
        migrated(3, StaffOfRendingMessage.class, Direction.SERVER_BOUND),
        migrated(4, StaffModeMessage.class, Direction.SERVER_BOUND),
        migrated(5, SpiritTabletMessage.class, Direction.SERVER_BOUND),
        migrated(6, PrepareSyncMessage.class, Direction.SERVER_BOUND),
        migrated(7, OpenSpellbookMessage.class, Direction.SERVER_BOUND),
        migrated(8, MobSpellMessage.class, Direction.SERVER_BOUND),
        migrated(9, InterdimensionalCageMessage.class, Direction.SERVER_BOUND),
        replaced(10, TransferStackMessage.class, Direction.SERVER_BOUND),
        replaced(11, WindowPropertyMessage.class, Direction.CLIENT_BOUND),
        migrated(12, RitualMessage.class, Direction.CLIENT_BOUND),
        migrated(13, RitualStartMessage.class, Direction.CLIENT_BOUND),
        replaced(14, CleansingRitualMessage.class, Direction.CLIENT_BOUND),
        replaced(15, DisruptionMessage.class, Direction.CLIENT_BOUND),
        migrated(16, EvilSheepMessage.class, Direction.CLIENT_BOUND),
        migrated(17, KnowledgeUnlockMessage.class, Direction.CLIENT_BOUND),
        migrated(18, NecroDataCapMessage.class, Direction.CLIENT_BOUND),
        migrated(19, PEStreamMessage.class, Direction.CLIENT_BOUND),
        migrated(20, ShouldSyncMessage.class, Direction.CLIENT_BOUND),
        migrated(21, SyncNecromancyDataMessage.class, Direction.CLIENT_BOUND),
        migrated(22, DisplayRoutesMessage.class, Direction.CLIENT_BOUND),
        migrated(23, NecronomiconPageActionMessage.class, Direction.SERVER_BOUND));

    private NetworkMessageAudit() {}

    public static void validate(NetworkChannel channel) {
        require(ALL.size() == 24, "network audit size changed");
        Set<Integer> ids = new HashSet<>();
        Set<Class<?>> types = new HashSet<>();
        for (Entry entry : ALL) {
            require(ids.add(entry.id()), "duplicate audited packet id " + entry.id());
            require(types.add(entry.type()), "duplicate audited packet type " + entry.type().getName());
            require(channel.registeredId(entry.type()) == entry.id(),
                "wire id changed for " + entry.type().getSimpleName());
            require(channel.registeredDirection(entry.type()) == platformDirection(entry.direction()),
                "registered direction changed for " + entry.type().getSimpleName());
        }
        require(ids.equals(java.util.stream.IntStream.range(0, 24).boxed()
            .collect(java.util.stream.Collectors.toSet())), "wire ids are not the closed range 0..23");
        require(channel.registeredCount() == 24 && channel.registeredIds().equals(ids),
            "registered network catalog differs from audit");
        require(count(Status.MIGRATED) == 19, "migrated packet count changed");
        require(count(Status.REPLACED) == 5, "replaced packet count changed");
        require(count(Status.BLOCKED) == 0, "blocked packet count changed");
        require(count(Direction.SERVER_BOUND) == 12, "server-bound packet count changed");
        require(count(Direction.CLIENT_BOUND) == 12, "client-bound packet count changed");
    }

    public static long count(Status status) {
        return ALL.stream().filter(entry -> entry.status() == status).count();
    }

    public static long count(Direction direction) {
        return ALL.stream().filter(entry -> entry.direction() == direction).count();
    }

    public static NetworkChannel.Direction platformDirection(Direction direction) {
        return direction == Direction.SERVER_BOUND
            ? NetworkChannel.Direction.SERVER_BOUND : NetworkChannel.Direction.CLIENT_BOUND;
    }

    private static Entry migrated(int id, Class<? extends NetworkChannel.ACPacket> type, Direction direction) {
        return new Entry(id, type, direction, Status.MIGRATED);
    }

    private static Entry replaced(int id, Class<? extends NetworkChannel.ACPacket> type, Direction direction) {
        return new Entry(id, type, direction, Status.REPLACED);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}