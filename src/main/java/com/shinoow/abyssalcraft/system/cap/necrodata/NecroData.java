package com.shinoow.abyssalcraft.system.cap.necrodata;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * The necrodata capability's data (owned by PS-2), a thin fork-free view over the player's neutral
 * {@link CompoundTag} (attached by {@code platform/PlayerDataCompat}). Mutations write straight into the
 * backing tag, so they persist with the player and serialize on save automatically.
 *
 * <p>Faithful to the 1.12.2 {@code NecroDataCapability} data: seven unlock-trigger lists + completed
 * researches + the all-knowledge flag + a knowledge level + per-branch knowledge points. The
 * <em>consumption</em> of this data (unlock-condition processing, research gating) is the knowledge
 * subsystem's job (PS-8); this class is only the store. Dimension triggers are modernised from the
 * 1.12.2 numeric dimension ids to dimension {@code ResourceLocation} strings.
 */
public final class NecroData {

    private final CompoundTag tag;

    public NecroData(CompoundTag tag) {
        this.tag = tag;
    }

    // --- unlock triggers (append-unique) ---

    public boolean triggerEntityUnlock(String name) {
        if (name != null && name.contains(":")) {
            return addUnique("entityTriggers", name);
        }
        return false;
    }

    public boolean triggerBiomeUnlock(String name) {
        return addUnique("biomeTriggers", name);
    }

    public boolean triggerDimensionUnlock(String dimension) {
        return addUnique("dimensionTriggers", dimension);
    }

    public boolean triggerArtifactUnlock(String name) {
        return addUnique("artifactTriggers", name);
    }

    public boolean triggerPageUnlock(String name) {
        return addUnique("pageTriggers", name);
    }

    public boolean triggerWhisperUnlock(String name) {
        return addUnique("whisperTriggers", name);
    }

    public boolean triggerMiscUnlock(String name) {
        return addUnique("miscTriggers", name);
    }

    public boolean completeResearch(String research) {
        return addUnique("completedResearches", research);
    }

    public List<String> getEntityTriggers() {
        return getStringList("entityTriggers");
    }

    public List<String> getBiomeTriggers() {
        return getStringList("biomeTriggers");
    }

    public List<String> getDimensionTriggers() {
        return getStringList("dimensionTriggers");
    }

    public List<String> getArtifactTriggers() {
        return getStringList("artifactTriggers");
    }

    public List<String> getPageTriggers() {
        return getStringList("pageTriggers");
    }

    public List<String> getWhisperTriggers() {
        return getStringList("whisperTriggers");
    }

    public List<String> getMiscTriggers() {
        return getStringList("miscTriggers");
    }

    public List<String> getCompletedResearches() {
        return getStringList("completedResearches");
    }

    // --- all-knowledge flag + level + points ---

    public boolean hasUnlockedAllKnowledge() {
        return tag.getBoolean("HasAllKnowledge");
    }

    public boolean unlockAllKnowledge(boolean unlock) {
        if (hasUnlockedAllKnowledge() == unlock) {
            return false;
        }
        tag.putBoolean("HasAllKnowledge", unlock);
        return true;
    }

    public int getKnowledgeLevel() {
        return tag.getInt("knowledgeLevel");
    }

    public boolean setKnowledgeLevel(int level) {
        if (getKnowledgeLevel() == level) {
            return false;
        }
        tag.putInt("knowledgeLevel", level);
        return true;
    }

    public int getKnowledgePoints(KnowledgeType type) {
        return tag.getInt("kp_" + type.name());
    }

    public boolean setKnowledgePoints(KnowledgeType type, int points) {
        if (getKnowledgePoints(type) == points) {
            return false;
        }
        tag.putInt("kp_" + type.name(), points);
        return true;
    }

    public boolean increaseKnowledgePoints(KnowledgeType type, int points) {
        return points != 0 && setKnowledgePoints(type, getKnowledgePoints(type) + points);
    }

    /** The backing tag (the live store; mutations here persist with the player). */
    public CompoundTag toNbt() {
        return tag;
    }

    private List<String> getStringList(String key) {
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        List<String> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }
        return out;
    }

    private boolean addUnique(String key, String value) {
        if (value == null) {
            return false;
        }
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.getString(i))) {
                return false;
            }
        }
        list.add(StringTag.valueOf(value));
        tag.put(key, list);
        return true;
    }
}
