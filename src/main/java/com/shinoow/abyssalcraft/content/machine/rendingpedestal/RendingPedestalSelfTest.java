package com.shinoow.abyssalcraft.content.machine.rendingpedestal;

import java.util.Arrays;
import java.util.List;

import com.shinoow.abyssalcraft.content.item.book.BookItems;
import com.shinoow.abyssalcraft.content.item.material.MaterialItems;
import com.shinoow.abyssalcraft.content.item.ritual.RitualItems;
import com.shinoow.abyssalcraft.content.item.ritual.StaffOfRendingItem;
import com.shinoow.abyssalcraft.content.recipe.rending.RendingRecipe;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;
import com.shinoow.abyssalcraft.system.energy.IEnergyContainerItem;
import com.shinoow.abyssalcraft.system.rending.RendingEnergyType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RendingPedestalSelfTest {

    private RendingPedestalSelfTest() {}

    public static void run(HolderLookup.Provider registries) {
        List<RendingRecipe> recipes = recipes();
        require(recipes.size() == 4 && recipes.stream().allMatch(recipe ->
            RendingEnergyType.fromRecipe(recipe).filter(type -> type.validates(recipe)).isPresent()),
            "Rending four-recipe contract changed");

        TestPedestal pedestal = new TestPedestal();
        ItemStack energyInput = new ItemStack(BookItems.NECRONOMICON.get());
        ((IEnergyContainerItem) energyInput.getItem()).setEnergy(energyInput, 75.0F);
        pedestal.setItem(RendingPedestalBlockEntity.SLOT_ENERGY, energyInput);
        pedestal.transferInputEnergy();
        require(pedestal.getContainedEnergy() == 20.0F
            && ((IEnergyContainerItem) energyInput.getItem()).getContainedEnergy(energyInput) == 55.0F,
            "Rending Pedestal did not pull exactly 20 PE per tick");

        ItemStack staffStack = new ItemStack(RitualItems.STAFF_OF_RENDING.get());
        StaffOfRendingItem staff = (StaffOfRendingItem) staffStack.getItem();
        for (RendingEnergyType type : RendingEnergyType.values()) {
            staff.setEnergy(staffStack, type, type.threshold() + 7);
        }
        pedestal.setItem(RendingPedestalBlockEntity.SLOT_STAFF, staffStack);
        pedestal.drainStaffLedgers();
        for (RendingEnergyType type : RendingEnergyType.values()) {
            require(staff.getEnergy(staffStack, type) == 0
                && pedestal.getRendingEnergy(type) == type.threshold() + 7,
                "Staff ledger transfer failed for " + type);
        }

        pedestal.setItem(2, new ItemStack(Items.COBBLESTONE));
        pedestal.produceOutputs(recipes);
        require(pedestal.getRendingEnergy(RendingEnergyType.SHADOW) == 207
            && pedestal.getItem(2).is(Items.COBBLESTONE),
            "blocked Shadow output consumed its ledger");
        for (RendingEnergyType type : List.of(RendingEnergyType.ABYSSAL,
                RendingEnergyType.DREAD, RendingEnergyType.OMOTHOL)) {
            require(pedestal.getRendingEnergy(type) == 7
                && pedestal.getItem(RendingPedestalBlockEntity.FIRST_OUTPUT_SLOT + type.ordinal())
                    .is(recipeFor(recipes, type).result().getItem()),
                "Rending output or overflow failed for " + type);
        }
        pedestal.setItem(2, ItemStack.EMPTY);
        pedestal.produceOutputs(recipes);
        require(pedestal.getItem(2).is(MaterialItems.BASICS.get(22).get())
            && pedestal.getRendingEnergy(RendingEnergyType.SHADOW) == 7,
            "Shadow output did not preserve seven-point overflow");

        pedestal.setEnergy(321.5F);
        pedestal.addRendingEnergy(RendingEnergyType.DREAD, 90);
        CompoundTag saved = pedestal.write(registries);
        TestPedestal restored = new TestPedestal();
        restored.read(saved, registries);
        require(restored.getContainedEnergy() == 321.5F
            && restored.getRendingEnergy(RendingEnergyType.DREAD)
                == pedestal.getRendingEnergy(RendingEnergyType.DREAD),
            "Rending Pedestal NBT round-trip changed PE or ledger");

        ItemStack dropped = RendingPedestalBlock.stackWithState(
            RendingPedestals.RENDING_PEDESTAL.get().defaultBlockState(), restored);
        require(ItemDataCompat.getFloat(dropped, "PotEnergy") == 321.5F
            && ItemDataCompat.getInt(dropped, RendingEnergyType.DREAD.dataKey(), 0)
                == restored.getRendingEnergy(RendingEnergyType.DREAD),
            "Rending Pedestal block item lost PE or ledger state");
        require(Arrays.equals(restored.getSlotsForFace(Direction.DOWN), new int[] {2, 3, 4, 5})
            && restored.getSlotsForFace(Direction.UP).length == 0
            && restored.canTakeItemThroughFace(2, restored.getItem(2), Direction.DOWN)
            && !restored.canTakeItemThroughFace(2, restored.getItem(2), Direction.NORTH),
            "Rending Pedestal sided output map changed");
        int updatesBeforeRemoval = pedestal.renderUpdates();
        ItemStack removedStaff = pedestal.removeItem(RendingPedestalBlockEntity.SLOT_STAFF, 1);
        require(!removedStaff.isEmpty() && pedestal.getItem(RendingPedestalBlockEntity.SLOT_STAFF).isEmpty()
            && pedestal.renderUpdates() == updatesBeforeRemoval + 1,
            "Rending Pedestal staff removal did not refresh the client renderer");

        System.out.println("RR_RENDING_PEDESTAL_SELF_TEST_OK recipes=4 slots=6 pe=5000 ledgers=4 staffSync=ok");
    }

    private static List<RendingRecipe> recipes() {
        return List.of(
            recipe(RendingEnergyType.SHADOW, MaterialItems.BASICS.get(22).get()),
            recipe(RendingEnergyType.ABYSSAL, RitualItems.ABYSSAL_WASTELAND_ESSENCE.get()),
            recipe(RendingEnergyType.DREAD, RitualItems.DREADLANDS_ESSENCE.get()),
            recipe(RendingEnergyType.OMOTHOL, RitualItems.OMOTHOL_ESSENCE.get()));
    }

    private static RendingRecipe recipe(RendingEnergyType type, Item output) {
        return new RendingRecipe(type.recipeName(), type.threshold(), new ItemStack(output), "test", 0);
    }

    private static RendingRecipe recipeFor(List<RendingRecipe> recipes, RendingEnergyType type) {
        return recipes.stream().filter(recipe -> RendingEnergyType.fromRecipe(recipe).orElse(null) == type)
            .findFirst().orElseThrow();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class TestPedestal extends RendingPedestalBlockEntity {
        private int renderUpdates;

        private TestPedestal() {
            super(BlockPos.ZERO, RendingPedestals.RENDING_PEDESTAL.get().defaultBlockState());
        }

        private CompoundTag write(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            saveData(tag, registries);
            return tag;
        }

        private void read(CompoundTag tag, HolderLookup.Provider registries) {
            loadData(tag, registries);
        }

        @Override
        protected void markUpdated() {
            renderUpdates++;
        }

        private int renderUpdates() {
            return renderUpdates;
        }
    }
}