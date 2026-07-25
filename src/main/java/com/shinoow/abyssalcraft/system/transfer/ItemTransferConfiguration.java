package com.shinoow.abyssalcraft.system.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.shinoow.abyssalcraft.platform.ContainerCompat;

/**
 * A single item-transfer route (owned by PC-4) -- the data plane of the AbyssalCraft item-transport
 * system, configured by host blocks (spirit altar / state transformer / rending pedestal, later
 * stages) and executed by {@link ItemTransfer}.
 *
 * <p>Carries the origin/destination positions, the side to extract from / insert into, an up-to-5
 * {@link #FILTER_SIZE} item filter, and whether the filter matches item data (components) or item type
 * only. Fork-free: the only version-forked bit -- the filter's {@code ItemStack} NBT and the stacking
 * check -- goes through {@code platform/ContainerCompat}. Ported from 1.12.2
 * {@code ItemTransferConfiguration} (its multi-hop {@code BlockPos[]} route collapses to origin +
 * destination; the driver resolves intermediate hops).
 */
public final class ItemTransferConfiguration {

    public static final int FILTER_SIZE = 5;

    private final List<BlockPos> route = new ArrayList<>();
    private final NonNullList<ItemStack> filter = NonNullList.withSize(FILTER_SIZE, ItemStack.EMPTY);
    private Direction exitSide = Direction.UP;
    private Direction entrySide = Direction.DOWN;
    private boolean ignoreSubtypes;
    private boolean matchComponents;

    public ItemTransferConfiguration() {}

    public ItemTransferConfiguration(BlockPos origin, BlockPos destination) {
        route.add(origin.immutable());
        route.add(destination.immutable());
    }

    public ItemTransferConfiguration(List<BlockPos> route) {
        setRoute(route);
    }

    public BlockPos origin() { return route.isEmpty() ? BlockPos.ZERO : route.get(0); }
    public BlockPos destination() { return route.isEmpty() ? BlockPos.ZERO : route.get(route.size() - 1); }
    public List<BlockPos> route() { return Collections.unmodifiableList(route); }
    public Direction exitSide() { return exitSide; }
    public Direction entrySide() { return entrySide; }
    public NonNullList<ItemStack> filter() { return filter; }
    public boolean ignoreSubtypes() { return ignoreSubtypes; }
    public boolean matchComponents() { return matchComponents; }

    public ItemTransferConfiguration setRoute(List<BlockPos> positions) {
        route.clear();
        for (BlockPos position : positions) {
            route.add(position.immutable());
        }
        return this;
    }
    public ItemTransferConfiguration exitSide(Direction side) { this.exitSide = side; return this; }
    public ItemTransferConfiguration entrySide(Direction side) { this.entrySide = side; return this; }
    public ItemTransferConfiguration ignoreSubtypes(boolean flag) { this.ignoreSubtypes = flag; return this; }
    public ItemTransferConfiguration matchComponents(boolean flag) { this.matchComponents = flag; return this; }
    public ItemTransferConfiguration filterSlot(int slot, ItemStack stack) {
        filter.set(slot, stack.copyWithCount(stack.isEmpty() ? 0 : 1));
        return this;
    }

    public boolean isValid() {
        return route.size() >= 2 && !origin().equals(destination());
    }

    public ItemTransferConfiguration copy() {
        ItemTransferConfiguration copy = new ItemTransferConfiguration(route)
            .exitSide(exitSide).entrySide(entrySide)
            .ignoreSubtypes(ignoreSubtypes).matchComponents(matchComponents);
        for (int slot = 0; slot < FILTER_SIZE; slot++) {
            copy.filterSlot(slot, filter.get(slot));
        }
        return copy;
    }

    /** Whether {@code stack} passes the filter (an all-empty filter allows everything through). */
    public boolean matches(ItemStack stack) {
        boolean anyFilter = false;
        for (ItemStack f : filter) {
            if (f.isEmpty()) {
                continue;
            }
            anyFilter = true;
            if (!ItemStack.isSameItem(f, stack)) {
                continue;
            }
            if (!ignoreSubtypes && f.getDamageValue() != stack.getDamageValue()) {
                continue;
            }
            if (!matchComponents || (ignoreSubtypes
                    ? ContainerCompat.canStackIgnoringDamage(f, stack)
                    : ContainerCompat.canStack(f, stack))) {
                return true;
            }
        }
        return !anyFilter;
    }

    /** The filter as a predicate for {@link ItemTransfer#move}. */
    public Predicate<ItemStack> asFilter() {
        return this::matches;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] positions = new long[route.size()];
        for (int index = 0; index < route.size(); index++) {
            positions[index] = route.get(index).asLong();
        }
        tag.putLongArray("Route", positions);
        tag.putInt("ExitSide", exitSide.get3DDataValue());
        tag.putInt("EntrySide", entrySide.get3DDataValue());
        tag.putBoolean("FilterSubtypes", ignoreSubtypes);
        tag.putBoolean("FilterNBT", matchComponents);
        ContainerCompat.saveItems(tag, filter, registries);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        route.clear();
        for (long position : tag.getLongArray("Route")) {
            route.add(BlockPos.of(position));
        }
        if (route.size() < 2 && tag.contains("Origin") && tag.contains("Destination")) {
            route.clear();
            route.add(BlockPos.of(tag.getLong("Origin")));
            route.add(BlockPos.of(tag.getLong("Destination")));
        }
        exitSide = Direction.from3DDataValue(tag.getInt("ExitSide"));
        entrySide = Direction.from3DDataValue(tag.getInt("EntrySide"));
        ignoreSubtypes = tag.contains("FilterSubtypes")
            ? tag.getBoolean("FilterSubtypes") : tag.getBoolean("MatchSubtypes");
        matchComponents = tag.contains("FilterNBT")
            ? tag.getBoolean("FilterNBT") : tag.getBoolean("MatchComponents");
        ContainerCompat.loadItems(tag, filter, registries);
    }
}
