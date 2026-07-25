package com.shinoow.abyssalcraft.content.block.shoggoth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.config.ACConfig;
import com.shinoow.abyssalcraft.content.block.deco.DecoBlocks;
import com.shinoow.abyssalcraft.content.entity.shoggoth.AbstractShoggoth;
import com.shinoow.abyssalcraft.platform.ArmorDurabilityCompat;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.world.ACDimensions;

/** Layered Shoggoth ooze that slows intruders and recedes under strong light. */
public final class ShoggothOozeBlock extends SnowLayerBlock {

    public ShoggothOozeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractShoggoth) return;
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4D, 1.0D, 0.4D));
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            if (living.tickCount % 60 == 0) {
                ArmorDurabilityCompat.damage(living.getItemBySlot(EquipmentSlot.LEGS), 1, living, EquipmentSlot.LEGS);
            }
            if (living.tickCount % 40 == 0) {
                ArmorDurabilityCompat.damage(living.getItemBySlot(EquipmentSlot.FEET), 1, living, EquipmentSlot.FEET);
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!ACConfig.oozeExpire.get() || random.nextInt(10) != 0
                || level.getMaxLocalRawBrightness(pos.above()) < 13) return;
        int layers = state.getValue(LAYERS);
        if (layers == 8) {
            level.setBlockAndUpdate(pos, replacementFor(level));
        } else if (layers > 1) {
            level.setBlockAndUpdate(pos, state.setValue(LAYERS, layers - 1));
        } else {
            level.removeBlock(pos, false);
        }
    }

    private static BlockState replacementFor(ServerLevel level) {
        if (level.dimension() == ACDimensions.ABYSSAL_WASTELAND) {
            return DecoBlocks.ABYSSAL_SAND.get().defaultBlockState();
        }
        if (level.dimension() == ACDimensions.DREADLANDS) {
            return DecoBlocks.DREADLANDS_DIRT.get().defaultBlockState();
        }
        if (level.dimension() == ACDimensions.OMOTHOL) {
            return BaseBlocks.OMOTHOL_STONE.get().defaultBlockState();
        }
        if (level.dimension() == ACDimensions.DARK_REALM) {
            return BaseBlocks.DARKSTONE.get().defaultBlockState();
        }
        if (level.dimension() == Level.NETHER) return Blocks.NETHERRACK.defaultBlockState();
        if (level.dimension() == Level.END) return Blocks.END_STONE.defaultBlockState();
        return Blocks.DIRT.defaultBlockState();
    }
}