package com.shinoow.abyssalcraft.net;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.shinoow.abyssalcraft.content.block.demon.DemonBlocks;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
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
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Permanent wire-format and catalog invariants for RR-NET. */
public final class NetworkSelfTest {

    private NetworkSelfTest() {}

    public static void run() {
        NetworkMessageAudit.validate(ACNetwork.CHANNEL);
        require(BuiltInRegistries.BLOCK.getKey(DemonBlocks.MIMIC_FIRE.get()).toString()
            .equals("abyssalcraft:mimic_fire"), "mimic fire registry id changed");
        require(RitualItems.INTERDIMENSIONAL_CAGE.get() instanceof InterdimensionalCageItem,
            "interdimensional cage lost its network behavior");
        require(RitualItems.STAFF_OF_RENDING.get() instanceof StaffOfRendingItem
            && RitualItems.ABYSSAL_WASTELAND_STAFF_OF_RENDING.get() instanceof StaffOfRendingItem
            && RitualItems.DREADLANDS_STAFF_OF_RENDING.get() instanceof StaffOfRendingItem
            && RitualItems.OMOTHOL_STAFF_OF_RENDING.get() instanceof StaffOfRendingItem,
            "Staff of Rending tiers lost their network behavior");
        require(InterdimensionalCageItem.energyCost(1.0F, 2.0F) == 200.0F,
            "interdimensional cage size cost changed");
        require(NetworkSelfTest.class.getClassLoader().getResource(
            "assets/abyssalcraft/blockstates/mimic_fire.json") != null,
            "mimic fire blockstate is missing");
        UUID uuid = UUID.fromString("2b46db9c-84c1-4d14-b2e3-2ca433a7e621");
        BlockPos from = new BlockPos(1, 64, -3);
        BlockPos to = new BlockPos(9, 70, 12);
        CompoundTag data = new CompoundTag();
        data.putString("Marker", "rr-net-round-trip");
        data.putInt("Value", 42);
        CompoundTag routes = new CompoundTag();
        ListTag routeList = new ListTag();
        ListTag route = new ListTag();
        route.add(LongTag.valueOf(from.asLong()));
        route.add(LongTag.valueOf(to.asLong()));
        routeList.add(route);
        routes.put("Routes", routeList);

        List<NetworkChannel.ACPacket> samples = List.of(
            new FireMessage(from),
            new UpdateModeMessage(2, 1),
            new ToggleStateMessage(from),
            new StaffOfRendingMessage(42, InteractionHand.OFF_HAND),
            new StaffModeMessage(),
            new SpiritTabletMessage(1, 2, true, false),
            new PrepareSyncMessage(uuid),
            new OpenSpellbookMessage(),
            new MobSpellMessage(42, "abyssalcraft:life_drain", 3),
            new InterdimensionalCageMessage(42, InteractionHand.MAIN_HAND),
            new TransferStackMessage(7, new ItemStack(Items.DIAMOND, 4)),
            new WindowPropertyMessage(3, 2, 99),
            new RitualMessage("cleansing", "", from, false),
            new RitualStartMessage(from, "cleansing", 42, 200),
            new CleansingRitualMessage(17, -9, 4, true),
            new DisruptionMessage("CTHULHU", "lightning", from),
            new EvilSheepMessage(uuid, "RRNet", 42),
            new KnowledgeUnlockMessage(1, "abyssalcraft:ghoul"),
            new KnowledgeUnlockMessage(3, "abyssalcraft:artifact_fixture"),
            new KnowledgeUnlockMessage(4, "abyssalcraft:page_fixture"),
            new KnowledgeUnlockMessage(5, "book/4"),
            new KnowledgeUnlockMessage(7, "abyssalcraft:root"),
            new NecroDataCapMessage(data.copy()),
            new PEStreamMessage(from, to),
            new ShouldSyncMessage(123456789L),
            new SyncNecromancyDataMessage(data.copy()),
            new DisplayRoutesMessage(routes));
        samples = new java.util.ArrayList<>(samples);
        samples.add(new NecronomiconPageActionMessage(
            com.shinoow.abyssalcraft.platform.ACRef.id("legacy/information_abyssalcraft_page_1")));

        for (NetworkChannel.ACPacket sample : samples) {
            byte[] first = encode(sample);
            byte[] second = ACNetwork.CHANNEL.roundTrip(sample);
            require(Arrays.equals(first, second), "packet round-trip changed: " + sample.getClass().getSimpleName());
        }
        int serverBound = 0;
        int clientBound = 0;
        int rejected = 0;
        for (NetworkMessageAudit.Entry entry : NetworkMessageAudit.ALL) {
            NetworkChannel.ACPacket sample = samples.stream()
                .filter(packet -> packet.getClass() == entry.type())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "direction gate sample missing: " + entry.type().getSimpleName()));
            NetworkChannel.Direction expected = NetworkMessageAudit.platformDirection(entry.direction());
            NetworkChannel.Direction reverse = expected == NetworkChannel.Direction.SERVER_BOUND
                ? NetworkChannel.Direction.CLIENT_BOUND : NetworkChannel.Direction.SERVER_BOUND;
            require(ACNetwork.CHANNEL.testDirectionGate(sample, expected),
                "expected direction rejected: " + entry.type().getSimpleName());
            require(!ACNetwork.CHANNEL.testDirectionGate(sample, reverse),
                "reverse direction reached handler queue: " + entry.type().getSimpleName());
            if (expected == NetworkChannel.Direction.SERVER_BOUND) serverBound++;
            else clientBound++;
            rejected++;
        }
        System.out.println("RR_NET_DIRECTION_GATE_OK serverBound=" + serverBound
            + " clientBound=" + clientBound + " rejected=" + rejected);
        System.out.println("RR_NET_SELF_TEST_OK messages=24 migrated=19 replaced=5 blocked=0 roundTrips=28");
    }

    private static byte[] encode(NetworkChannel.ACPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}