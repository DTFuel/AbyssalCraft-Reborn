package com.shinoow.abyssalcraft.content.item.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import com.shinoow.abyssalcraft.platform.ContainerCompat;
import com.shinoow.abyssalcraft.platform.ItemDataCompat;

public final class SpiritTabletStorage {

    public static final int FILTER_SIZE = 5;
    private static final String FILTER_KEY = "FilterItems";

    private SpiritTabletStorage() {}

    public static int mode(ItemStack tablet) {
        return Math.floorMod(ItemDataCompat.copyData(tablet).getInt("Mode"), 3);
    }

    public static void setMode(ItemStack tablet, int mode) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        tag.putInt("Mode", Math.floorMod(mode, 3));
        ItemDataCompat.setData(tablet, tag);
    }

    public static List<BlockPos> route(ItemStack tablet) {
        long[] positions = ItemDataCompat.copyData(tablet).getLongArray("Route");
        List<BlockPos> route = new ArrayList<>(positions.length);
        for (long position : positions) {
            route.add(BlockPos.of(position));
        }
        return Collections.unmodifiableList(route);
    }

    public static void appendRoute(ItemStack tablet, BlockPos position, Direction entrySide,
                                   ResourceLocation dimension) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        if (tag.contains("RouteDimension") && !tag.getString("RouteDimension").equals(dimension.toString())) {
            tag.remove("Route");
        }
        long[] oldRoute = tag.getLongArray("Route");
        long[] route = new long[oldRoute.length + 1];
        System.arraycopy(oldRoute, 0, route, 0, oldRoute.length);
        route[oldRoute.length] = position.asLong();
        tag.putLongArray("Route", route);
        tag.putInt("EntrySide", entrySide.get3DDataValue());
        tag.putString("RouteDimension", dimension.toString());
        ItemDataCompat.setData(tablet, tag);
    }

    public static void clearRoute(ItemStack tablet) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        tag.remove("Route");
        tag.remove("EntrySide");
        tag.remove("RouteDimension");
        ItemDataCompat.setData(tablet, tag);
    }

    public static Direction entrySide(ItemStack tablet) {
        return Direction.from3DDataValue(ItemDataCompat.copyData(tablet).getInt("EntrySide"));
    }

    public static boolean isRouteDimension(ItemStack tablet, ResourceLocation dimension) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        return tag.contains("RouteDimension") && tag.getString("RouteDimension").equals(dimension.toString());
    }

    public static String routeDimension(ItemStack tablet) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        return tag.contains("RouteDimension") ? tag.getString("RouteDimension") : "-";
    }

    public static boolean ignoreSubtypes(ItemStack tablet) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        return tag.getBoolean("FilterSubtypes");
    }

    public static boolean matchComponents(ItemStack tablet) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        return tag.contains("FilterNBT") ? tag.getBoolean("FilterNBT") : tag.getBoolean("FilterComponents");
    }

    public static void toggleFilter(ItemStack tablet, int id) {
        CompoundTag tag = ItemDataCompat.copyData(tablet);
        String key = id == 0 ? "FilterSubtypes" : "FilterNBT";
        boolean current = tag.getBoolean(key);
        tag.putBoolean(key, !current);
        ItemDataCompat.setData(tablet, tag);
    }

    public static NonNullList<ItemStack> loadFilter(ItemStack tablet, HolderLookup.Provider registries) {
        NonNullList<ItemStack> filter = NonNullList.withSize(FILTER_SIZE, ItemStack.EMPTY);
        CompoundTag root = ItemDataCompat.copyData(tablet);
        if (root.contains(FILTER_KEY, Tag.TAG_COMPOUND)) {
            ContainerCompat.loadItems(root.getCompound(FILTER_KEY), filter, registries);
        }
        return filter;
    }

    public static int filterCount(ItemStack tablet) {
        CompoundTag root = ItemDataCompat.copyData(tablet);
        if (!root.contains(FILTER_KEY, Tag.TAG_COMPOUND)) return 0;
        return root.getCompound(FILTER_KEY).getList("Items", Tag.TAG_COMPOUND).size();
    }

    public static void saveFilter(ItemStack tablet, NonNullList<ItemStack> filter,
                                  HolderLookup.Provider registries) {
        CompoundTag filterTag = new CompoundTag();
        ContainerCompat.saveItems(filterTag, filter, registries);
        CompoundTag root = ItemDataCompat.copyData(tablet);
        root.put(FILTER_KEY, filterTag);
        ItemDataCompat.setData(tablet, root);
    }
}