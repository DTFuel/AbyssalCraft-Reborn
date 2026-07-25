package com.shinoow.abyssalcraft.content.block.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FlowingFluid;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.entity.anti.AntiEntity;
import com.shinoow.abyssalcraft.platform.MobEffectCompat;
import com.shinoow.abyssalcraft.platform.LiquidCoraliumCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.effect.ACEffects;

/** Liquid Antimatter contact effects used by naturally generated lakes. */
public final class LiquidAntimatterBlock extends LiquidBlock {

    public LiquidAntimatterBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (entity instanceof ItemEntity && ACConfig.antiItemDisintegration.get()) {
            entity.discard();
            return;
        }
        if (entity instanceof LivingEntity living && !(living instanceof AntiEntity)) {
            living.addEffect(MobEffectCompat.vanillaEffect(MobEffects.MOVEMENT_SLOWDOWN, 400, 0));
            living.addEffect(MobEffectCompat.vanillaEffect(MobEffects.BLINDNESS, 400, 0));
            living.addEffect(MobEffectCompat.vanillaEffect(MobEffects.WEAKNESS, 400, 0));
            living.addEffect(MobEffectCompat.vanillaEffect(MobEffects.HUNGER, 400, 0));
            living.addEffect(MobEffectCompat.vanillaEffect(MobEffects.NIGHT_VISION, 400, 0));
            living.addEffect(MobEffectCompat.effectInstance(ACEffects.ANTIMATTER, 200, 0));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (!level.isClientSide && solidifyNeighbors(level, pos)) return;
        super.onPlace(state, level, pos, oldState, moved);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level actualLevel && !actualLevel.isClientSide && solidify(actualLevel, neighborPos)) {
            return state;
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    private static boolean solidifyNeighbors(Level level, BlockPos pos) {
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            changed |= solidify(level, pos.relative(direction));
        }
        return changed;
    }

    private static boolean solidify(Level level, BlockPos pos) {
        var fluid = level.getFluidState(pos).getType();
        if (fluid == LiquidCoraliumCompat.SOURCE.get() || fluid == LiquidCoraliumCompat.FLOWING.get()) {
            level.setBlockAndUpdate(pos, BaseBlocks.CORALIUM_STONE.get().defaultBlockState());
            return true;
        }
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        return false;
    }
}