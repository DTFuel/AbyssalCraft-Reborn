package com.shinoow.abyssalcraft.system.knowledge.condition;

/**
 * Unlocked by a miscellaneous trigger (owned by PS-8), faithful to the 1.12.2 {@code MiscCondition}.
 * Processor type {@code 10}: met when the misc key (e.g. {@code coralium_plague}, {@code dread_plague}) is in
 * the necrodata's misc triggers.
 */
public class MiscCondition extends UnlockCondition {

    public MiscCondition(String key) {
        super(10, key);
    }
}
