package com.shinoow.abyssalcraft.validation.server;

import com.shinoow.abyssalcraft.content.block.energy.DeityStatueBlockEntity;
import com.shinoow.abyssalcraft.content.block.energy.DeityStatueBlock;
import com.shinoow.abyssalcraft.content.block.energy.EnergyBlocks;
import com.shinoow.abyssalcraft.content.block.energy.EnergyPedestalBlockEntity;
import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.system.energy.AmplifierType;
import com.shinoow.abyssalcraft.system.energy.DeityType;
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
        level.setBlockAndUpdate(pedestalPos, EnergyBlocks.ENERGY_PEDESTALS.get(0).get().defaultBlockState());

        try {
            require(level.canSeeSky(statuePos.above()), "functional statue is not open to the sky");
            var pedestalBlockEntity = level.getBlockEntity(pedestalPos);
            require(pedestalBlockEntity instanceof EnergyPedestalBlockEntity,
                "PE pedestal block entity is missing");
            EnergyPedestalBlockEntity pedestal = (EnergyPedestalBlockEntity) pedestalBlockEntity;
            int totalAttempts = 0;
            for (var statueSupplier : EnergyBlocks.DEITY_STATUES) {
                level.setBlockAndUpdate(statuePos, statueSupplier.get().defaultBlockState());
                var statueBlockEntity = level.getBlockEntity(statuePos);
                require(statueBlockEntity instanceof DeityStatueBlockEntity,
                    "functional statue block entity is missing");
                DeityStatueBlockEntity statue = (DeityStatueBlockEntity) statueBlockEntity;
                require(statueSupplier.get() instanceof DeityStatueBlock statueBlock
                    && statue.getDeity() == statueBlock.deity(),
                    "functional statue block entity has the wrong deity");
                DeityType deity = statue.getDeity();
                require(statue.getEnergyQuanta() == 5.0F,
                    deity + " statue base PE quanta changed");
                require(statue.setActive(AmplifierType.POWER, deity)
                    && statue.getAmplifier(AmplifierType.POWER) == 2.5F
                    && statue.getEnergyQuanta() == 25.0F,
                    deity + " statue did not apply its matching deity amplifier");
                statue.clearActive();
                DeityType otherDeity = deity == DeityType.CTHULHU ? DeityType.HASTUR : DeityType.CTHULHU;
                require(statue.setActive(AmplifierType.POWER, otherDeity)
                    && statue.getAmplifier(AmplifierType.POWER) == 1.5F
                    && statue.getEnergyQuanta() == 15.0F,
                    deity + " statue did not apply its mismatched deity amplifier");
                statue.clearActive();

                pedestal.setEnergy(0.0F);
                pedestal.setItem(0, ItemStack.EMPTY);
                PEUtils.locateCollectors(level, statuePos, statue);
                require(statue.getEnergyCollectors().contains(pedestalPos),
                    deity + " statue did not discover the PE pedestal at the legacy three-block radius");
                int attempts = 0;
                while (pedestal.getContainedEnergy() == 0.0F && attempts++ < 2_000) {
                    PEUtils.transferToCollectors(level, statue);
                }
                totalAttempts += attempts;
                require(pedestal.getContainedEnergy() > 0.0F,
                    deity + " statue did not transfer PE to its discovered pedestal");

                ItemStack heldBook = new ItemStack(BookItems.NECRONOMICON.get());
                require(PEUtils.transferToItem(statue, heldBook, 2) > 0.0F,
                    deity + " statue did not directly charge a held Necronomicon");
            }

            ItemStack pedestalBook = new ItemStack(BookItems.NECRONOMICON.get());
            pedestal.setItem(0, pedestalBook);
            pedestal.serverTick();
            IEnergyContainerItem pedestalEnergy = (IEnergyContainerItem) pedestalBook.getItem();
            require(pedestalEnergy.getContainedEnergy(pedestalBook) > 0.0F,
                "PE pedestal did not charge its Necronomicon");
            System.out.printf("RR_DEITY_STATUE_PE_OK statues=%d collectorAttempts=%d pedestalPE=%.1f%n",
                EnergyBlocks.DEITY_STATUES.size(), totalAttempts,
                pedestalEnergy.getContainedEnergy(pedestalBook));
        } finally {
            level.setBlockAndUpdate(statuePos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pedestalPos, Blocks.AIR.defaultBlockState());
        }
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("RR_DEITY_STATUE_PE_FAIL " + reason);
    }
}