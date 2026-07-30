package com.shinoow.abyssalcraft.content.block.ritual;

import java.util.List;
import java.util.OptionalInt;

import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Faithful material-ring conversion used by the Necronomicon create-altar action. */
public final class RitualAltarFormation {

    public static final List<BlockPos> PEDESTAL_OFFSETS = List.of(
        new BlockPos(-3, 0, 0), new BlockPos(0, 0, -3),
        new BlockPos(3, 0, 0), new BlockPos(0, 0, 3),
        new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2),
        new BlockPos(2, 0, 2), new BlockPos(2, 0, -2));

    private RitualAltarFormation() {}

    public static boolean canCreate(Level level, BlockPos center, int bookType) {
        int requiredBookType = materialBookType(level.getBlockState(center));
        if (requiredBookType < 0 || bookType < requiredBookType) return false;
        OptionalInt dimensionBookType = DimensionDataRegistry.instance().ritualBookType(level.dimension());
        if (dimensionBookType.isEmpty() || dimensionBookType.getAsInt() != requiredBookType) return false;
        ChunkPos chunk = new ChunkPos(center);
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            BlockPos pedestal = center.offset(offset);
            if (!chunk.equals(new ChunkPos(pedestal))
                || materialBookType(level.getBlockState(pedestal)) != requiredBookType
                || !hasClearPedestalArea(level, pedestal)) {
                return false;
            }
        }
        return true;
    }

    public static void create(Level level, BlockPos center) {
        if (level.isClientSide) return;
        level.setBlock(center, RitualBlocks.RITUAL_ALTAR.get().defaultBlockState(), 3);
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            level.setBlock(center.offset(offset), RitualBlocks.RITUAL_PEDESTAL.get().defaultBlockState(), 3);
        }
    }

    public static int materialBookType(BlockState state) {
        if (state.is(Blocks.COBBLESTONE) || state.is(BaseBlocks.DARKSTONE_COBBLESTONE.get())) return 0;
        if (state.is(BaseBlocks.ABYSSAL_COBBLESTONE.get())
            || state.is(BaseBlocks.CORALIUM_COBBLESTONE.get())) return 1;
        if (state.is(BaseBlocks.DREADSTONE_COBBLESTONE.get())
            || state.is(BaseBlocks.ELYSIAN_COBBLESTONE.get())) return 2;
        if (state.is(BaseBlocks.ETHAXIUM_BRICKS.get())
            || state.is(BaseBlocks.DARK_ETHAXIUM_BRICK.get())) return 3;
        return -1;
    }

    private static boolean hasClearPedestalArea(Level level, BlockPos pedestal) {
        for (BlockPos nearby : BlockPos.betweenClosed(pedestal.south().west(), pedestal.north().east())) {
            if (!nearby.equals(pedestal)
                && level.getBlockState(nearby).isCollisionShapeFullBlock(level, nearby)) {
                return false;
            }
        }
        return true;
    }
}