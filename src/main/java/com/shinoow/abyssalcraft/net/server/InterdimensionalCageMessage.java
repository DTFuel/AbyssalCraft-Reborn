package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.config.ComplexConfig;
import com.shinoow.abyssalcraft.content.entity.boss.ACBossMob;
import com.shinoow.abyssalcraft.content.entity.boss.EliteMob;
import com.shinoow.abyssalcraft.content.item.ritual.InterdimensionalCageItem;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client &rarr; server: activate the Interdimensional Cage held in {@code hand} on the entity with
 * network {@code id} (owned by PS-1). Serialization is faithful; the capture effect is deferred until
 * the energy-container API + cage item are ported.
 */
public class InterdimensionalCageMessage implements NetworkChannel.ACPacket {

    private final int id;
    private final InteractionHand hand;

    public InterdimensionalCageMessage(int id, InteractionHand hand) {
        this.id = id;
        this.hand = hand;
    }

    public InterdimensionalCageMessage(FriendlyByteBuf buf) {
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
        if (!(stack.getItem() instanceof InterdimensionalCageItem cage)
            || ItemDataCompat.copyData(stack).contains(InterdimensionalCageItem.ENTITY_KEY)
            || !(player.level().getEntity(id) instanceof LivingEntity target)
            || target instanceof Player || !target.isAlive() || target.level() != player.level()
            || target instanceof ACBossMob || target instanceof EliteMob
            || target instanceof EnderDragon || target instanceof WitherBoss
            || target.isPassenger() || target.isVehicle()
            || player.distanceToSqr(target) > 16.0D || !player.hasLineOfSight(target)
            || ComplexConfig.interdimensionalCageBlacklist().contains(
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()))) return;
        float cost = InterdimensionalCageItem.energyCost(target);
        if (cage.getContainedEnergy(stack) < cost) return;
        CompoundTag entityTag = target.saveWithoutId(new CompoundTag());
        entityTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
        if (cage.consumeEnergy(stack, cost) + 0.001F < cost) return;
        CompoundTag root = ItemDataCompat.copyData(stack);
        root.put(InterdimensionalCageItem.ENTITY_KEY, entityTag);
        root.putString(InterdimensionalCageItem.ENTITY_NAME_KEY, target.getName().getString());
        ItemDataCompat.setData(stack, root);
        target.discard();
    }
}
