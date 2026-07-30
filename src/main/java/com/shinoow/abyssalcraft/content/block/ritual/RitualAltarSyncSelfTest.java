package com.shinoow.abyssalcraft.content.block.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RitualAltarSyncSelfTest {

    private RitualAltarSyncSelfTest() {}

    public static void run() {
        TestAltar altar = new TestAltar();
        altar.setCenterItem(new ItemStack(Items.DIAMOND));
        ItemStack removed = altar.takeCenterItem();
        altar.setRitualCenter(new ItemStack(Items.EMERALD));
        if (!removed.is(Items.DIAMOND) || !altar.getCenterItem().is(Items.EMERALD)
            || altar.renderUpdates != 3) {
            throw new IllegalStateException("Ritual Altar center-item changes did not refresh the client renderer");
        }
        System.out.println("RR_RITUAL_ALTAR_SYNC_SELF_TEST_OK updates=3");
    }

    private static final class TestAltar extends RitualAltarBlockEntity {
        private int renderUpdates;

        private TestAltar() {
            super(BlockPos.ZERO, RitualBlocks.RITUAL_ALTAR.get().defaultBlockState());
        }

        @Override
        protected void markUpdated() {
            renderUpdates++;
        }
    }
}