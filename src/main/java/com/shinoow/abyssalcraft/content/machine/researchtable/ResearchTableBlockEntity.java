package com.shinoow.abyssalcraft.content.machine.researchtable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.ACBlockEntity;

/**
 * Research Table block entity (owned by PC-8, Stage C2a).
 *
 * <p>Ports the 1.12.2 {@code TileEntityResearchTable}, which was a bare marker tile (the research
 * interface is knowledge-driven, not slot-driven). Here it is a {@link ACBlockEntity} that only serves
 * as the {@link MenuProvider} opening the (currently inventory-only) research screen. The actual
 * knowledge/research hook is deferred to Stage S-B; no persistent state yet.
 */
public class ResearchTableBlockEntity extends ACBlockEntity implements MenuProvider {

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ResearchTables.RESEARCH_TABLE_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.research_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new ResearchTableMenu(ResearchTables.RESEARCH_TABLE_MENU.get(), windowId, playerInv);
    }

    @Override
    protected void saveData(CompoundTag tag, HolderLookup.Provider registries) {
        // No persistent state yet (research/knowledge deferred to Stage S-B).
    }

    @Override
    protected void loadData(CompoundTag tag, HolderLookup.Provider registries) {
        // No persistent state yet (research/knowledge deferred to Stage S-B).
    }
}
