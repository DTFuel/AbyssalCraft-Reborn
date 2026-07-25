package com.shinoow.abyssalcraft.content.block.energy;

import com.shinoow.abyssalcraft.content.blockentity.base.TickingBlockEntity;
import com.shinoow.abyssalcraft.registry.BaseBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
/*import net.minecraft.world.level.LevelReader;
*///?} else {
import net.minecraft.world.level.BlockGetter;
//?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import java.util.List;

/** Internal master block formed from monolith stone when a Place of Power activates. */
public final class PlaceOfPowerBaseBlock extends Block implements EntityBlock {

    public PlaceOfPowerBaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlaceOfPowerBaseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide || type != EnergyBlocks.PLACE_OF_POWER_BASE_BE.get()
            ? null
            : TickingBlockEntity.serverTicker();
    }

    //? if >=1.21 {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    *///?} else {
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
    //?}
        return new ItemStack(BaseBlocks.MONOLITH_STONE.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(BaseBlocks.MONOLITH_STONE.get()));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()
            && level.getBlockEntity(pos) instanceof PlaceOfPowerBaseBlockEntity base) {
            base.detachComponents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}