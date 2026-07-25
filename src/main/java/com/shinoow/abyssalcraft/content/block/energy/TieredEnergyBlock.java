package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.system.energy.EnergyTier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.system.energy.IEnergyContainer;

/** Shared tier metadata for the four five-variant PE network block families. */
public abstract class TieredEnergyBlock extends EnergyDropBlock implements EntityBlock {

    private final EnergyTier tier;

    protected TieredEnergyBlock(BlockBehaviour.Properties properties, EnergyTier tier) {
        super(properties);
        this.tier = tier;
    }

    public final EnergyTier tier() {
        return tier;
    }

    @Override
    protected InteractionResult onUse(BlockState state, net.minecraft.world.level.Level level,
                                      BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof IEnergyContainer container) {
            if (!handleEmptyHandUse(level, pos, player)) {
                player.displayClientMessage(Component.translatable("message.abyssalcraft.energy.status",
                    (int) container.getContainedEnergy(), container.getMaxEnergy()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected boolean handleEmptyHandUse(net.minecraft.world.level.Level level, BlockPos pos, Player player) {
        return false;
    }
}