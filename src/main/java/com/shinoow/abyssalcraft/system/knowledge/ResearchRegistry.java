package com.shinoow.abyssalcraft.system.knowledge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Registry of Necronomicon research entries and Crystallizer offerings (owned by PS-8), faithful to the
 * 1.12.2 {@code api.knowledge.ResearchRegistry}. Content registers its {@link IResearchItem}s here; the
 * Necronomicon book (deferred GUI) reads them, gating each entry through {@link KnowledgeGate}. Offerings are
 * the items that grant knowledge points of a given {@link KnowledgeType} when crystallized.
 */
public final class ResearchRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResearchRegistry INSTANCE = new ResearchRegistry();

    private final List<IResearchItem> researches = new ArrayList<>();
    private final Map<KnowledgeType, List<ItemStack>> offerings = new EnumMap<>(KnowledgeType.class);

    private ResearchRegistry() {
        for (KnowledgeType type : KnowledgeType.values()) {
            offerings.put(type, new ArrayList<>());
        }
    }

    public static ResearchRegistry instance() {
        return INSTANCE;
    }

    public void registerResearchItem(IResearchItem research) {
        for (IResearchItem entry : researches) {
            if (entry.getID().equals(research.getID())) {
                LOGGER.error("Research Item with ID already registered: {}", research.getID());
                return;
            }
        }
        researches.add(research);
    }

    public List<IResearchItem> getResearchItems() {
        return Collections.unmodifiableList(researches);
    }

    public IResearchItem getResearchItemById(ResourceLocation id) {
        for (IResearchItem research : researches) {
            if (research.getID().equals(id)) {
                return research;
            }
        }
        return null;
    }

    public List<ItemStack> getOfferingsForType(KnowledgeType type) {
        return offerings.get(type);
    }

    public boolean isOfferingOfType(KnowledgeType type, ItemStack stack) {
        for (ItemStack offering : offerings.get(type)) {
            if (ItemStack.isSameItem(offering, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOffering(ItemStack stack) {
        for (KnowledgeType type : KnowledgeType.values()) {
            if (isOfferingOfType(type, stack)) {
                return true;
            }
        }
        return false;
    }

    public void addOffering(KnowledgeType type, ItemStack stack) {
        if (isOfferingOfType(type, stack)) {
            LOGGER.error("Offering is already registered: {}", stack);
            return;
        }
        offerings.get(type).add(stack);
    }
}
