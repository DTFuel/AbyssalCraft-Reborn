package com.shinoow.abyssalcraft.client.network;

import java.util.BitSet;

import com.shinoow.abyssalcraft.content.entity.ghoul.ShadowGhoul;
import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.content.item.ritual.GatekeeperStaffItem;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.content.item.scroll.ScrollItem;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletItem;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.RRNetValidation;
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

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Temporary client half of the real RR-NET matrix. */
public final class RRNetClientValidation {

    private static final BitSet SENT = new BitSet(23);
    private static final BitSet HANDLED = new BitSet(23);
    private static int cooldown;
    private static boolean complete;
    private static boolean announced;
    private static boolean tickSeen;

    private RRNetClientValidation() {}

    public static boolean markTickSeen() {
        if (tickSeen) return false;
        tickSeen = true;
        return true;
    }

    public static void recordHandled(int id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) throw new IllegalStateException("RR-NET client handler off-thread");
        HANDLED.set(id);
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (complete || minecraft.player == null || minecraft.level == null
            || !"RRNetClient".equals(minecraft.player.getGameProfile().getName())) return;
        if (!announced) {
            announced = true;
            System.out.println("RR_NET_CLIENT_READY player=RRNetClient");
        }
        if (cooldown > 0 && cooldown % 100 == 0) {
            System.out.println("RR_NET_CLIENT_STAGE held="
                + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(minecraft.player.getMainHandItem().getItem())
                + " sent=" + SENT + " handled=" + HANDLED);
        }
        if (cooldown++ < 20) return;
        ItemStack held = minecraft.player.getMainHandItem();
        if (held.is(Items.STICK) && !SENT.get(0)) {
            send(0, new FireMessage(RRNetValidation.FIRE));
            send(2, new ToggleStateMessage(RRNetValidation.HOST));
            cooldown = 0;
        } else if (held.getItem() instanceof SpiritTabletItem && !SENT.get(1)
                && minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
            send(1, new UpdateModeMessage(0, 1));
            send(5, new SpiritTabletMessage(2, -1, false, false));
            cooldown = 0;
        } else if (held.getItem() instanceof StaffOfRendingItem && !SENT.get(3)) {
            LivingEntity target = nearest(ShadowGhoul.class);
            if (target != null) {
                send(3, new StaffOfRendingMessage(target.getId(), InteractionHand.MAIN_HAND));
                cooldown = 0;
            }
        } else if (held.getItem() instanceof GatekeeperStaffItem && !SENT.get(4)) {
            send(4, new StaffModeMessage());
            cooldown = 0;
        } else if (held.getItem() instanceof NecronomiconItem && !SENT.get(7)) {
            send(6, new PrepareSyncMessage(minecraft.player.getUUID()));
            send(7, new OpenSpellbookMessage());
            cooldown = 0;
        } else if (held.getItem() instanceof ScrollItem && !SENT.get(8) && cooldown >= 55) {
            LivingEntity target = nearest(Zombie.class);
            if (target != null) {
                send(8, new MobSpellMessage(target.getId(), "spoofed", 4));
                cooldown = 0;
            }
        } else if (held.getItem() instanceof InterdimensionalCageItem && !SENT.get(9)) {
            LivingEntity target = nearest(Pig.class);
            if (target != null) {
                send(9, new InterdimensionalCageMessage(target.getId(), InteractionHand.MAIN_HAND));
                cooldown = 0;
            }
        } else if (held.is(Items.DIAMOND) && !SENT.get(10)) {
            send(10, new TransferStackMessage(0, new ItemStack(Items.DIAMOND, 64)));
            cooldown = 0;
        }
        if (containsRange(SENT, 0, 11) && containsRange(HANDLED, 11, 23)) {
            complete = true;
            System.out.println("RR_NET_CLIENT_MATRIX_OK ids=23 c2s=11 s2c=12 threads=main");
        }
    }

    private static void send(int id, com.shinoow.abyssalcraft.platform.NetworkChannel.ACPacket packet) {
        SENT.set(id);
        ACNetwork.sendToServer(packet);
    }

    private static <T extends LivingEntity> T nearest(Class<T> type) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level.getEntitiesOfClass(type, minecraft.player.getBoundingBox().inflate(32.0D)).stream()
            .filter(LivingEntity::isAlive)
            .min(java.util.Comparator.comparingDouble(minecraft.player::distanceToSqr)).orElse(null);
    }

    private static boolean containsRange(BitSet bits, int from, int to) {
        for (int id = from; id < to; id++) if (!bits.get(id)) return false;
        return true;
    }
}