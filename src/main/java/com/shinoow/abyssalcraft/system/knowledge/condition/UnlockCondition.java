package com.shinoow.abyssalcraft.system.knowledge.condition;

import java.util.Arrays;
import java.util.Objects;

import com.shinoow.abyssalcraft.system.cap.necrodata.KnowledgeType;

/**
 * Base {@link IUnlockCondition} (owned by PS-8), faithful to the 1.12.2 condition classes. Holds the processor
 * {@code type} and the {@code conditionObject} the processor compares. Usable directly, or subclassed for a
 * named condition (see {@link EntityCondition}, {@link DimensionCondition}, {@link MiscCondition},
 * {@link MandatoryMultiEntityCondition}).
 */
public class UnlockCondition implements IUnlockCondition {

    private final int type;
    private final Object conditionObject;
    private String hint = "";
    private int pointsCost;
    private KnowledgeType knowledgeType = KnowledgeType.BASE;

    public UnlockCondition(int type, Object conditionObject) {
        this.type = type;
        this.conditionObject = conditionObject;
    }

    @Override
    public boolean areConditionObjectsEqual(Object other) {
        if (conditionObject instanceof Object[] a && other instanceof Object[] b) {
            return Arrays.equals(a, b);
        }
        return Objects.equals(conditionObject, other);
    }

    @Override
    public Object getConditionObject() {
        return conditionObject;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public IUnlockCondition setHint(String hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public String getHint() {
        return hint;
    }

    @Override
    public int getPointsCost() {
        return pointsCost;
    }

    @Override
    public IUnlockCondition setPointsCost(int cost) {
        this.pointsCost = cost;
        return this;
    }

    @Override
    public KnowledgeType getKnowledgeType() {
        return knowledgeType;
    }

    @Override
    public IUnlockCondition setKnowledgeType(KnowledgeType type) {
        this.knowledgeType = type;
        return this;
    }
}
