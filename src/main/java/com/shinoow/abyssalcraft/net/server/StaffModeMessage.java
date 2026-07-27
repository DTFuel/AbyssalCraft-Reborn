package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.ritual.GatekeeperStaffItem;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server: toggle the legacy two-state mode field on Gatekeeper Staffs held by the sender.
 * The packet carries no client-selected state.
 */
public class StaffModeMessage implements NetworkChannel.ACPacket {

    public StaffModeMessage() {}

    public StaffModeMessage(FriendlyByteBuf buf) {}

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        boolean changed = toggle(player.getMainHandItem(), player);
        if (!changed) toggle(player.getOffhandItem(), player);
    }

    private static boolean toggle(ItemStack stack, ServerPlayer player) {
        if (!(stack.getItem() instanceof GatekeeperStaffItem staff)) return false;
        int mode = staff.toggleMode(stack);
        player.displayClientMessage(Component.translatable(
            "message.abyssalcraft.gatekeeper_staff.mode", mode), true);
        return true;
    }
}
