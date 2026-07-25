package com.shinoow.abyssalcraft.content.machine.crystallizer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.blockentity.base.MachineBlockEntity;
import com.shinoow.abyssalcraft.content.recipe.crystallization.CrystallizationRecipe;
import com.shinoow.abyssalcraft.platform.DataRecipeCompat;
import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.MachineItemCompat;
import com.shinoow.abyssalcraft.registry.ModRecipes;

/**
 * Crystallizer block entity (owned by PP-2) -- a functional furnace-style machine on the PP-1
 * {@link MachineBlockEntity} base.
 *
 * <p>Input + fuel -&gt; crystallized output over time, driven by {@code abyssalcraft:crystallization}
 * recipes. Recipe lookup and the Recipe-interface differences are absorbed by {@link RecipeCompat}, so
 * this class carries no loader fork.
 */
public class CrystallizerBlockEntity extends MachineBlockEntity implements WorldlyContainer {

    public static final int SLOT_SECONDARY_OUTPUT = 3;
    private static final int[] TOP_SLOTS = {SLOT_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_FUEL};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT, SLOT_SECONDARY_OUTPUT, SLOT_FUEL};

    public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(Crystallizers.CRYSTALLIZER_BE.get(), pos, state, 4, 200);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.abyssalcraft.crystallizer");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new CrystallizerMenu(windowId, playerInv, this, dataAccess);
    }

    @Override
    protected void serverTick() {
        boolean wasBurning = burnTime > 0;
        boolean changed = false;

        if (burnTime > 0) {
            burnTime--;
            changed = true;
        }

        ItemStack input = items.get(SLOT_INPUT);
        DataRecipeCompat.Entry<CrystallizationRecipe> entry = DataRecipeCompat.findEntry(
            level, ModRecipes.CRYSTALLIZATION.get(), recipe -> recipe.input().test(input)).orElse(null);
        CrystallizationRecipe recipe = entry == null ? null : entry.value();
        if (selectRecipe(entry == null ? null : entry.id())) changed = true;
        boolean canProduce = recipe != null && hasOutputRoom(SLOT_OUTPUT, recipe.result())
            && hasOutputRoom(SLOT_SECONDARY_OUTPUT, recipe.secondaryResult());

        if (canProduce) {
            ItemStack fuel = items.get(SLOT_FUEL);
            int fuelBurn = fuelBurnTime(fuel);
            if (burnTime == 0 && fuelBurn > 0) {
                burnTime = fuelBurn;
                maxBurnTime = fuelBurn;
                consumeFuel();
                changed = true;
            }
            if (burnTime > 0) {
                maxProgress = recipe.time();
                progress++;
                changed = true;
                if (progress >= maxProgress) {
                    produce(SLOT_OUTPUT, recipe.result());
                    produce(SLOT_SECONDARY_OUTPUT, recipe.secondaryResult());
                    recordExperience(SLOT_OUTPUT, entry.id(), recipe.result().getCount(), recipe.experience());
                    recordExperience(SLOT_SECONDARY_OUTPUT, entry.id(), recipe.secondaryResult().getCount(), recipe.experience());
                    consumeInput();
                    progress = 0;
                    changed = true;
                }
            } else {
                if (progress != 0) {
                    progress = 0;
                    changed = true;
                }
            }
        } else {
            if (progress != 0) {
                progress = 0;
                changed = true;
            }
        }

        if (wasBurning != (burnTime > 0)) {
            if (level != null && getBlockState().hasProperty(CrystallizerBlock.LIT)) {
                level.setBlock(worldPosition, getBlockState().setValue(CrystallizerBlock.LIT, burnTime > 0), 3);
            }
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean hasOutputRoom(int slot, ItemStack result) {
        if (result.isEmpty()) return true;
        ItemStack out = items.get(slot);
        if (out.isEmpty()) {
            return true;
        }
        if (!ContainerCompat.canStack(out, result)) {
            return false;
        }
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void produce(int slot, ItemStack result) {
        if (result.isEmpty()) return;
        ItemStack out = items.get(slot);
        if (out.isEmpty()) {
            items.set(slot, result.copy());
        } else {
            out.grow(result.getCount());
        }
    }

    public static boolean isFuel(ItemStack stack) {
        return fuelBurnTime(stack) > 0;
    }

    public boolean isRecipeInput(ItemStack stack) {
        return level != null && DataRecipeCompat.findEntry(level, ModRecipes.CRYSTALLIZATION.get(),
            recipe -> recipe.input().test(stack)).isPresent();
    }

    public static int fuelBurnTime(ItemStack stack) {
        if (MachineItemCompat.is(stack, "abyssalcraft:dread_fragment")) return 100;
        if (MachineItemCompat.is(stack, "abyssalcraft:dreaded_shard_of_abyssalnite")) return 1000;
        if (stack.is(Items.BLAZE_POWDER)) return 1200;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        if (MachineItemCompat.is(stack, "abyssalcraft:methane")) return 10000;
        if (MachineItemCompat.is(stack, "abyssalcraft:carbon_cluster")) return 1600;
        if (MachineItemCompat.is(stack, "abyssalcraft:dense_carbon_cluster")) return 12800;
        if (MachineItemCompat.is(stack, "abyssalcraft:crystal_carbon")) return 200;
        if (MachineItemCompat.is(stack, "abyssalcraft:carbon_crystal_cluster")) return 1800;
        return 0;
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT || slot == SLOT_FUEL && isFuel(stack);
    }

    private void consumeInput() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack remainder = MachineItemCompat.craftingRemainder(input);
        input.shrink(1);
        if (input.isEmpty() && !remainder.isEmpty()) {
            items.set(SLOT_INPUT, remainder);
        }
    }

    private void consumeFuel() {
        ItemStack fuel = items.get(SLOT_FUEL);
        ItemStack remainder = MachineItemCompat.craftingRemainder(fuel);
        fuel.shrink(1);
        if (fuel.isEmpty() && !remainder.isEmpty()) {
            items.set(SLOT_FUEL, remainder);
        }
    }

    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? TOP_SLOTS : side == Direction.DOWN ? BOTTOM_SLOTS : SIDE_SLOTS;
    }

    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_INPUT || slot == SLOT_FUEL && isFuel(stack);
    }

    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT || slot == SLOT_SECONDARY_OUTPUT || slot == SLOT_FUEL && !isFuel(stack);
    }
}
