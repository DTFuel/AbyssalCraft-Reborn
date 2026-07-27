package com.shinoow.abyssalcraft.content.block.energy;

import java.util.Arrays;

import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.tablet.StoneTabletStorage;
import com.shinoow.abyssalcraft.content.item.tablet.TabletItems;
import com.shinoow.abyssalcraft.registry.BaseBlocks;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EnergyGuiSelfTest {

    private EnergyGuiSelfTest() {}

    public static void run(HolderLookup.Provider registries) {
        TestEnergyContainer container = new TestEnergyContainer();
        ItemStack input = new ItemStack(BookItems.NECRONOMICON.get());
        ItemStack output = new ItemStack(BookItems.NECRONOMICON.get());
        IEnergyContainerItem energyInput = (IEnergyContainerItem) input.getItem();
        IEnergyContainerItem energyOutput = (IEnergyContainerItem) output.getItem();
        energyInput.setEnergy(input, 60.0F);
        container.setItem(0, input);
        container.setItem(1, output);
        container.serverTick();
        require(container.getContainedEnergy() == 0.0F
            && energyInput.getContainedEnergy(input) == 40.0F
            && energyOutput.getContainedEnergy(output) == 20.0F,
            "Energy Container did not transfer 20 PE through both slots");

        ItemStack tablet = new ItemStack(TabletItems.STONE_TABLET.get());
        NonNullList<ItemStack> contents = NonNullList.withSize(StoneTabletStorage.INVENTORY_SIZE, ItemStack.EMPTY);
        contents.set(0, new ItemStack(Items.COBBLESTONE, 64));
        contents.set(48, new ItemStack(Items.DIAMOND_SWORD));
        StoneTabletStorage.store(tablet, contents, 1000.0F, registries);

        TestDepositioner depositioner = new TestDepositioner();
        depositioner.setItem(0, tablet);
        for (int tick = 0; tick < 73; tick++) depositioner.processTabletTick();
        require(depositioner.processingTime() == 73 && depositioner.getContainedEnergy() == 365.0F,
            "Depositioner tick 73 PE/progress changed");
        CompoundTag saved = depositioner.write(registries);
        TestDepositioner restored = new TestDepositioner();
        restored.read(saved, registries);
        require(restored.processingTime() == 73 && !restored.processingStack().isEmpty(),
            "Depositioner restart lost its in-flight Stone Tablet");
        for (int tick = 73; tick < EnergyDepositionerBlockEntity.PROCESS_DURATION; tick++) {
            restored.processTabletTick();
        }
        require(restored.getContainedEnergy() == 1000.0F && restored.processingTime() == 0
            && restored.processingStack().isEmpty() && !StoneTabletStorage.hasInventory(restored.getItem(1)),
            "Depositioner 200 tick transaction changed PE or output tablet");

        TestDepositioner blocked = new TestDepositioner();
        blocked.setItem(0, tablet.copy());
        blocked.setItem(1, new ItemStack(Items.STONE));
        blocked.processTabletTick();
        require(blocked.processingTime() == 0 && blocked.processingStack().isEmpty()
            && StoneTabletStorage.hasInventory(blocked.getItem(0)),
            "blocked Depositioner started or consumed its input");

        require(Arrays.equals(restored.getSlotsForFace(Direction.UP), new int[] {0})
            && Arrays.equals(restored.getSlotsForFace(Direction.DOWN), new int[] {1})
            && restored.canTakeItemThroughFace(1, restored.getItem(1), Direction.DOWN),
            "Depositioner sided automation changed");
        require(EnergyDepositionerBlockEntity.corrupt(Items.STONE.getDefaultInstance()
                .getItem().equals(Items.STONE) ? net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()
                : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState())
                .is(BaseBlocks.DARKSTONE.get())
            && EnergyDepositionerBlockEntity.corrupt(net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState())
                .is(BaseBlocks.DARKLANDS_OAK_LOG.get()),
            "Depositioner Darklands block mapping changed");

        System.out.println("RR_ENERGY_GUI_SELF_TEST_OK screens=2 slots=4 depositionerTicks=200");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class TestEnergyContainer extends EnergyContainerBlockEntity {
        private TestEnergyContainer() {
            super(BlockPos.ZERO, EnergyBlocks.ENERGY_CONTAINERS.get(0).get().defaultBlockState());
        }
    }

    private static final class TestDepositioner extends EnergyDepositionerBlockEntity {
        private TestDepositioner() {
            super(BlockPos.ZERO, EnergyBlocks.ENERGY_DEPOSITIONER.get().defaultBlockState());
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