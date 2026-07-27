package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletItem;
import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletStorage;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server: apply spirit-tablet menu settings -- the two mode selectors plus the
 * open-filter / clear-path toggles (owned by PS-1). Serialization is faithful; applying the settings is
 * deferred until the spirit-tablet menu is ported.
 */
public class SpiritTabletMessage implements NetworkChannel.ACPacket {

    private final int mode1;
    private final int mode2;
    private final boolean openFilter;
    private final boolean clearPath;

    public SpiritTabletMessage(int mode1, int mode2, boolean openFilter, boolean clearPath) {
        this.mode1 = mode1;
        this.mode2 = mode2;
        this.openFilter = openFilter;
        this.clearPath = clearPath;
    }

    public SpiritTabletMessage(FriendlyByteBuf buf) {
        this.mode1 = buf.readVarInt();
        this.mode2 = buf.readVarInt();
        this.openFilter = buf.readBoolean();
        this.clearPath = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(mode1);
        buf.writeVarInt(mode2);
        buf.writeBoolean(openFilter);
        buf.writeBoolean(clearPath);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (clearPath) {
            clearRoute(player.getMainHandItem());
            clearRoute(player.getOffhandItem());
            player.inventoryMenu.broadcastChanges();
            return;
        }
        if (openFilter) {
            if (open(player, InteractionHand.MAIN_HAND)) return;
            open(player, InteractionHand.OFF_HAND);
            return;
        }
        setMode(player.getMainHandItem(), mode1);
        setMode(player.getOffhandItem(), mode2);
        player.inventoryMenu.broadcastChanges();
    }

    private static void clearRoute(ItemStack stack) {
        if (stack.getItem() instanceof SpiritTabletItem) SpiritTabletStorage.clearRoute(stack);
    }

    private static boolean open(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof SpiritTabletItem)) return false;
        SpiritTabletItem.openMenu(player, hand, stack);
        return true;
    }

    private static void setMode(ItemStack stack, int mode) {
        if (stack.getItem() instanceof SpiritTabletItem && mode >= 0 && mode <= 2) {
            SpiritTabletStorage.setMode(stack, mode);
        }
    }
}
