package com.shinoow.abyssalcraft.system.knowledge.condition;

/**
 * Unlocked by encountering <em>all</em> of the given entities (owned by PS-8), faithful to the 1.12.2
 * {@code MandatoryMultiEntityCondition} (e.g. "killed all bosses"). Processor type {@code 11}: met only when
 * every entity id is in the necrodata's entity triggers. Type {@code 11} is intentionally <b>not</b> bypassed
 * by the "all knowledge" flag (see {@link KnowledgeGate}).
 */
public class MandatoryMultiEntityCondition extends UnlockCondition {

    public MandatoryMultiEntityCondition(String... entityIds) {
        super(11, entityIds);
    }
}
