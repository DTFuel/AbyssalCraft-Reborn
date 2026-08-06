package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.net.ACNetwork;
import com.shinoow.abyssalcraft.net.client.LineEffectMessage;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.system.enchant.EnchantmentEffects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client &rarr; server: use the Staff of Rending held in {@code hand} on the entity with network
 * {@code id}. A successful primary rend triggers the production Multi-Rend radius consumer.
 */
public class StaffOfRendingMessage implements NetworkChannel.ACPacket {

    private static final int LINE_DURATION_TICKS = 8;
    private static final double LINE_VISIBILITY_RANGE_SQR = 128.0D * 128.0D;

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
        double radius = EnchantmentEffects.multiRendRadius(staff.multiRendLevel(stack));
        if (!staff.rend(player, target, stack)) return;
        broadcastLine(player, target, staff.lineStartColor(), staff.lineEndColor());
        if (radius <= 0.0D) return;
        for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class,
            new AABB(target.blockPosition()).inflate(radius),
                entity -> entity != target && entity != player)) {
            if (staff.rend(player, nearby, stack)) {
                broadcastLine(player, nearby, staff.lineStartColor(), staff.lineEndColor());
            }
        }
    }

    private static void broadcastLine(ServerPlayer player, LivingEntity target,
                                      int startColor, int endColor) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(0.4D));
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        LineEffectMessage message = new LineEffectMessage(
            start, end, startColor, endColor, LINE_DURATION_TICKS);
        for (ServerPlayer observer : player.serverLevel().players()) {
            if (observer.distanceToSqr(player) <= LINE_VISIBILITY_RANGE_SQR) {
                ACNetwork.sendToPlayer(observer, message);
            }
        }
    }
}
