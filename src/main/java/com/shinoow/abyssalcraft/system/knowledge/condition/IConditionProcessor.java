package com.shinoow.abyssalcraft.system.knowledge.condition;

import com.shinoow.abyssalcraft.system.cap.necrodata.NecroData;

import net.minecraft.world.entity.player.Player;

/**
 * Checks whether one {@link IUnlockCondition} is satisfied by a player's necrodata (owned by PS-8), faithful
 * to the 1.12.2 {@code api.knowledge.condition.IConditionProcessor}. Registered by {@link IUnlockCondition#getType()}
 * in {@link ConditionProcessorRegistry}. Reads PS-2's {@link NecroData} directly (the modernised store).
 */
@FunctionalInterface
public interface IConditionProcessor {

    boolean processUnlock(IUnlockCondition condition, NecroData data, Player player);
}
