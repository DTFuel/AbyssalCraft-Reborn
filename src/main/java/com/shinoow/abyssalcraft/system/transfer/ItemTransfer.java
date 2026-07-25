package com.shinoow.abyssalcraft.system.transfer;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.shinoow.abyssalcraft.platform.CapabilityAccess;
import com.shinoow.abyssalcraft.platform.CapabilityAccess.ItemView;

/**
 * Item-transfer engine (owned by PC-4): the transfer logic of the AbyssalCraft item-transport system.
 *
 * <p>Moves filtered items between two inventories exposed as neutral {@link ItemView}s -- obtained
 * either from a loader item-handler capability ({@link CapabilityAccess#itemView}) or from a vanilla
 * {@link com.shinoow.abyssalcraft.system.transfer.ContainerItemView Container}. Fully vanilla /
 * fork-free: the only loader divergence (the {@code IItemHandler} package + the item-stacking check)
 * is hidden in {@code platform/CapabilityAccess} and {@code platform/ContainerCompat}. Host block
 * entities (spirit altar / state transformer / rending pedestal, later stages) drive this via
 * {@link #run} once per tick.
 */
public final class ItemTransfer {

    private ItemTransfer() {}

    /** Move up to {@code maxAmount} filtered items from {@code from} into {@code to}; returns moved count. */
    public static int move(ItemView from, ItemView to, int maxAmount, Predicate<ItemStack> filter) {
        if (from == null || to == null || maxAmount <= 0) {
            return 0;
        }
        int moved = 0;
        for (int slot = 0; slot < from.size() && moved < maxAmount; slot++) {
            ItemStack inSlot = from.getStackInSlot(slot);
            if (inSlot.isEmpty() || (filter != null && !filter.test(inSlot))) {
                continue;
            }
            int want = Math.min(maxAmount - moved, inSlot.getCount());
            ItemStack simulated = from.extract(slot, want, true);
            if (simulated.isEmpty()) {
                continue;
            }
            int accepted = simulated.getCount() - insertAll(to, simulated, true).getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = from.extract(slot, accepted, false);
            if (extracted.isEmpty()) {
                continue;
            }
            ItemStack leftover = insertAll(to, extracted, false);
            if (!leftover.isEmpty()) {
                from.insert(slot, leftover, false); // safety: return the unaccepted remainder
            }
            moved += extracted.getCount() - leftover.getCount();
        }
        return moved;
    }

    /** Insert {@code stack} across all slots of {@code to}; returns the leftover that did not fit. */
    private static ItemStack insertAll(ItemView to, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < to.size() && !remaining.isEmpty(); slot++) {
            remaining = to.insert(slot, remaining, simulate);
        }
        return remaining;
    }

    /** Convenience: resolve both inventories via item-handler capability and move between them. */
    public static int transfer(Level level, BlockPos fromPos, Direction fromSide, BlockPos toPos, Direction toSide,
                               int maxAmount, Predicate<ItemStack> filter) {
        return move(CapabilityAccess.itemView(level, fromPos, fromSide),
            CapabilityAccess.itemView(level, toPos, toSide), maxAmount, filter);
    }

    /** Run every configuration of a running host once (each origin -> destination); returns items moved. */
    public static int run(Level level, ItemTransferHost host) {
        if (!host.isTransferRunning()) {
            return 0;
        }
        int total = 0;
        for (ItemTransferConfiguration config : host.getTransferConfigurations()) {
            if (!config.isValid()) {
                continue;
            }
            total += transfer(level, config.origin(), config.exitSide(), config.destination(), config.entrySide(),
                host.transferRate(), config.asFilter());
        }
        return total;
    }

    /**
     * Dev self-check exercising the fork-free engine over two {@link SimpleContainer}s (no capability
     * needed): fill a source slot with 64 cobblestone, move 40, and verify source/destination counts.
     * Returns a single-line PASS/FAIL result for logging.
     */
    public static String selfTest() {
        SimpleContainer source = new SimpleContainer(3);
        SimpleContainer dest = new SimpleContainer(3);
        source.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        int moved = move(new ContainerItemView(source), new ContainerItemView(dest), 40, stack -> true);
        int srcCount = source.getItem(0).getCount();
        int dstCount = dest.getItem(0).getCount();
        boolean pass = moved == 40 && srcCount == 24 && dstCount == 40;
        return "ItemTransfer self-test: moved=" + moved + " src=" + srcCount + " dst=" + dstCount
            + " -> " + (pass ? "PASS" : "FAIL");
    }
}
