package com.shinoow.abyssalcraft.system.knowledge.condition;

import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;

/**
 * A single locked-knowledge condition (owned by PS-8), faithful to the 1.12.2
 * {@code api.knowledge.condition.IUnlockCondition}. {@link #getType()} selects the
 * {@link IConditionProcessor} that checks it against the player's necrodata (PS-2); {@link #getConditionObject()}
 * is the value that processor compares (an entity id string, a dimension id string, a {@code String[]}, ...).
 * Special types: {@code -1} = always met, {@code -2} = never met.
 */
public interface IUnlockCondition {

    boolean areConditionObjectsEqual(Object other);

    Object getConditionObject();

    int getType();

    IUnlockCondition setHint(String hint);

    String getHint();

    int getPointsCost();

    IUnlockCondition setPointsCost(int cost);

    KnowledgeType getKnowledgeType();

    IUnlockCondition setKnowledgeType(KnowledgeType type);
}
