package com.shinoow.abyssalcraft.system.portal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.shinoow.abyssalcraft.content.block.portal.PortalAnchorBlock;
import com.shinoow.abyssalcraft.content.block.portal.PortalAnchorBlockEntity;
import com.shinoow.abyssalcraft.platform.ACRef;
import com.shinoow.abyssalcraft.platform.SavedDataCompat;

/** Persistent per-dimension index of active portal anchors. */
public final class PortalAnchorIndex extends SavedDataCompat {

    private static final String DATA_NAME = "abyssalcraft_portal_anchors";

    private final Map<Long, ResourceKey<Level>> destinations = new LinkedHashMap<>();

    public static PortalAnchorIndex get(ServerLevel level) {
        return SavedDataCompat.getOrCreate(level, DATA_NAME, PortalAnchorIndex::new, PortalAnchorIndex::load);
    }

    public void register(BlockPos pos, ResourceKey<Level> destination) {
        ResourceKey<Level> previous = destinations.put(pos.asLong(), destination);
        if (!destination.equals(previous)) setDirty();
    }

    public void unregister(BlockPos pos) {
        if (destinations.remove(pos.asLong()) != null) setDirty();
    }

    /** Find the nearest active anchor whose portal returns to {@code sourceDimension}. */
    public Optional<BlockPos> findNearestReturnAnchor(ServerLevel level,
                                                       ResourceKey<Level> sourceDimension,
                                                       double targetX,
                                                       double targetZ) {
        List<Long> candidates = destinations.entrySet().stream()
            .filter(entry -> entry.getValue().equals(sourceDimension))
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparingDouble(packed -> horizontalDistance(BlockPos.of(packed), targetX, targetZ)))
            .toList();
        List<Long> stale = new ArrayList<>();
        for (long packed : candidates) {
            BlockPos pos = BlockPos.of(packed);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof PortalAnchorBlock
                && state.getValue(PortalAnchorBlock.ACTIVE)
                && level.getBlockEntity(pos) instanceof PortalAnchorBlockEntity anchor
                && sourceDimension.equals(anchor.getDestination())) {
                removeStale(stale);
                return Optional.of(pos);
            }
            stale.add(packed);
        }
        removeStale(stale);
        return Optional.empty();
    }

    private static double horizontalDistance(BlockPos pos, double x, double z) {
        double dx = pos.getX() + 0.5D - x;
        double dz = pos.getZ() + 0.5D - z;
        return dx * dx + dz * dz;
    }

    private void removeStale(List<Long> stale) {
        if (!stale.isEmpty()) {
            stale.forEach(destinations::remove);
            setDirty();
        }
    }

    @Override
    protected CompoundTag saveData(CompoundTag tag) {
        ListTag anchors = new ListTag();
        destinations.forEach((position, destination) -> {
            CompoundTag anchor = new CompoundTag();
            anchor.putLong("Position", position);
            anchor.putString("Destination", destination.location().toString());
            anchors.add(anchor);
        });
        tag.put("Anchors", anchors);
        return tag;
    }

    private static PortalAnchorIndex load(CompoundTag tag) {
        PortalAnchorIndex index = new PortalAnchorIndex();
        ListTag anchors = tag.getList("Anchors", Tag.TAG_COMPOUND);
        for (int i = 0; i < anchors.size(); i++) {
            CompoundTag anchor = anchors.getCompound(i);
            try {
                ResourceKey<Level> destination = ResourceKey.create(Registries.DIMENSION,
                    ACRef.parse(anchor.getString("Destination")));
                index.destinations.put(anchor.getLong("Position"), destination);
            } catch (RuntimeException ignored) {
                // Invalid legacy or manually edited entries are dropped on the next save.
            }
        }
        return index;
    }
}