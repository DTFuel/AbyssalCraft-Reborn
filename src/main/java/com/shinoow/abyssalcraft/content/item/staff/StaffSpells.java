package com.shinoow.abyssalcraft.content.item.staff;

import com.shinoow.abyssalcraft.system.spell.ManifestSpell;
import com.shinoow.abyssalcraft.system.spell.SpellManifest;
import com.shinoow.abyssalcraft.system.spell.SpellManifestCatalog;
import com.shinoow.abyssalcraft.system.spell.SpellRegistry;
import com.shinoow.abyssalcraft.system.spell.SpellBehaviors;

/**
 * The concrete spells (owned by content/item/staff), registered into the {@link SpellRegistry} (PS-7) at mod
 * init. The pilot life-drain spell proves the end-to-end cast loop (charge the staff at a statue -> aim at a
 * mob -> drain its life to heal). The faithful spell roster (their reagents / scroll tiers / effects) is the
 * PS-7b follow-up. The spell holds no item reference, so it is safe to build at class-load.
 */
public final class StaffSpells {

    private StaffSpells() {}

    public static ManifestSpell lifeDrain() {
        return (ManifestSpell) SpellRegistry.instance().getSpell("lifedrain");
    }

    public static void bootstrap() {
        SpellBehaviors.bootstrap();
        SpellRegistry registry = SpellRegistry.instance();
        for (SpellManifest manifest : SpellManifestCatalog.entries()) {
            registry.registerSpell(new ManifestSpell(manifest));
            for (String alias : manifest.aliases()) registry.registerAlias(alias, manifest.id());
        }
    }
}
