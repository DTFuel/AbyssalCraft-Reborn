package com.shinoow.abyssalcraft.system.knowledge;

import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;

import net.minecraft.resources.ResourceLocation;

/**
 * Base {@link IResearchItem} implementation (owned by PS-8), faithful to the 1.12.2
 * {@code api.knowledge.ResearchItem}. Content constructs these and registers them with
 * {@link ResearchRegistry}.
 */
public class ResearchItem implements IResearchItem {

    private final ResourceLocation id;
    private final String name;
    private final KnowledgeType type;
    private final int requiredLevel;
    private final int pointsCost;
    private IUnlockCondition[] conditions = new IUnlockCondition[0];

    public ResearchItem(ResourceLocation id, String name, KnowledgeType type, int requiredLevel, int pointsCost) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.pointsCost = pointsCost;
    }

    @Override
    public IResearchItem setUnlockConditions(IUnlockCondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public IUnlockCondition[] getUnlockConditions() {
        return conditions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public int getPointsCost() {
        return pointsCost;
    }

    @Override
    public KnowledgeType getType() {
        return type;
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
