package com.shinoow.abyssalcraft.content.item.staff;

import com.shinoow.abyssalcraft.system.spell.LifeDrainSpell;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;

/**
 * The concrete spells (owned by content/item/staff), registered into the {@link SpellRegistry} (PS-7) at mod
 * init. The pilot life-drain spell proves the end-to-end cast loop (charge the staff at a statue -> aim at a
 * mob -> drain its life to heal). The faithful spell roster (their reagents / scroll tiers / effects) is the
 * PS-7b follow-up. The spell holds no item reference, so it is safe to build at class-load.
 */
public final class StaffSpells {

    private StaffSpells() {}

    /** Canonical 1.12.2 life-drain spell (the transitional {@code life_drain} id remains an alias). */
    public static final LifeDrainSpell LIFE_DRAIN = new LifeDrainSpell("lifedrain", 0, 100F);

    public static void bootstrap() {
        SpellRegistry.instance().registerSpell(LIFE_DRAIN);
        SpellRegistry.instance().registerAlias("life_drain", LIFE_DRAIN.id());
    }
}
