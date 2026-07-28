package com.shinoow.abyssalcraft.content.block.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RitualPedestalSyncSelfTest {

    private RitualPedestalSyncSelfTest() {}

    public static void run() {
        TestPedestal pedestal = new TestPedestal();
        pedestal.setOffering(new ItemStack(Items.DIAMOND));
        pedestal.consumeOffering();
        if (!pedestal.getOffering().isEmpty() || pedestal.renderUpdates != 2) {
            throw new IllegalStateException("Ritual Pedestal offering changes did not refresh the client renderer");
        }
        System.out.println("RR_RITUAL_PEDESTAL_SYNC_SELF_TEST_OK updates=2");
    }

    private static final class TestPedestal extends RitualPedestalBlockEntity {
        private int renderUpdates;

        private TestPedestal() {
            super(BlockPos.ZERO, RitualBlocks.RITUAL_PEDESTAL.get().defaultBlockState());
        }

        @Override
        protected void markUpdated() {
            renderUpdates++;
        }
    }
}