package com.shinoow.abyssalcraft.system.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * The queryable "item-transfer capability" (owned by PC-4): implemented by block entities that host
 * transfer routes (spirit altar / state transformer / rending pedestal, later stages).
 *
 * <p>Ported from the 1.12.2 {@code IItemTransferCapability}, but modernised off the loader capability
 * machinery: the configuration list + running flag simply live on the block entity, so the "capability"
 * is queried with {@code level.getBlockEntity(pos) instanceof ItemTransferHost host ? host : null} --
 * fork-free, no Forge {@code AttachCapabilitiesEvent} / NeoForge {@code BlockCapability} registration.
 * A running host drives its routes each tick through {@link ItemTransfer#run(net.minecraft.world.level.Level, ItemTransferHost)}.
 */
public final class ItemTransferHost {

    private final List<ItemTransferConfiguration> configurations = new ArrayList<>();
    private final Runnable dirty;
    private boolean running;

    public ItemTransferHost(Runnable dirty) {
        this.dirty = dirty == null ? () -> { } : dirty;
    }

    public List<ItemTransferConfiguration> getTransferConfigurations() {
        return Collections.unmodifiableList(configurations);
    }

    public void addTransferConfiguration(ItemTransferConfiguration configuration) {
        configurations.add(configuration.copy());
        dirty.run();
    }

    public void clearConfigurations() {
        if (!configurations.isEmpty()) {
            configurations.clear();
            dirty.run();
        }
    }

    public boolean isTransferRunning() {
        return running;
    }

    public void setTransferRunning(boolean running) {
        if (this.running != running) {
            this.running = running;
            dirty.run();
        }
    }

    /** Max items moved per configuration per run (per tick); hopper-like default. */
    public int transferRate() {
        return 1;
    }

    public boolean isEmpty() {
        return configurations.isEmpty() && !running;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag routes = new ListTag();
        for (ItemTransferConfiguration configuration : configurations) {
            routes.add(configuration.save(new CompoundTag(), registries));
        }
        tag.put("Configurations", routes);
        tag.putBoolean("Running", running);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        configurations.clear();
        ListTag routes = tag.getList("Configurations", Tag.TAG_COMPOUND);
        for (int index = 0; index < routes.size(); index++) {
            ItemTransferConfiguration configuration = new ItemTransferConfiguration();
            configuration.load(routes.getCompound(index), registries);
            if (configuration.isValid()) {
                configurations.add(configuration);
            }
        }
        running = tag.getBoolean("Running");
    }
}
