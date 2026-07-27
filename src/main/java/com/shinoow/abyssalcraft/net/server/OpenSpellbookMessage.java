package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.content.menu.spellbook.SpellbookMenu;
import com.shinoow.abyssalcraft.platform.MenuCompat;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server: request opening the spellbook GUI for the sender (owned by PS-1). Carries no
 * payload. Opening the menu is deferred until the spellbook GUI is ported.
 */
public class OpenSpellbookMessage implements NetworkChannel.ACPacket {

    public OpenSpellbookMessage() {}

    public OpenSpellbookMessage(FriendlyByteBuf buf) {}

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        InteractionHand hand = bookHand(player);
        if (hand == null || player.containerMenu instanceof SpellbookMenu) return;
        ItemStack book = player.getItemInHand(hand);
        SimpleMenuProvider provider = new SimpleMenuProvider(
            (windowId, inventory, ignored) -> new SpellbookMenu(windowId, inventory, hand, book),
            Component.translatable("container.abyssalcraft.spellbook"));
        MenuCompat.open(player, provider, buffer -> buffer.writeBoolean(hand == InteractionHand.MAIN_HAND));
    }

    private static InteractionHand bookHand(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof NecronomiconItem) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().getItem() instanceof NecronomiconItem) return InteractionHand.OFF_HAND;
        return null;
    }
}
