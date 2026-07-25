package com.shinoow.abyssalcraft.system.knowledge;

import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;

import net.minecraft.resources.ResourceLocation;

/**
 * A Necronomicon research entry (owned by PS-8), faithful to the 1.12.2 {@code api.knowledge.IResearchItem}.
 * A research is unlocked once all of its {@link IUnlockCondition}s are met (see
 * {@link KnowledgeGate#isUnlocked(com.shinoow.abyssalcraft.system.cap.necrodata.NecroData, IResearchItem, net.minecraft.world.entity.player.Player)}),
 * whereupon it is recorded in the player's necrodata (PS-2). {@code requiredLevel == -1} = always unlocked,
 * {@code -2} = never.
 */
public interface IResearchItem {

    IResearchItem setUnlockConditions(IUnlockCondition... conditions);

    IUnlockCondition[] getUnlockConditions();

    String getName();

    default String getDescription() {
        return getName() + ".description";
    }

    default String getHint() {
        return getName() + ".hint";
    }

    int getRequiredLevel();

    int getPointsCost();

    KnowledgeType getType();

    ResourceLocation getID();
}
