package com.shinoow.abyssalcraft.client.necronomicon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A Necronomicon information node (owned by PH-5), faithful to the recursive 1.12.2
 * {@code api.necronomicon.NecroData} tree: an identifier + title + optional body text + display icon, with
 * nested child entries (categories &rarr; chapters &rarr; pages). Optionally gated behind a research id.
 *
 * <p>The knowledge gate (1.12.2 {@code IResearchItem}) is decoupled to an optional research id; the screen
 * resolves it against the player's synced necrodata (PS-2) which PS-8 populates &mdash; so PH-5 reads PS-8's
 * output without depending on its runtime {@code KnowledgeGate} side effects.
 */
public final class NecronomiconEntry {

    private final String id;
    private final String titleKey;
    private final String textKey;
    private final ItemStack icon;
    private Object[] textArgs = new Object[0];
    private ResourceLocation researchId; // null = always visible
    private ResourceLocation advancementId;
    private boolean showWhenLocked;
    private final List<NecronomiconEntry> children = new ArrayList<>();

    public NecronomiconEntry(String id, String titleKey, String textKey, ItemStack icon) {
        this.id = id;
        this.titleKey = titleKey;
        this.textKey = textKey;
        this.icon = icon;
    }

    public NecronomiconEntry(String id, String titleKey) {
        this(id, titleKey, null, ItemStack.EMPTY);
    }

    public NecronomiconEntry addChild(NecronomiconEntry child) {
        children.add(child);
        return this;
    }

    /** Gate this entry behind a research id (resolved against the player's necrodata by the screen). */
    public NecronomiconEntry setResearch(ResourceLocation researchId) {
        this.researchId = researchId;
        return this;
    }

    /** Gate this entry behind completion of an AbyssalCraft progression advancement. */
    public NecronomiconEntry setAdvancement(ResourceLocation advancementId) {
        this.advancementId = advancementId;
        return this;
    }

    public NecronomiconEntry showWhenLocked() {
        showWhenLocked = true;
        return this;
    }

    public NecronomiconEntry setTextArgs(Object... textArgs) {
        this.textArgs = textArgs;
        return this;
    }

    public boolean isShownWhenLocked() {
        return showWhenLocked;
    }

    public String id() {
        return id;
    }

    public String titleKey() {
        return titleKey;
    }

    public String textKey() {
        return textKey;
    }

    public boolean hasText() {
        return textKey != null && !textKey.isEmpty();
    }

    public Component text() {
        return Component.translatable(textKey, textArgs);
    }

    public ItemStack icon() {
        return icon;
    }

    /** The research gate id, or {@code null} for always-visible. */
    public ResourceLocation researchId() {
        return researchId;
    }

    public ResourceLocation advancementId() {
        return advancementId;
    }

    public List<NecronomiconEntry> children() {
        return Collections.unmodifiableList(children);
    }
}
