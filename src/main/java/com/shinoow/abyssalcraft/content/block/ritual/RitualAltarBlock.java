package com.shinoow.abyssalcraft.content.block.ritual;

import com.shinoow.abyssalcraft.content.item.book.NecronomiconItem;
import com.shinoow.abyssalcraft.platform.InteractiveBlockCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ritual altar block (owned by content/block/ritual): the centre of the ritual multiblock. Holds a
 * {@link RitualAltarBlockEntity} and, on a right click with the Necronomicon, performs the ritual matching
 * its ring pedestals' offerings (see the block entity). Interaction fork lives in
 * {@link InteractiveBlockCompat}; {@code EntityBlock} is vanilla-shared, so this block carries no
 * {@code //?}. No ticker -- the pilot ritual completes instantly on use.
 */
public class RitualAltarBlock extends InteractiveBlockCompat implements EntityBlock {

    public RitualAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAltarBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != RitualBlocks.RITUAL_ALTAR_BE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<RitualAltarBlockEntity>)
            RitualAltarBlockEntity::serverTick;
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof NecronomiconItem) {
                altar.tryRitual(level, pos, player);
            } else if (altar.isPerformingRitual()) {
                player.displayClientMessage(Component.translatable("message.abyssalcraft.ritual.busy"), true);
            } else if (altar.getCenterItem().isEmpty() && !held.isEmpty()) {
                altar.setCenterItem(held);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            } else if (!altar.getCenterItem().isEmpty()) {
                ItemStack returned = altar.takeCenterItem();
                if (!player.addItem(returned)) {
                    player.drop(returned, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean acceptsHeldItem() {
        return true;
    }
}
