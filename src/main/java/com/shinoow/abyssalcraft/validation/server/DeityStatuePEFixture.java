package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.content.block.energy.DeityStatueBlockEntity;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyPedestalBlockEntity;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.energy.PEUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Real-level regression for direct and PE-pedestal Necronomicon charging. */
public final class DeityStatuePEFixture {

    private DeityStatuePEFixture() {}

    public static void run(ServerLevel level) {
        int x = 320;
        int z = 320;
        int y = level.getMaxBuildHeight() - 2;
        BlockPos statuePos = new BlockPos(x, y, z);
        BlockPos pedestalPos = statuePos.offset(3, 0, 0);
        level.setBlockAndUpdate(statuePos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pedestalPos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(statuePos, EnergyBlocks.DEITY_STATUES.get(0).get().defaultBlockState());
        level.setBlockAndUpdate(pedestalPos, EnergyBlocks.ENERGY_PEDESTALS.get(0).get().defaultBlockState());

        try {
            require(level.canSeeSky(statuePos.above()), "functional statue is not open to the sky");
            var statueBlockEntity = level.getBlockEntity(statuePos);
            var pedestalBlockEntity = level.getBlockEntity(pedestalPos);
            require(statueBlockEntity instanceof DeityStatueBlockEntity,
                "functional statue block entity is missing");
            require(pedestalBlockEntity instanceof EnergyPedestalBlockEntity,
                "PE pedestal block entity is missing");
            DeityStatueBlockEntity statue = (DeityStatueBlockEntity) statueBlockEntity;
            EnergyPedestalBlockEntity pedestal = (EnergyPedestalBlockEntity) pedestalBlockEntity;

            PEUtils.locateCollectors(level, statuePos, statue);
            require(statue.getEnergyCollectors().contains(pedestalPos),
                "statue did not discover the PE pedestal at the legacy three-block radius");
            int attempts = 0;
            while (pedestal.getContainedEnergy() == 0.0F && attempts++ < 2_000) {
                PEUtils.transferToCollectors(level, statue);
            }
            require(pedestal.getContainedEnergy() > 0.0F,
                "statue did not transfer PE to its discovered pedestal");

            ItemStack pedestalBook = new ItemStack(BookItems.NECRONOMICON.get());
            pedestal.setItem(0, pedestalBook);
            pedestal.serverTick();
            IEnergyContainerItem pedestalEnergy = (IEnergyContainerItem) pedestalBook.getItem();
            require(pedestalEnergy.getContainedEnergy(pedestalBook) > 0.0F,
                "PE pedestal did not charge its Necronomicon");

            ItemStack heldBook = new ItemStack(BookItems.NECRONOMICON.get());
            require(PEUtils.transferToItem(statue, heldBook, 2) > 0.0F,
                "functional statue did not directly charge a held Necronomicon");
            System.out.printf("RR_DEITY_STATUE_PE_OK collectorAttempts=%d pedestalPE=%.1f heldPE=%.1f%n",
                attempts, pedestalEnergy.getContainedEnergy(pedestalBook),
                ((IEnergyContainerItem) heldBook.getItem()).getContainedEnergy(heldBook));
        } finally {
            level.setBlockAndUpdate(statuePos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pedestalPos, Blocks.AIR.defaultBlockState());
        }
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_DEITY_STATUE_PE_FAIL " + reason);
    }
}