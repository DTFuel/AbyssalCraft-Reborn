package com.shinoow.abyssalcraft.world.feature;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/**
 * Bare dead tree / snag, preserving the old trunk, radial branches and four roots.
 */
public class DeadTreeFeature extends Feature<BlockStateConfiguration> {

    public DeadTreeFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BlockState log = context.config().state;
        if (origin.getY() <= 54 || origin.getY() >= level.getMaxBuildHeight() - 10) return false;
        BlockState soil = level.getBlockState(origin.below());
        if (!soil.is(com.shinoow.abyssalcraft.content.block.deco.DecoBlocks.ABYSSAL_SAND.get())
                && !soil.is(com.shinoow.abyssalcraft.content.block.deco.DecoBlocks.FUSED_ABYSSAL_SAND.get())) {
            return false;
        }

        int height = 7 + random.nextInt(3);
        int crownVariance = random.nextInt(3);
        int branches = 4 + random.nextInt(4);
        BlockState vertical = withAxis(log, Direction.Axis.Y);
        for (int y = 0; y < height; y++) {
            level.setBlock(origin.above(y), vertical, 2);
        }
        level.setBlock(origin.below(), com.shinoow.abyssalcraft.content.block.deco.DecoBlocks.ABYSSAL_SAND.get().defaultBlockState(), 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int rootHeight = random.nextInt(3);
            for (int y = 0; y <= rootHeight; y++) {
                BlockPos root = origin.relative(direction).above(y);
                level.setBlock(root, withAxis(log, direction.getAxis()), 2);
            }
        }

        int angle = random.nextInt(Math.max(1, 360 / branches));
        for (int branch = 0; branch < branches; branch++) {
            double distance = 0.0D;
            double branchHeight = height - random.nextFloat() * crownVariance - 2.0D;
            angle += 360 / branches;
            double xDirection = cos(angle * PI / 180.0D);
            double zDirection = sin(angle * PI / 180.0D);
            Direction.Axis axis = Math.abs(xDirection) >= Math.abs(zDirection) ? Direction.Axis.X : Direction.Axis.Z;
            while (distance < 4.0D) {
                distance += 1.0D;
                branchHeight += 0.5D;
                BlockPos branchPos = origin.offset((int) (distance * xDirection), (int) branchHeight,
                    (int) (distance * zDirection));
                level.setBlock(branchPos, withAxis(log, axis), 2);
            }
        }
        return true;
    }

    private static BlockState withAxis(BlockState state, Direction.Axis axis) {
        return state.hasProperty(BlockStateProperties.AXIS)
            ? state.setValue(RotatedPillarBlock.AXIS, axis)
            : state;
    }
}
