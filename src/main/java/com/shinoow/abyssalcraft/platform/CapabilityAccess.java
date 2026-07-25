package com.shinoow.abyssalcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

//? if forge {
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
//?} else {
/*import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
*///?}

/**
 * Compat: capability lookups (loader axis - the largest divergence).
 *
 * <p>Forge queries a BlockEntity's {@code LazyOptional} capability; NeoForge queries a static
 * {@code BlockCapability} on the Level. Both return {@code null} when the capability is absent.
 * BlockEntity-side capability <em>registration</em> is added here when the first machine lands
 * (Stage P), following the design rule "new divergence -> new compat method first".
 */
public final class CapabilityAccess {

    private CapabilityAccess() {}

    /**
     * Neutral view over a loader's item-handler capability, so business code (the item-transfer
     * engine) reads/writes an exposed inventory without importing the loader-forked
     * {@code IItemHandler}. Slot semantics mirror {@code IItemHandler}: {@link #insert}/{@link #extract}
     * return the leftover / extracted stack and honour {@code simulate}.
     */
    public interface ItemView {
        int size();
        ItemStack getStackInSlot(int slot);
        ItemStack insert(int slot, ItemStack stack, boolean simulate);
        ItemStack extract(int slot, int amount, boolean simulate);
    }

    /** Neutral {@link ItemView} of the block at {@code pos} on {@code side}, or {@code null} if none. */
    public static ItemView itemView(Level level, BlockPos pos, Direction side) {
        //? if forge {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        return handler == null ? null : wrap(handler);
        //?} else {
        /*IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return handler == null ? null : wrap(handler);
        *///?}
    }

    /** Adapt a loader {@code IItemHandler} to the neutral {@link ItemView} (identical method surface). */
    private static ItemView wrap(IItemHandler handler) {
        return new ItemView() {
            @Override public int size() { return handler.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return handler.getStackInSlot(slot); }
            @Override public ItemStack insert(int slot, ItemStack stack, boolean simulate) { return handler.insertItem(slot, stack, simulate); }
            @Override public ItemStack extract(int slot, int amount, boolean simulate) { return handler.extractItem(slot, amount, simulate); }
        };
    }

    /** Item handler exposed by the block at {@code pos} on the given side, or {@code null}. */
    public static IItemHandler itemHandler(Level level, BlockPos pos, Direction side) {
        //? if forge {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        //?} else {
        /*return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        *///?}
    }

    /** Fluid handler exposed by the block at {@code pos} on the given side, or {@code null}. */
    public static IFluidHandler fluidHandler(Level level, BlockPos pos, Direction side) {
        //? if forge {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
        //?} else {
        /*return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
        *///?}
    }
}
