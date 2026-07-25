package com.shinoow.abyssalcraft.system.energy;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Persistent state shared by statue and depositioner PE manipulators. */
public final class ManipulatorState {

    private static final int MAX_COLLECTORS = 20;

    private final Set<BlockPos> collectors = new LinkedHashSet<>();
    private int tolerance;
    private DeityType activeDeity;
    private AmplifierType activeAmplifier;

    public Set<BlockPos> collectors() {
        return collectors;
    }

    public int tolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = Math.max(0, tolerance);
    }

    public DeityType activeDeity() {
        return activeDeity;
    }

    public void setActiveDeity(DeityType activeDeity) {
        this.activeDeity = activeDeity;
    }

    public AmplifierType activeAmplifier() {
        return activeAmplifier;
    }

    public void setActiveAmplifier(AmplifierType activeAmplifier) {
        this.activeAmplifier = activeAmplifier;
    }

    public void save(CompoundTag tag) {
        tag.putInt("Tolerance", tolerance);
        tag.putString("ActiveDeity", activeDeity == null ? "" : activeDeity.name());
        tag.putString("ActiveAmplifier", activeAmplifier == null ? "" : activeAmplifier.name());
        tag.putLongArray("EnergyCollectors", collectors.stream().mapToLong(BlockPos::asLong).toArray());
    }

    public void load(CompoundTag tag) {
        setTolerance(tag.getInt("Tolerance"));
        activeDeity = parseEnum(DeityType.class, tag.getString("ActiveDeity"));
        activeAmplifier = parseEnum(AmplifierType.class, tag.getString("ActiveAmplifier"));
        collectors.clear();
        long[] positions = tag.getLongArray("EnergyCollectors");
        for (int index = 0; index < Math.min(positions.length, MAX_COLLECTORS); index++) {
            collectors.add(BlockPos.of(positions[index]));
        }
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String name) {
        if (name.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}