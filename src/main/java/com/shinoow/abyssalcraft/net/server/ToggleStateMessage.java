package com.shinoow.abyssalcraft.net.server;

import com.shinoow.abyssalcraft.platform.ItemTransferAttachmentCompat;
import com.shinoow.abyssalcraft.platform.NetworkChannel;
import com.shinoow.abyssalcraft.system.transfer.ItemTransferHost;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Client &rarr; server: toggle the running state of the item-transfer host block-entity at {@code pos}
 * (owned by PS-1). Targets the ported PC-4 {@code ItemTransferHost}; the handler wiring (fetch the BE,
 * flip running) is left to the transfer-host consumer task so this message stays free of a BE lookup.
 */
public class ToggleStateMessage implements NetworkChannel.ACPacket {

    private final BlockPos pos;

    public ToggleStateMessage(BlockPos pos) {
        this.pos = pos;
    }

    public ToggleStateMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    /** The targeted block position. */
    public BlockPos pos() {
        return pos;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkChannel.Context ctx) {
        if (!(ctx.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)
            || !level.hasChunkAt(pos) || player.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0D
            || !player.mayUseItemAt(pos, Direction.UP, player.getMainHandItem())) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        ItemTransferHost host = ItemTransferAttachmentCompat.get(blockEntity);
        if (host == null) return;
        host.setTransferRunning(!host.isTransferRunning());
        level.sendParticles(host.isTransferRunning() ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER,
            pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}
