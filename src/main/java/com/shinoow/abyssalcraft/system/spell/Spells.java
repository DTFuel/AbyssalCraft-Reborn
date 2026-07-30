package com.shinoow.abyssalcraft.system.spell;

/** Mounts the source-derived spell manifest and behaviors into their runtime registries. */
public final class Spells {

    private Spells() {}

    public static void bootstrap() {
        SpellBehaviors.bootstrap();
        SpellRegistry registry = SpellRegistry.instance();
        for (SpellManifest manifest : SpellManifestCatalog.entries()) {
            registry.registerSpell(new ManifestSpell(manifest));
        }
        for (SpellManifest manifest : SpellManifestCatalog.entries()) {
            for (String alias : manifest.aliases()) registry.registerAlias(alias, manifest.id());
        }
    }
}