package com.shinoow.abyssalcraft.system.knowledge;

import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;
import com.shinoow.abyssalcraft.system.knowledge.condition.ConditionProcessorRegistry;
import com.shinoow.abyssalcraft.system.knowledge.condition.IUnlockCondition;
import com.shinoow.abyssalcraft.system.knowledge.condition.NecronomiconCondition;
import com.shinoow.abyssalcraft.system.portal.DimensionDataRegistry;

import net.minecraft.world.entity.player.Player;

/**
 * The knowledge gate (owned by PS-8): decides whether an {@link IUnlockCondition} / {@link IResearchItem} is
 * unlocked for a player by reading their necrodata (PS-2's {@link NecroData}), faithful to the 1.12.2
 * {@code NecroDataCapability.isUnlocked(...)} logic. This is the consumption layer PS-2 deliberately left to
 * PS-8 (PS-2 only stores the triggers / completed researches / points).
 *
 * <p>Research that passes is recorded via {@link NecroData#completeResearch(String)} (auto-complete). The
 * {@code hasAllKnowledge} flag short-circuits every condition <b>except</b> the mandatory multi-entity type
 * (11), faithful to the original.
 */
public final class KnowledgeGate {

    private KnowledgeGate() {}

    /** Whether a single condition is satisfied by {@code data}. */
    public static boolean isUnlocked(NecroData data, IUnlockCondition cond, Player player) {
        return isUnlocked(data, cond, player, -1);
    }

    /** Evaluate a condition with the tier of the Necronomicon currently being used. */
    public static boolean isUnlocked(NecroData data, IUnlockCondition cond, Player player, int bookType) {
        if (cond.getType() == -2) {
            return false;
        }
        if (!isBookAllowedInDimension(player, bookType)) {
            return false;
        }
        if (cond instanceof NecronomiconCondition bookCondition) {
            return bookType >= bookCondition.requiredBookType();
        }
        if (cond.getType() == -1 || data.hasUnlockedAllKnowledge() && cond.getType() != 11) {
            return true;
        }
        return ConditionProcessorRegistry.instance().getProcessor(cond.getType()).processUnlock(cond, data, player);
    }

    /** Whether a research is unlocked (all conditions met); auto-completes it in {@code data} when it is. */
    public static boolean isUnlocked(NecroData data, IResearchItem research, Player player) {
        return isUnlocked(data, research, player, -1);
    }

    /** Evaluate and auto-complete research in the context of a specific book tier. */
    public static boolean isUnlocked(NecroData data, IResearchItem research, Player player, int bookType) {
        if (research.getRequiredLevel() == -2) {
            return false;
        }
        if (!isBookAllowedInDimension(player, bookType)) {
            return false;
        }
        if (research.getRequiredLevel() == -1
                || data.hasUnlockedAllKnowledge()
                || data.getCompletedResearches().contains(research.getID().toString())) {
            return true;
        }
        boolean unlocked = true;
        for (IUnlockCondition cond : research.getUnlockConditions()) {
            if (!isUnlocked(data, cond, player, bookType)) {
                unlocked = false;
            }
        }
        if (unlocked) {
            data.completeResearch(research.getID().toString());
        }
        return unlocked;
    }

    private static boolean isBookAllowedInDimension(Player player, int bookType) {
        if (bookType < 0) return true;
        return DimensionDataRegistry.instance().requiredBookType(player.level().dimension()).stream()
            .anyMatch(required -> bookType >= required);
    }
}
