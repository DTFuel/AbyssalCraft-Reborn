package com.shinoow.abyssalcraft.system.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.shinoow.abyssalcraft.platform.SavedDataCompat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

/**
 * Per-world necromancy saved data (owned by PS-11), faithful to the 1.12.2
 * {@code common.world.data.NecromancyWorldSavedData}. Stores up to 20 snapshots of dead entities (keyed by
 * entity name) captured on death, consumed later by the Necronomicon resurrection ritual (deferred content,
 * PS-6). Fork-free NBT; persistence / access forks live in {@code platform/SavedDataCompat}.
 */
public final class NecromancyData extends SavedDataCompat {

    private static final String DATA_NAME = "abyssalcraft_necromancy";
    private static final int MAX_ENTRIES = 20;

    /** One captured dead-entity snapshot. */
    public record Entry(String name, CompoundTag tag) {}

    private final List<Entry> entries = new ArrayList<>();

    public NecromancyData() {}

    /** The necromancy data for {@code level} (created if absent). */
    public static NecromancyData get(ServerLevel level) {
        return SavedDataCompat.getOrCreate(level, DATA_NAME, NecromancyData::new, NecromancyData::load);
    }

    /** Capture a dead entity's NBT (with the resurrection crystal size), capped at 20 (oldest dropped). */
    public void storeData(String name, CompoundTag data, int crystalSize) {
        data.putInt("ResurrectionRitualCrystalSize", crystalSize);
        if (entries.size() >= MAX_ENTRIES) {
            entries.remove(0);
        }
        entries.add(new Entry(name, data));
        setDirty();
    }

    public CompoundTag getDataForName(String name) {
        for (Entry entry : entries) {
            if (entry.name().equals(name)) {
                return entry.tag();
            }
        }
        return null;
    }

    public void clearEntry(String name) {
        for (Iterator<Entry> it = entries.iterator(); it.hasNext();) {
            if (it.next().name().equals(name)) {
                it.remove();
                setDirty();
                return;
            }
        }
    }

    public List<Entry> getData() {
        return Collections.unmodifiableList(entries);
    }

    public static int crystalSize(float height) {
        if (height >= 1.5F) return 2;
        if (height >= 0.75F) return 1;
        return 0;
    }

    /** Serialize through the version-neutral payload used by SavedDataCompat. */
    public CompoundTag serialize() {
        return saveData(new CompoundTag());
    }

    @Override
    protected CompoundTag saveData(CompoundTag tag) {
        tag.putInt("Version", 1);
        CompoundTag data = new CompoundTag();
        for (Entry entry : entries) {
            ListTag list = data.getList(entry.name(), Tag.TAG_COMPOUND);
            list.add(entry.tag());
            data.put(entry.name(), list);
        }
        tag.put("Data", data);
        return tag;
    }

    /** Rebuild from saved NBT (referenced by {@code platform/SavedDataCompat.getOrCreate}). */
    public static NecromancyData load(CompoundTag tag) {
        NecromancyData necromancy = new NecromancyData();
        CompoundTag data = tag.getCompound("Data");
        for (String name : data.getAllKeys()) {
            ListTag list = data.getList(name, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                if (necromancy.entries.size() >= MAX_ENTRIES) {
                    necromancy.entries.remove(0);
                }
                necromancy.entries.add(new Entry(name, list.getCompound(i)));
            }
        }
        return necromancy;
    }
}
