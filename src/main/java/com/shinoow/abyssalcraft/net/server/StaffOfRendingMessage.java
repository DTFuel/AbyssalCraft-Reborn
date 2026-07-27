package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Client &rarr; server: use the Staff of Rending held in {@code hand} on the entity with network
 * {@code id} (owned by PS-1). Serialization is faithful; the rending effect is deferred until the
 * rending API + staff item are ported.
 */
public class StaffOfRendingMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final InteractionHand hand;

    public StaffOfRendingMessage(int id, InteractionHand hand) {
        this.id = id;
        this.hand = hand;
    }

    public StaffOfRendingMessage(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.hand = buf.readVarInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeVarInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof StaffOfRendingItem staff)
            || !(player.level().getEntity(id) instanceof LivingEntity target)) return;
        if (staff.rend(player, target, stack) && staff.multiRendLevel(stack) > 0) {
            for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(target.blockPosition()).inflate(3.0D),
                    entity -> entity != target && entity != player)) {
                staff.rend(player, nearby, stack);
            }
        }
    }
}
