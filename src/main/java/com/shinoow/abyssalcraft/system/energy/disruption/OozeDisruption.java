package com.shinoow.abyssalcraft.system.energy.disruption;

import java.util.List;

import com.shinoow.abyssalcraft.content.block.shoggoth.ShoggothBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Places a two-to-five block radius of layered Shoggoth ooze. */
public final class OozeDisruption extends Disruption {

    public OozeDisruption() {
        super("ooze", null);
    }

    @Override
    public void disrupt(Level level, BlockPos pos, List<Player> players) {
        if (!(level instanceof ServerLevel server)) return;
        int radius = server.random.nextInt(4) + 2;
        for (BlockPos target : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius),
                pos.offset(radius, radius, radius))) {
            int dx = target.getX() - pos.getX();
            int dy = target.getY() - pos.getY();
            int dz = target.getZ() - pos.getZ();
            if (!((Math.abs(dx) < 2 && Math.abs(dy) < 2 && Math.abs(dz) < 2 && server.random.nextBoolean())
                    || server.random.nextInt(3) == 0)) continue;
            BlockState current = server.getBlockState(target);
            if (current.is(ShoggothBlocks.SHOGGOTH_OOZE.get())) {
                int layers = current.getValue(SnowLayerBlock.LAYERS);
                if (layers < 8) server.setBlockAndUpdate(target, current.setValue(SnowLayerBlock.LAYERS, layers + 1));
                continue;
            }
            BlockState ooze = ShoggothBlocks.SHOGGOTH_OOZE.get().defaultBlockState()
                .setValue(SnowLayerBlock.LAYERS, 1 + server.random.nextInt(5));
            if (current.getFluidState().isEmpty() && current.canBeReplaced() && ooze.canSurvive(server, target)) {
                server.setBlockAndUpdate(target, ooze);
            }
        }
    }
}