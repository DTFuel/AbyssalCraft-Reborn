package com.shinoow.abyssalcraft.client.input;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import com.shinoow.abyssalcraft.content.item.ritual.GatekeeperStaffItem;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletItem;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.server.InterdimensionalCageMessage;
import com.shinoow.abyssalcraft.net.server.SpiritTabletMessage;
import com.shinoow.abyssalcraft.net.server.StaffModeMessage;
import com.shinoow.abyssalcraft.platform.ClientHooksCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.client.ClientInputContract;
import com.shinoow.abyssalcraft.system.spell.SpellUtils;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Client-only key dispatcher. Packets carry intent; every effect is revalidated on the server. */
public final class ClientInputHandler {

    private static final Map<ClientInputContract.Action, KeyMapping> MAPPINGS =
        new EnumMap<>(ClientInputContract.Action.class);
    private static boolean registered;

    private ClientInputHandler() {}

    public static void register() {
        if (registered) throw new IllegalStateException("AbyssalCraft client inputs registered twice");
        registered = true;
        for (ClientInputContract.Action action : ClientInputContract.ACTIONS) {
            KeyMapping mapping = new KeyMapping(action.translationKey(), InputConstants.Type.KEYSYM,
                action.defaultKey(), ClientInputContract.CATEGORY);
            MAPPINGS.put(action, mapping);
            ClientHooksCompat.queueKeyMapping(mapping);
        }
        ClientHooksCompat.queueClientTick(ClientInputHandler::tick);
    }

    private static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) return;
        consume(ClientInputContract.Action.STAFF_MODE, () -> useStaffMode(player));
        consume(ClientInputContract.Action.USE_CAGE, () -> useCage(player));
        consume(ClientInputContract.Action.TABLET_MODE, () -> cycleTabletModes(player));
        consume(ClientInputContract.Action.TABLET_FILTER, () -> openTabletFilter(player));
        consume(ClientInputContract.Action.TABLET_PATH, () -> clearTabletPath(player));
    }

    private static void consume(ClientInputContract.Action action, Runnable handler) {
        KeyMapping mapping = MAPPINGS.get(action);
        while (mapping != null && mapping.consumeClick()) handler.run();
    }

    private static void useStaffMode(Player player) {
        if (player.getMainHandItem().getItem() instanceof GatekeeperStaffItem
            || player.getOffhandItem().getItem() instanceof GatekeeperStaffItem) {
            ACNetwork.sendToServer(new StaffModeMessage());
        }
    }

    private static void useCage(Player player) {
        InteractionHand hand = emptyCageHand(player);
        if (hand == null) return;
        LivingEntity target = SpellUtils.rayTraceTarget(player, 3.0F);
        if (target != null) {
            ACNetwork.sendToServer(new InterdimensionalCageMessage(target.getId(), hand));
        }
    }

    private static InteractionHand emptyCageHand(Player player) {
        if (isEmptyCage(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        return isEmptyCage(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null;
    }

    private static boolean isEmptyCage(ItemStack stack) {
        return stack.getItem() instanceof InterdimensionalCageItem
            && !ItemDataCompat.copyData(stack).contains(InterdimensionalCageItem.ENTITY_KEY);
    }

    private static void cycleTabletModes(Player player) {
        int mainMode = nextTabletMode(player.getMainHandItem());
        int offMode = nextTabletMode(player.getOffhandItem());
        if (mainMode >= 0 || offMode >= 0) {
            ACNetwork.sendToServer(new SpiritTabletMessage(mainMode, offMode, false, false));
        }
    }

    private static int nextTabletMode(ItemStack stack) {
        return stack.getItem() instanceof SpiritTabletItem
            ? (SpiritTabletStorage.mode(stack) + 1) % 3 : -1;
    }

    private static void openTabletFilter(Player player) {
        if (hasTablet(player)) {
            ACNetwork.sendToServer(new SpiritTabletMessage(-1, -1, true, false));
        }
    }

    private static void clearTabletPath(Player player) {
        if (hasTablet(player)) {
            ACNetwork.sendToServer(new SpiritTabletMessage(-1, -1, false, true));
        }
    }

    private static boolean hasTablet(Player player) {
        return player.getMainHandItem().getItem() instanceof SpiritTabletItem
            || player.getOffhandItem().getItem() instanceof SpiritTabletItem;
    }
}