package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.content.block.demon.DemonBlocks;
import com.shinoow.abyssalcraft.platform.NetworkChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Client &rarr; server: extinguish a mimic-fire block at {@code pos} (owned by PS-1). Serialization is
 * faithful; the effect (extinguish + sound) is deferred until the mimic-fire block is ported.
 */
public class FireMessage implements NetworkChannel.ACPacket {

    private final BlockPos pos;

    public FireMessage(BlockPos pos) {
        this.pos = pos;
    }

    public FireMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player) || !player.level().hasChunkAt(pos)
            || player.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0D
            || !player.mayUseItemAt(pos, Direction.UP, player.getMainHandItem())
            || !player.level().getBlockState(pos).is(DemonBlocks.MIMIC_FIRE.get())) return;
        player.level().playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
            0.5F, 2.6F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.8F);
        player.level().removeBlock(pos, false);
    }
}
