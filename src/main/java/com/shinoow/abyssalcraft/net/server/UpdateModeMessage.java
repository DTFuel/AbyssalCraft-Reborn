package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.transfer.SpiritTabletMenu;
import com.shinoow.abyssalcraft.content.machine.statetransformer.StateTransformerMenu;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client &rarr; server: set the mode of the open state-transformer ({@code container==0}) or spirit
 * tablet ({@code container==1}) menu (owned by PS-1). Serialization is faithful; the effect is deferred
 * until those menus are ported.
 */
public class UpdateModeMessage implements NetworkChannel.ACPacket {

    private final int mode;
    private final int container;

    public UpdateModeMessage(int mode, int container) {
        this.mode = mode;
        this.container = container;
    }

    public UpdateModeMessage(FriendlyByteBuf buf) {
        this.mode = buf.readVarInt();
        this.container = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(mode);
        buf.writeVarInt(container);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player) || mode < 0 || mode > 1) return;
        if (container == 0 && player.containerMenu instanceof StateTransformerMenu transformerMenu) {
            if (transformerMenu.clickMenuButton(player, mode)) transformerMenu.broadcastChanges();
        } else if (container == 1 && player.containerMenu instanceof SpiritTabletMenu tabletMenu) {
            if (tabletMenu.clickMenuButton(player, mode)) tabletMenu.broadcastChanges();
        }
    }
}
