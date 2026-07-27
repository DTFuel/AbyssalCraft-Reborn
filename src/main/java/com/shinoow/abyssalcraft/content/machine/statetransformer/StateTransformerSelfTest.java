package com.shinoow.abyssalcraft.content.machine.statetransformer;

import java.util.Arrays;

import com.shinoow.abyssalcraft.content.item.bag.CrystalBagItems;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class StateTransformerSelfTest {

    private StateTransformerSelfTest() {}

    public static void run(HolderLookup.Provider registries) {
        TestTransformer transformer = seeded();
        for (int tick = 0; tick < 73; tick++) transformer.tick(registries);
        require(transformer.processingTime() == 73, "State Transformer did not advance to tick 73");

        CompoundTag saved = transformer.write(registries);
        TestTransformer restored = new TestTransformer();
        restored.read(saved, registries);
        require(restored.processingTime() == 73 && restored.getItem(1).getCount() == 4,
            "State Transformer restart lost progress or input");
        for (int tick = 73; tick < StateTransformerBlockEntity.PROCESS_DURATION; tick++) {
            restored.tick(registries);
        }

        ItemStack filledTablet = restored.getItem(StateTransformerBlockEntity.SLOT_TABLET);
        require(StoneTabletStorage.hasInventory(filledTablet), "insert did not fill the Stone Tablet");
        require(StoneTabletStorage.storedStacks(filledTablet) == 3,
            "insert did not preserve all occupied slots");
        require(StoneTabletStorage.potentialEnergy(filledTablet) == 132.0F,
            "Stone Tablet PE formula changed");
        require(restored.processingTime() == 0 && contentsEmpty(restored),
            "insert did not atomically clear machine content");

        require(restored.setMode(StateTransformerBlockEntity.MODE_EXTRACT), "extract mode was rejected");
        for (int tick = 0; tick < StateTransformerBlockEntity.PROCESS_DURATION; tick++) {
            restored.tick(registries);
        }
        require(!StoneTabletStorage.hasInventory(restored.getItem(StateTransformerBlockEntity.SLOT_TABLET)),
            "extract did not clear the Stone Tablet payload");
        require(restored.getItem(1).is(Items.COBBLESTONE) && restored.getItem(1).getCount() == 4
            && restored.getItem(2).is(Items.EGG) && restored.getItem(2).getCount() == 16
            && restored.getItem(49).is(Items.DIAMOND_SWORD),
            "49-slot round trip changed slot identity or counts");

        TestTransformer nestedInventory = new TestTransformer();
        nestedInventory.setItem(0, new ItemStack(TabletItems.STONE_TABLET.get()));
        nestedInventory.setItem(1, new ItemStack(CrystalBagItems.SMALL.get()));
        nestedInventory.tick(registries);
        require(nestedInventory.processingTime() == 0
            && !StoneTabletStorage.hasInventory(nestedInventory.getItem(0))
            && nestedInventory.getItem(1).is(CrystalBagItems.SMALL.get()),
            "nested inventory rejection mutated the machine");

        require(Arrays.equals(restored.getSlotsForFace(Direction.UP), new int[] {0})
            && restored.getSlotsForFace(Direction.DOWN).length == StateTransformerBlockEntity.SLOT_COUNT
            && restored.getSlotsForFace(Direction.NORTH).length == 0,
            "extract automation slot map changed");

        System.out.println("RR_STATE_TRANSFORMER_SELF_TEST_OK slots=50 duration=200 roundtrip=49");
    }

    private static TestTransformer seeded() {
        TestTransformer transformer = new TestTransformer();
        transformer.setItem(0, new ItemStack(TabletItems.STONE_TABLET.get()));
        transformer.setItem(1, new ItemStack(Items.COBBLESTONE, 4));
        transformer.setItem(2, new ItemStack(Items.EGG, 16));
        transformer.setItem(49, new ItemStack(Items.DIAMOND_SWORD));
        return transformer;
    }

    private static boolean contentsEmpty(TestTransformer transformer) {
        for (int slot = StateTransformerBlockEntity.FIRST_CONTENT_SLOT;
             slot < StateTransformerBlockEntity.SLOT_COUNT; slot++) {
            if (!transformer.getItem(slot).isEmpty()) return false;
        }
        return true;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class TestTransformer extends StateTransformerBlockEntity {
        private TestTransformer() {
            super(BlockPos.ZERO, StateTransformers.STATE_TRANSFORMER.get().defaultBlockState());
        }

        private CompoundTag write(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            saveData(tag, registries);
            return tag;
        }

        private void read(CompoundTag tag, HolderLookup.Provider registries) {
            loadData(tag, registries);
        }
    }
}